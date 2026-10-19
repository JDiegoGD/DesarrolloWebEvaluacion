package com.tecsup.auditoria;

import com.tecsup.enums.OperacionAuditoria;
import com.tecsup.model.Auditoria;
import com.tecsup.repository.UsuarioRepository;
import com.tecsup.service.AuditoriaService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Registra en auditoria cada método @Auditable que termina SIN excepción.
 *
 * Por qué @Around y no @AfterReturning: además de leer el resultado, necesita abrir y cerrar el
 * AuditoriaContexto (notas del detalle) alrededor de la llamada, también cuando falla.
 *
 * Por qué @Order(0): el interceptor de @Transactional tiene la precedencia más baja (Ordered.LOWEST_PRECEDENCE),
 * así que este aspecto queda POR FUERA de la transacción. Cuando proceed() devuelve, el commit ya se hizo;
 * si el commit falla (p. ej. una FK al hacer flush) la excepción sale de proceed() y no se audita nada.
 * Si además hubiera una transacción externa, AuditoriaService espera a su commit.
 */
@Aspect
@Component
@Order(0)
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);
    public static final String SISTEMA = "SISTEMA";
    // Cualquier cosa con forma de hash BCrypt se oculta del detalle
    private static final Pattern HASH_BCRYPT = Pattern.compile("\\$2[abxy]?\\$\\d{2}\\$[./A-Za-z0-9]{53}");

    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nombresParametros = new DefaultParameterNameDiscoverer();

    public AuditoriaAspect(AuditoriaService auditoriaService, UsuarioRepository usuarioRepository) {
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
    }

    @Around("@annotation(auditable)")
    public Object auditar(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        List<String> anteriores = AuditoriaContexto.abrir();
        try {
            Object resultado = pjp.proceed();          // si lanza excepción, no se audita
            registrarSinFallar(pjp, auditable, resultado, AuditoriaContexto.notas());
            return resultado;
        } finally {
            AuditoriaContexto.cerrar(anteriores);
        }
    }

    // Nada de lo que ocurra al construir o guardar la auditoría puede afectar a la operación de negocio
    private void registrarSinFallar(ProceedingJoinPoint pjp, Auditable auditable, Object resultado, List<String> notas) {
        try {
            Method metodo = ((MethodSignature) pjp.getSignature()).getMethod();
            MethodBasedEvaluationContext ctx =
                    new MethodBasedEvaluationContext(pjp.getTarget(), metodo, pjp.getArgs(), nombresParametros);
            ctx.setVariable("resultado", resultado);

            OperacionAuditoria operacion = auditable.estado().isBlank()
                    ? auditable.operacion()
                    : (Boolean.TRUE.equals(evaluar(auditable.estado(), ctx, Boolean.class))
                        ? OperacionAuditoria.ACTIVACION : OperacionAuditoria.DESACTIVACION);

            String idRegistro = auditable.id().isBlank()
                    ? idPorConvencion(resultado, auditable.entidad(), pjp.getArgs())
                    : texto(evaluar(auditable.id(), ctx, Object.class));

            String detalle = auditable.detalle().isBlank() ? null : evaluar(auditable.detalle(), ctx, String.class);
            if (detalle == null)
                detalle = "Se realizó " + operacion.name().toLowerCase() + " de " + auditable.entidad()
                        + (idRegistro != null ? " " + idRegistro : "");
            if (!notas.isEmpty()) detalle += ". " + String.join("; ", notas);

            Auditoria a = new Auditoria();
            completarUsuario(a);
            a.setFechaHora(LocalDateTime.now());
            a.setOperacion(operacion);
            a.setEntidad(auditable.entidad());
            a.setIdRegistro(idRegistro);
            a.setDetalle(HASH_BCRYPT.matcher(detalle).replaceAll("[oculto]"));
            auditoriaService.registrar(a);
        } catch (RuntimeException e) {
            log.error("No se pudo preparar la auditoría de {}: {}", pjp.getSignature().toShortString(), e.getMessage(), e);
        }
    }

    private <T> T evaluar(String expresion, MethodBasedEvaluationContext ctx, Class<T> tipo) {
        try {
            return parser.parseExpression(expresion).getValue(ctx, tipo);
        } catch (RuntimeException e) {
            log.warn("Expresión de auditoría inválida '{}': {}", expresion, e.getMessage());
            return null;
        }
    }

    private void completarUsuario(Auditoria a) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            a.setUsuario(SISTEMA);
            return;
        }
        a.setUsuario(auth.getName());
        a.setRolUsuario(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(r -> r.startsWith("ROLE_")).map(r -> r.substring(5)).findFirst().orElse(null));
        try {
            usuarioRepository.findByNombreUsuario(auth.getName()).ifPresent(u -> a.setIdUsuario(u.getIdUsuario()));
        } catch (RuntimeException e) {
            log.warn("Auditoría sin idUsuario para {}: {}", auth.getName(), e.getMessage());
        }
    }

    /**
     * Id del registro sin lógica por entidad: el valor devuelto si es simple (Number/String); si es un
     * objeto, su getId()/id(), luego getId{Entidad}()/id{Entidad}() y luego cualquier otro getId*()/id*();
     * si no devuelve nada, el primer argumento simple (id o número de documento).
     */
    static String idPorConvencion(Object resultado, String entidad, Object[] args) {
        if (resultado instanceof Number || resultado instanceof CharSequence) return resultado.toString();
        if (resultado != null) {
            Optional<Method> getter = Arrays.stream(resultado.getClass().getMethods())
                    .filter(m -> m.getParameterCount() == 0 && !Modifier.isStatic(m.getModifiers()))
                    .filter(m -> Number.class.isAssignableFrom(m.getReturnType())
                            || m.getReturnType() == int.class || m.getReturnType() == long.class
                            || m.getReturnType() == String.class)
                    .filter(m -> m.getName().startsWith("getId") || m.getName().startsWith("id"))
                    .min(Comparator.comparingInt((Method m) -> prioridad(m.getName(), entidad))
                            .thenComparing(Method::getName));
            if (getter.isPresent()) {
                try {
                    Object id = getter.get().invoke(resultado);
                    if (id != null) return id.toString();
                } catch (ReflectiveOperationException e) {
                    log.warn("No se pudo leer {} de {}", getter.get().getName(), resultado.getClass().getSimpleName());
                }
            }
        }
        return Arrays.stream(args)
                .filter(a -> a instanceof Number || a instanceof CharSequence)
                .map(Object::toString).findFirst().orElse(null);
    }

    private static int prioridad(String nombre, String entidad) {
        if (nombre.equals("getId") || nombre.equals("id")) return 0;
        if (nombre.equals("getId" + entidad) || nombre.equals("id" + entidad)) return 1;
        return 2;
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}
