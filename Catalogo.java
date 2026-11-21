package chomsky.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de lenguajes de ejemplo para cada nivel de la jerarquía, con sus
 * máquinas ya construidas y sus cadenas de prueba (las mismas que se listan en el README).
 */
public final class Catalogo {

    private Catalogo() { }

    public static final String NOMBRE_MULTIPLOS = "Múltiplos de N en binario (parametrizable)";

    // =====================================================================
    // NIVEL 3 · Lenguajes regulares  →  AFD
    // =====================================================================

    public static List<Lenguaje<AFD>> afds() {
        return List.of(paresDeUnos(), terminaEnAb(), multiplos(3));
    }

    private static Lenguaje<AFD> paresDeUnos() {
        AFD m = new AFD(List.of("q0", "q1"), "01", "q0", List.of("q0"))
                .transicion("q0", '0', "q0").transicion("q0", '1', "q1")
                .transicion("q1", '0', "q1").transicion("q1", '1', "q0");
        return new Lenguaje<>("Cantidad par de 1s (Σ = {0,1})",
                "L = { w ∈ {0,1}* : w tiene una cantidad par de 1s }. El estado q0 recuerda \"van un número par "
                        + "de 1s\" y q1 \"van un número impar\"; cada 1 alterna entre ambos y los 0 no cambian nada.",
                m, List.of(
                new Ejemplo("", true, "ε tiene cero 1s (cero es par)"),
                new Ejemplo("11", true, "dos 1s"),
                new Ejemplo("1001", true, "dos 1s"),
                new Ejemplo("000", true, "ningún 1"),
                new Ejemplo("1", false, "un solo 1"),
                new Ejemplo("10101", false, "tres 1s"),
                new Ejemplo("0110111", false, "cinco 1s")));
    }

    private static Lenguaje<AFD> terminaEnAb() {
        AFD m = new AFD(List.of("q0", "q1", "q2"), "ab", "q0", List.of("q2"))
                .transicion("q0", 'a', "q1").transicion("q0", 'b', "q0")
                .transicion("q1", 'a', "q1").transicion("q1", 'b', "q2")
                .transicion("q2", 'a', "q1").transicion("q2", 'b', "q0");
        return new Lenguaje<>("Cadenas que terminan en \"ab\" (Σ = {a,b})",
                "L = { w ∈ {a,b}* : w termina en ab }. q1 significa \"el último símbolo fue a\" y q2 \"los dos "
                        + "últimos fueron ab\" (único estado de aceptación).",
                m, List.of(
                new Ejemplo("ab", true, "termina en ab"),
                new Ejemplo("aabab", true, "termina en ab"),
                new Ejemplo("bbab", true, "termina en ab"),
                new Ejemplo("abb", false, "termina en bb"),
                new Ejemplo("ba", false, "termina en ba"),
                new Ejemplo("a", false, "solo una a"),
                new Ejemplo("", false, "ε no termina en nada")));
    }

    /**
     * AFD parametrizable: acepta los números binarios que son múltiplos de n.
     * El estado rK significa "lo leído hasta ahora vale K módulo n". Al leer el bit b,
     * el nuevo residuo es (2K + b) mod n, porque agregar un bit duplica el valor y le suma b.
     */
    public static Lenguaje<AFD> multiplos(int n) {
        List<String> estados = new ArrayList<>();
        for (int r = 0; r < n; r++) estados.add("r" + r);
        AFD m = new AFD(estados, "01", "r0", List.of("r0"));
        for (int r = 0; r < n; r++) {
            for (int bit = 0; bit <= 1; bit++) {
                m.transicion("r" + r, (char) ('0' + bit), "r" + ((2 * r + bit) % n));
            }
        }
        List<Ejemplo> ejemplos = new ArrayList<>();
        ejemplos.add(new Ejemplo(Integer.toBinaryString(n), true, n + " = 1·" + n));
        ejemplos.add(new Ejemplo(Integer.toBinaryString(2 * n), true, (2 * n) + " = 2·" + n));
        ejemplos.add(new Ejemplo(Integer.toBinaryString(3 * n), true, (3 * n) + " = 3·" + n));
        ejemplos.add(new Ejemplo("0", true, "0 es múltiplo de todo"));
        ejemplos.add(new Ejemplo(Integer.toBinaryString(n + 1), false, (n + 1) + " no es múltiplo de " + n));
        ejemplos.add(new Ejemplo(Integer.toBinaryString(2 * n + 1), false, (2 * n + 1) + " no es múltiplo de " + n));
        return new Lenguaje<>(NOMBRE_MULTIPLOS,
                "L = { w ∈ {0,1}* : el valor binario de w es múltiplo de " + n + " }. Cambia N con el selector: el "
                        + "autómata se reconstruye con " + n + " estados. El estado rK = \"el valor leído vale K mod "
                        + n + "\". Aquí N = " + n + ".",
                m, ejemplos);
    }

