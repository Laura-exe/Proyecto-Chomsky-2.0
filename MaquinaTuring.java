package chomsky.modelo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

//Niveles 0 y 1 de Chomsky 
public class MaquinaTuring {

    public static final char BLANCO = '_';
    public static final char IZQ = '<';
    public static final char DER = '>';
    public static final int MAX_PASOS = 5000;

    public record Regla(char escribe, char mov, String siguiente) { }

    private final boolean acotada;
    private final String inicial;
    private final Set<String> finales = new LinkedHashSet<>();
    private final String sigma;
    private final Map<String, Regla> delta = new LinkedHashMap<>();
    private final Map<String, String> textos = new LinkedHashMap<>();
    private final Set<Character> gamma = new TreeSet<>();

    public MaquinaTuring(boolean acotada, String inicial, Collection<String> finales, String sigma) {
        this.acotada = acotada;
        this.inicial = inicial;
        this.finales.addAll(finales);
        this.sigma = sigma;
        if (acotada) {
            gamma.add(IZQ);
            gamma.add(DER);
        } else {
            gamma.add(BLANCO);
        }
        for (char c : sigma.toCharArray()) gamma.add(c);
    }

    // Agrega (q, leer) = (siguiente, escribe, mov)
    public MaquinaTuring regla(String q, char leer, char escribe, char mov, String siguiente) {
        String k = clave(q, leer);
        delta.put(k, new Regla(escribe, mov, siguiente));
        textos.put(k, "δ(" + q + ", " + leer + ") = (" + siguiente + ", " + escribe + ", " + mov + ")");
        gamma.add(leer);
        gamma.add(escribe);
        return this;
    }

    private static String clave(String q, char c) {
        return q + "|" + c;
    }

    public boolean acotada() { return acotada; }
    public String inicial() { return inicial; }
    public Set<String> finales() { return finales; }
    public String sigma() { return sigma; }
    public Set<Character> gamma() { return gamma; }
    public List<String> reglas() { return new ArrayList<>(textos.values()); }

    // resultado de una función 
    public static String contenido(List<Character> cinta) {
        StringBuilder sb = new StringBuilder();
        for (char c : cinta) {
            if (c != BLANCO && c != IZQ && c != DER) sb.append(c);
        }
        return sb.toString();
    }

    public List<Paso> simular(String w) {
        List<Paso> pasos = new ArrayList<>();
        List<Character> cinta = new ArrayList<>();
        if (acotada) {
            cinta.add(IZQ);
            for (char c : w.toCharArray()) cinta.add(c);
            cinta.add(DER);
        } else {
            for (char c : w.toCharArray()) cinta.add(c);
            if (cinta.isEmpty()) cinta.add(BLANCO);
        }
        int cab = 0;
        String q = inicial;
        pasos.add(Paso.mt(q, acotada
                ? "Cinta inicial: <w> con delimitadores. El cabezal empieza sobre el delimitador izquierdo."
                : "Cinta inicial con la entrada. El cabezal empieza sobre el primer símbolo.",
                Veredicto.EN_CURSO, cinta, cab));

        for (int n = 0; n < MAX_PASOS; n++) {
            if (finales.contains(q)) {
                pasos.add(Paso.mt(q, "Se alcanzó el estado de aceptación " + q + " ⇒ ACEPTADA",
                        Veredicto.ACEPTADA, cinta, cab));
                return pasos;
            }
            char leido = cinta.get(cab);
            String k = clave(q, leido);
            Regla r = delta.get(k);
            if (r == null) {
                pasos.add(Paso.mt(q, "No hay transición para (" + q + ", " + leido
                        + "): la máquina se detiene sin aceptar ⇒ RECHAZADA", Veredicto.RECHAZADA, cinta, cab));
                return pasos;
            }
            if (acotada && (leido == IZQ || leido == DER) && r.escribe() != leido) {
                pasos.add(Paso.mt(q, "El LBA no puede modificar los delimitadores ⇒ RECHAZADA",
                        Veredicto.RECHAZADA, cinta, cab));
                return pasos;
            }

            cinta.set(cab, r.escribe());
            int nuevo = cab + (r.mov() == 'R' ? 1 : r.mov() == 'L' ? -1 : 0);
            if (nuevo < 0 || nuevo >= cinta.size()) {
                if (acotada) {
                    pasos.add(Paso.mt(q, textos.get(k) + Paso.SEP + "el cabezal intenta salir de la frontera de la "
                            + "cinta acotada ⇒ RECHAZADA", Veredicto.RECHAZADA, cinta, cab));
                    return pasos;
                }
                if (nuevo < 0) {          // la cinta crece hacia la izquierda
                    cinta.add(0, BLANCO);
                    nuevo = 0;
                } else {                   // la cinta crece hacia la derecha
                    cinta.add(BLANCO);
                }
            }
            String accion = "escribir '" + r.escribe() + "', "
                    + (r.mov() == 'R' ? "mover a la derecha" : r.mov() == 'L' ? "mover a la izquierda" : "no mover")
                    + ", pasar a " + r.siguiente();
            cab = nuevo;
            q = r.siguiente();
            pasos.add(Paso.mt(q, textos.get(k) + Paso.SEP + accion, Veredicto.EN_CURSO, cinta, cab));
        }

        pasos.add(Paso.mt(q, "Se superó el límite de " + MAX_PASOS
                + " pasos (posible ciclo infinito) ⇒ RECHAZADA", Veredicto.RECHAZADA, cinta, cab));
        return pasos;
    }
}
