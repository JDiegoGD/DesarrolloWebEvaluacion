package com.tecsup.auditoria;

import java.util.ArrayList;
import java.util.List;

/**
 * Notas que un método @Auditable puede añadir al detalle durante su ejecución (por ejemplo los campos
 * modificados). Vive en el hilo de la petición; AuditoriaAspect lo abre y lo cierra en cada llamada.
 */
public final class AuditoriaContexto {

    private static final ThreadLocal<List<String>> NOTAS = new ThreadLocal<>();

    private AuditoriaContexto() { }

    /** Sin efecto si no hay un método @Auditable en curso. */
    public static void anotar(String nota) {
        List<String> notas = NOTAS.get();
        if (notas != null && nota != null && !nota.isBlank()) notas.add(nota);
    }

    static List<String> abrir() {
        List<String> anteriores = NOTAS.get();
        NOTAS.set(new ArrayList<>());
        return anteriores;
    }

    static List<String> notas() {
        List<String> notas = NOTAS.get();
        return notas == null ? List.of() : List.copyOf(notas);
    }

    static void cerrar(List<String> anteriores) {
        if (anteriores == null) NOTAS.remove();
        else NOTAS.set(anteriores);
    }
}