    // =====================================================================
    // NIVEL 2 · Lenguajes libres de contexto  →  Autómata de pila
    // =====================================================================

    public static List<Lenguaje<AP>> aps() {
        return List.of(anbn(), parentesis(), wcwr());
    }

    private static Lenguaje<AP> anbn() {
        AP m = new AP("q0", List.of("q2"), 'Z')
                .regla("q0", 'a', 'Z', "q0", "AZ")
                .regla("q0", 'a', 'A', "q0", "AA")
                .regla("q0", 'b', 'A', "q1", "")
                .regla("q1", 'b', 'A', "q1", "")
                .regla("q1", AP.EPS, 'Z', "q2", "Z");
        return new Lenguaje<>("aⁿbⁿ  (n ≥ 1)",
                "L = { aⁿbⁿ : n ≥ 1 }. Por cada a se apila una A; por cada b se desapila una A. Si al terminar "
                        + "solo queda el fondo Z, la cantidad de a y b coincide. Un AFD no puede hacerlo: necesitaría "
                        + "infinitos estados para contar.",
                m, List.of(
                new Ejemplo("ab", true, "n = 1"),
                new Ejemplo("aabb", true, "n = 2"),
                new Ejemplo("aaabbb", true, "n = 3"),
                new Ejemplo("aab", false, "sobra una a (la pila no queda en Z)"),
                new Ejemplo("abb", false, "sobra una b (pila agotada)"),
                new Ejemplo("abab", false, "las a y las b no están en bloques"),
                new Ejemplo("", false, "n debe ser ≥ 1")));
    }

    private static Lenguaje<AP> parentesis() {
        AP m = new AP("q0", List.of("q1"), 'Z');
        for (char t : new char[]{'Z', '(', '['}) {
            m.regla("q0", '(', t, "q0", "(" + t);
            m.regla("q0", '[', t, "q0", "[" + t);
        }
        m.regla("q0", ')', '(', "q0", "");
        m.regla("q0", ']', '[', "q0", "");
        m.regla("q0", AP.EPS, 'Z', "q1", "Z");
        return new Lenguaje<>("Paréntesis y corchetes balanceados",
                "L = { w ∈ {(,),[,]}* : w está bien balanceada }. Cada apertura se apila; cada cierre debe "
                        + "coincidir con el tope y lo desapila. Es la misma idea que usa un compilador para validar "
                        + "expresiones anidadas.",
                m, List.of(
                new Ejemplo("()", true, "un par"),
                new Ejemplo("(())", true, "anidados"),
                new Ejemplo("([]())", true, "mezcla de tipos"),
                new Ejemplo("", true, "ε está balanceada"),
                new Ejemplo("(()", false, "falta cerrar un paréntesis"),
                new Ejemplo("())", false, "sobra un cierre"),
                new Ejemplo("([)]", false, "se cierra en orden incorrecto")));
    }

    private static Lenguaje<AP> wcwr() {
        AP m = new AP("q0", List.of("q2"), 'Z');
        for (char s : new char[]{'a', 'b'}) {
            char mayus = Character.toUpperCase(s);
            for (char t : new char[]{'Z', 'A', 'B'}) {
                m.regla("q0", s, t, "q0", "" + mayus + t);
            }
        }
        for (char t : new char[]{'Z', 'A', 'B'}) {
            m.regla("q0", 'c', t, "q1", "" + t);
        }
        m.regla("q1", 'a', 'A', "q1", "");
        m.regla("q1", 'b', 'B', "q1", "");
        m.regla("q1", AP.EPS, 'Z', "q2", "Z");
        return new Lenguaje<>("w c wᴿ  (palíndromo con marca central, Σ = {a,b,c})",
                "L = { w c wᴿ : w ∈ {a,b}* } donde wᴿ es w al revés. Antes de la c se apila cada símbolo; después "
                        + "de la c se desapila comparando: la pila entrega los símbolos en orden inverso (LIFO), que "
                        + "es justo lo que exige wᴿ.",
                m, List.of(
                new Ejemplo("abcba", true, "w = ab, wᴿ = ba"),
                new Ejemplo("abbcbba", true, "w = abb"),
                new Ejemplo("aacaa", true, "w = aa"),
                new Ejemplo("c", true, "w = ε"),
                new Ejemplo("abcab", false, "la segunda mitad no es el reverso"),
                new Ejemplo("abba", false, "no tiene marca central c"),
                new Ejemplo("acb", false, "a ≠ b")));
    }

