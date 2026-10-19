package com.tecsup.auditoria;

import com.tecsup.enums.OperacionAuditoria;
import java.lang.annotation.*;

/**
 * Marca un método de servicio cuya ejecución EXITOSA se registra en la tabla auditoria.
 * Debe ir en el método que llama el controlador: las llamadas internas (this.metodo()) no pasan
 * por el proxy y no se auditan.
 *
 * Expresiones SpEL disponibles en id, detalle y estado: los argumentos por nombre (#numeroDocumento,
 * #dto...) o posición (#p0, #a0) y el valor devuelto (#resultado).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    String entidad();

    OperacionAuditoria operacion() default OperacionAuditoria.MODIFICACION;

    /** SpEL booleana opcional: si se indica, la operación es ACTIVACION (true) o DESACTIVACION (false). */
    String estado() default "";

    /** SpEL opcional del id; si está vacío se toma del resultado (getId*, id*) o del primer argumento simple. */
    String id() default "";

    /** SpEL opcional del texto del detalle; si está vacío se genera uno genérico. Nunca incluir contraseñas. */
    String detalle() default "";
}
