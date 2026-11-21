package chomsky.modelo;

import java.util.List;

/**
 * Una "fotografía" de la máquina en un instante de la ejecución.
 *
 * Cada simulador (AFD, AP, MT/LBA) NO dibuja nada: solo produce una lista de Paso.
 * La interfaz gráfica recorre esa lista hacia adelante o hacia atrás, y por eso
 * el botón "Atrás" es trivial: basta con mostrar el paso anterior.
 *
 * @param estado      estado de control actual
 * @param pos         símbolos de la entrada ya consumidos (AFD y AP); -1 si no aplica
 * @param descripcion texto que explica qué ocurrió (empieza con la regla δ usada)
 * @param veredicto   EN_CURSO, ACEPTADA o RECHAZADA
 * @param pila        contenido de la pila (índice 0 = fondo, último = tope); vacía si no aplica
 * @param cinta       contenido de la cinta (MT y LBA); vacía si no aplica
 * @param cabezal     posición del cabezal dentro de la cinta; -1 si no aplica
 * @param desde       estado anterior (sirve para resaltar la flecha usada en el AFD)
 */
public record Paso(
        String estado,
        int pos,
        String descripcion,
        Veredicto veredicto,
        List<Character> pila,
        List<Character> cinta,
        int cabezal,
        String desde) {

    /** Separador entre la regla δ y su explicación en lenguaje natural. */
    public static final String SEP = "  →  ";

    public static Paso afd(String estado, int pos, String desc, Veredicto v, String desde) {
        return new Paso(estado, pos, desc, v, List.of(), List.of(), -1, desde);
    }

    public static Paso ap(String estado, int pos, String desc, Veredicto v, List<Character> pila) {
        return new Paso(estado, pos, desc, v, List.copyOf(pila), List.of(), -1, null);
    }

    public static Paso mt(String estado, String desc, Veredicto v, List<Character> cinta, int cabezal) {
        return new Paso(estado, -1, desc, v, List.of(), List.copyOf(cinta), cabezal, null);
    }
}