    // =====================================================================
    // NIVEL 1 · Lenguajes sensibles al contexto  →  Autómata linealmente acotado
    // =====================================================================

    public static List<Lenguaje<MaquinaTuring>> alas() {
        return List.of(anbncn());
    }

    /**
     * a^n b^n c^n no es libre de contexto (una sola pila no puede comparar tres bloques),
     * pero un LBA lo reconoce marcando en cada ronda una a (X), una b (Y) y una c (Z).
     */
    private static Lenguaje<MaquinaTuring> anbncn() {
        MaquinaTuring m = new MaquinaTuring(true, "q0", List.of("qf"), "abc")
                .regla("q0", '<', '<', 'R', "q1")
                // q1: busca la primera 'a' sin marcar; si ya no hay, verifica que todo lo demás esté marcado
                .regla("q1", 'a', 'X', 'R', "q2")
                .regla("q1", 'Y', 'Y', 'R', "q5")
                // q2: avanza sobre a's y Y's hasta encontrar la primera 'b' sin marcar
                .regla("q2", 'a', 'a', 'R', "q2")
                .regla("q2", 'Y', 'Y', 'R', "q2")
                .regla("q2", 'b', 'Y', 'R', "q3")
                // q3: avanza sobre b's y Z's hasta encontrar la primera 'c' sin marcar
                .regla("q3", 'b', 'b', 'R', "q3")
                .regla("q3", 'Z', 'Z', 'R', "q3")
                .regla("q3", 'c', 'Z', 'L', "q4")
                // q4: regresa hacia la izquierda hasta la última X
                .regla("q4", 'X', 'X', 'R', "q1")
                // q5/q6: verificación final: solo deben quedar Y's seguidas de Z's y luego el delimitador
                .regla("q5", 'Y', 'Y', 'R', "q5")
                .regla("q5", 'Z', 'Z', 'R', "q6")
                .regla("q6", 'Z', 'Z', 'R', "q6")
                .regla("q6", '>', '>', 'S', "qf");
        for (char s : "abYZ".toCharArray()) {
            m.regla("q4", s, s, 'L', "q4");
        }
        return new Lenguaje<>("aⁿbⁿcⁿ  (n ≥ 1)",
                "L = { aⁿbⁿcⁿ : n ≥ 1 }. No es libre de contexto, pero un autómata linealmente acotado lo "
                        + "reconoce sin salirse de la cinta <w>: en cada ronda marca una a (X), una b (Y) y una c (Z). "
                        + "Si al final todo quedó marcado y en orden, acepta.",
                m, List.of(
                new Ejemplo("abc", true, "n = 1"),
                new Ejemplo("aabbcc", true, "n = 2"),
                new Ejemplo("aaabbbccc", true, "n = 3"),
                new Ejemplo("aabbc", false, "falta una c"),
                new Ejemplo("abcc", false, "sobra una c"),
                new Ejemplo("abcabc", false, "no están en bloques"),
                new Ejemplo("acb", false, "orden incorrecto")));
    }

    // =====================================================================
    // NIVEL 0 · Lenguajes recursivamente enumerables  →  Máquina de Turing
    // =====================================================================

    public static List<Lenguaje<MaquinaTuring>> turings() {
        return List.of(palindromos(), sumaUnaria(), incrementoBinario());
    }

