package chomsky.modelo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AFD {

    /** Arista del grafo de estados (varias transiciones con mismo origen y destino se agrupan). */
    public record Arista(String desde, String hacia, String etiqueta) { }

    private final List<String> estados = new ArrayList<>();
    private final String alfabeto;
    private final String inicial;
    private final Set<String> finales = new LinkedHashSet<>();
    private final Map<String, Map<Character, String>> delta = new LinkedHashMap<>();

    public AFD(List<String> estados, String alfabeto, String inicial, Collection<String> finales) {
        this.estados.addAll(estados);
        this.alfabeto = alfabeto;
        this.inicial = inicial;
        this.finales.addAll(finales);
        for (String e : estados) {
            delta.put(e, new LinkedHashMap<>());
        }
    }

    /** Define (qo, simbolo) */
    public AFD transicion(String desde, char simbolo, String hacia) {
        delta.get(desde).put(simbolo, hacia);
        return this;
    }

    public List<String> estados() { return estados; }
    public String alfabeto() { return alfabeto; }
    public String inicial() { return inicial; }
    public Set<String> finales() { return finales; }

    /** Aristas  */
    public List<Arista> aristas() {
        Map<String, List<Character>> grupos = new LinkedHashMap<>();
        for (String e : estados) {
            for (Map.Entry<Character, String> t : delta.get(e).entrySet()) {
                grupos.computeIfAbsent(e + "\u0000" + t.getValue(), k -> new ArrayList<>()).add(t.getKey());
            }
        }
        List<Arista> resultado = new ArrayList<>();
        for (Map.Entry<String, List<Character>> g : grupos.entrySet()) {
            String[] partes = g.getKey().split("\u0000");
            StringBuilder etiqueta = new StringBuilder();
            for (char c : g.getValue()) {
                if (etiqueta.length() > 0) etiqueta.append(',');
                etiqueta.append(c);
            }
            resultado.add(new Arista(partes[0], partes[1], etiqueta.toString()));
        }
        return resultado;
    }

    /** devuelve la traza completa. */
    public List<Paso> simular(String w) {
        List<Paso> pasos = new ArrayList<>();
        String q = inicial;
        pasos.add(Paso.afd(q, 0, "Estado inicial " + q + ". Aún no se ha leído ningún símbolo.",
                Veredicto.EN_CURSO, null));

        for (int i = 0; i < w.length(); i++) {
            char c = w.charAt(i);
            String siguiente = delta.get(q).get(c);
            if (siguiente == null) {
                pasos.add(Paso.afd(q, i,
                        "El símbolo '" + c + "' no está definido desde " + q
                                + " (no pertenece al alfabeto Σ = {" + alfabeto + "})" + " ⇒ RECHAZADA",
                        Veredicto.RECHAZADA, q));
                return pasos;
            }
            String desc = "δ(" + q + ", " + c + ") = " + siguiente + Paso.SEP
                    + "leer '" + c + "' y pasar de " + q + " a " + siguiente;
            pasos.add(Paso.afd(siguiente, i + 1, desc, Veredicto.EN_CURSO, q));
            q = siguiente;
        }

        boolean acepta = finales.contains(q);
        String desc = acepta
                ? "Cadena terminada. " + q + " es un estado de aceptación ⇒ ACEPTADA"
                : "Cadena terminada. " + q + " NO es un estado de aceptación ⇒ RECHAZADA";
        pasos.add(Paso.afd(q, w.length(), desc, acepta ? Veredicto.ACEPTADA : Veredicto.RECHAZADA, null));
        return pasos;
    }
}
