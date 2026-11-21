package chomsky.modelo;

/**
 * Cadena de prueba que aparece en la interfaz.
 *
 * @param cadena  la cadena de entrada (vacía = ε)
 * @param acepta  resultado esperado según la definición matemática del lenguaje
 * @param nota    breve justificación (se muestra como ayuda emergente)
 */
public record Ejemplo(String cadena, boolean acepta, String nota) {
}