    /** Acepta los palíndromos binarios borrando el primer y el último símbolo en cada ronda. */
    private static Lenguaje<MaquinaTuring> palindromos() {
        MaquinaTuring m = new MaquinaTuring(false, "q0", List.of("qf"), "01")
                // q0: lee el primer símbolo, lo borra y lo "recuerda" yendo a q1 (era 0) o q4 (era 1)
                .regla("q0", '0', '_', 'R', "q1")
                .regla("q0", '1', '_', 'R', "q4")
                .regla("q0", '_', '_', 'S', "qf")
                // q1: viaja hasta el final de la cadena
                .regla("q1", '0', '0', 'R', "q1")
                .regla("q1", '1', '1', 'R', "q1")
                .regla("q1", '_', '_', 'L', "q2")
                // q2: el último símbolo debe ser 0 (si ya no hay nada, la cadena tenía longitud impar)
                .regla("q2", '0', '_', 'L', "q3")
                .regla("q2", '_', '_', 'S', "qf")
                // q3: regresa al inicio
                .regla("q3", '0', '0', 'L', "q3")
                .regla("q3", '1', '1', 'L', "q3")
                .regla("q3", '_', '_', 'R', "q0")
                // q4/q5: igual que q1/q2 pero para el caso en que el primer símbolo era 1
                .regla("q4", '0', '0', 'R', "q4")
                .regla("q4", '1', '1', 'R', "q4")
                .regla("q4", '_', '_', 'L', "q5")
                .regla("q5", '1', '_', 'L', "q3")
                .regla("q5", '_', '_', 'S', "qf");
        return new Lenguaje<>("Palíndromos binarios (acepta / rechaza)",
                "L = { w ∈ {0,1}* : w = wᴿ }. La máquina borra el primer símbolo, viaja hasta el final y comprueba "
                        + "que el último sea igual; si lo es, lo borra y repite con lo que queda. Si se queda sin "
                        + "símbolos (o con uno solo) acepta; si no coinciden, se atasca y rechaza.",
                m, List.of(
                new Ejemplo("", true, "ε es palíndromo"),
                new Ejemplo("0", true, "un símbolo"),
                new Ejemplo("0110", true, "longitud par"),
                new Ejemplo("10101", true, "longitud impar"),
                new Ejemplo("01", false, "0 ≠ 1"),
                new Ejemplo("0111", false, "primer y último símbolo distintos"),
                new Ejemplo("1100", false, "no es simétrica")));
    }

    /** Función aritmética: 1^a + 1^b  →  1^(a+b)  (números en notación unaria). */
    private static Lenguaje<MaquinaTuring> sumaUnaria() {
        MaquinaTuring m = new MaquinaTuring(false, "q0", List.of("qf"), "1+")
                // q0: recorre el primer sumando; al llegar al '+' lo reemplaza por un 1 (une los dos bloques)
                .regla("q0", '1', '1', 'R', "q0")
                .regla("q0", '+', '1', 'R', "q1")
                // q1: recorre el segundo sumando hasta el blanco final
                .regla("q1", '1', '1', 'R', "q1")
                .regla("q1", '_', '_', 'L', "q2")
                // q2: se unió un 1 de más, así que se borra el último
                .regla("q2", '1', '_', 'S', "qf");
        return new Lenguaje<>("Suma unaria:  1ᵃ + 1ᵇ  →  1ᵃ⁺ᵇ",
                "Función f(a, b) = a + b con los números escritos en unario (3 = 111). Entrada: 111+11. La "
                        + "máquina cambia el + por un 1 (juntando los dos bloques) y borra el último 1 sobrante. "
                        + "El resultado queda escrito en la cinta cuando la máquina se detiene.",
                m, List.of(
                new Ejemplo("11+111", true, "2 + 3 = 5 → 11111"),
                new Ejemplo("1+1", true, "1 + 1 = 2 → 11"),
                new Ejemplo("111+", true, "3 + 0 = 3 → 111"),
                new Ejemplo("+11", true, "0 + 2 = 2 → 11"),
                new Ejemplo("1111+1111", true, "4 + 4 = 8 → 11111111")));
    }

    /** Función aritmética: incremento binario n → n + 1 (demuestra el crecimiento de la cinta hacia la izquierda). */
    private static Lenguaje<MaquinaTuring> incrementoBinario() {
        MaquinaTuring m = new MaquinaTuring(false, "q0", List.of("qf"), "01")
                // q0: va hasta el bit menos significativo (el último)
                .regla("q0", '0', '0', 'R', "q0")
                .regla("q0", '1', '1', 'R', "q0")
                .regla("q0", '_', '_', 'L', "q1")
                // q1: suma 1 con acarreo: los 1 pasan a 0 hacia la izquierda hasta hallar un 0 (o un blanco)
                .regla("q1", '1', '0', 'L', "q1")
                .regla("q1", '0', '1', 'S', "qf")
                .regla("q1", '_', '1', 'S', "qf");
        return new Lenguaje<>("Incremento binario:  n → n + 1",
                "Función f(n) = n + 1 con n en binario. Va al último bit y propaga el acarreo hacia la izquierda. "
                        + "Con 111 el acarreo se sale de la cinta y esta se EXTIENDE con un blanco a la izquierda: "
                        + "ahí se ve que la cinta es infinita.",
                m, List.of(
                new Ejemplo("1011", true, "11 + 1 = 12 → 1100"),
                new Ejemplo("111", true, "7 + 1 = 8 → 1000 (la cinta crece a la izquierda)"),
                new Ejemplo("100", true, "4 + 1 = 5 → 101"),
                new Ejemplo("0", true, "0 + 1 = 1 → 1"),
                new Ejemplo("1111", true, "15 + 1 = 16 → 10000")));
    }
}
