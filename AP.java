package chomsky.modelo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Nivel 2 de Chomsky: Autómata de Pila determinista (APD)
public class AP {

    // Vacio 
    public static final char EPS = 'ε';

    public record Regla(String siguiente, String apila) { }

    private final String inicial;
    private final Set<String> finales = new LinkedHashSet<>();
    private final char fondo;
    private final Map<String, Regla> delta = new LinkedHashMap<>();
    private final Map<String, String> textos = new LinkedHashMap<>();

    public AP(String inicial, Collection<String> finales, char fondo) {
        this.inicial = inicial;
        this.finales.addAll(finales);
        this.fondo = fondo;
    }

    //(q, entrada, tope) = (siguiente, apila) 
    public AP regla(String q, char entrada, char tope, String siguiente, String apila) {
        String k = clave(q, entrada, tope);
        delta.put(k, new Regla(siguiente, apila));
        textos.put(k, "δ(" + q + ", " + entrada + ", " + tope + ") = (" + siguiente + ", "
                + (apila.isEmpty() ? "ε" : apila) + ")");
        return this;
    }

    private static String clave(String q, char entrada, char tope) {
        return q + "|" + entrada + "|" + tope;
    }

    public String inicial() { return inicial; }
    public Set<String> finales() { return finales; }
    public char fondo() { return fondo; }

    // Reglas  
    public List<String> reglas() { return new ArrayList<>(textos.values()); }

    public List<Paso> simular(String w) {
        List<Paso> pasos = new ArrayList<>();
        List<Character> pila = new ArrayList<>();
        pila.add(fondo);
        String q = inicial;
        int i = 0;
        pasos.add(Paso.ap(q, 0, "Configuración inicial: estado " + q + ", pila con solo el símbolo de fondo "
                + fondo + ".", Veredicto.EN_CURSO, pila));

        for (int guardia = 0; guardia < 10_000 && !pila.isEmpty(); guardia++) {
            char tope = pila.get(pila.size() - 1);

            // Se prefiere la transición que consume si no existe = vacio.
            String clave = null;
            boolean consume = false;
            if (i < w.length() && delta.containsKey(clave(q, w.charAt(i), tope))) {
                clave = clave(q, w.charAt(i), tope);
                consume = true;
            } else if (delta.containsKey(clave(q, EPS, tope))) {
                clave = clave(q, EPS, tope);
            }
            if (clave == null) break;

            Regla r = delta.get(clave);
            pila.remove(pila.size() - 1);                       // desapilar el tope
            for (int k = r.apila().length() - 1; k >= 0; k--) { // apilar γ (primer símbolo queda arriba)
                pila.add(r.apila().charAt(k));
            }
            String accion = (consume ? "leer '" + w.charAt(i) + "'" : "sin leer entrada (movimiento ε)") + ", ";
            if (r.apila().isEmpty()) {
                accion += "desapilar " + tope;
            } else if (r.apila().equals(String.valueOf(tope))) {
                accion += "el tope " + tope + " se mantiene";
            } else {
                accion += "reemplazar el tope " + tope + " por " + r.apila() + " (el primero queda arriba)";
            }
            if (consume) i++;
            q = r.siguiente();
            pasos.add(Paso.ap(q, i, textos.get(clave) + Paso.SEP + accion, Veredicto.EN_CURSO, pila));
        }

        boolean acepta = i == w.length() && finales.contains(q);
        String motivo;
        if (acepta) {
            motivo = "Entrada consumida y " + q + " es de aceptación ⇒ ACEPTADA";
        } else if (i < w.length()) {
            String tope = pila.isEmpty() ? "pila vacía" : "tope " + pila.get(pila.size() - 1);
            motivo = "No existe transición para (" + q + ", '" + w.charAt(i) + "', " + tope + ") ⇒ RECHAZADA";
        } else {
            motivo = "Entrada consumida pero " + q + " no es de aceptación ⇒ RECHAZADA";
        }
        pasos.add(Paso.ap(q, i, motivo, acepta ? Veredicto.ACEPTADA : Veredicto.RECHAZADA, pila));
        return pasos;
    }
}
