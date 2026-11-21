package chomsky.ui;

import java.awt.Color;
import java.awt.Font;

/** Colores y fuentes compartidos por toda la aplicación (un solo lugar para cambiar el estilo). */
public final class Tema {

    private Tema() { }

    public static final Color FONDO = new Color(0xF4F6FA);
    public static final Color TEXTO = new Color(0x1F2937);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);
    public static final Color BORDE = new Color(0xD1D5DB);
    public static final Color PRIMARIO = new Color(0x2F5BEA);

    /** Estado o símbolo activo. */
    public static final Color ACTIVO = new Color(0xFBBF24);
    public static final Color OK = new Color(0x22A55E);
    public static final Color ERROR = new Color(0xDC4C4C);

    /** Un color de acento por nivel de la jerarquía: índice 0 = Tipo 0 ... índice 3 = Tipo 3. */
    public static final Color[] NIVEL = {
            new Color(0x7C3AED),   // Tipo 0 · violeta
            new Color(0xEA7A1A),   // Tipo 1 · naranja
            new Color(0x0E9AA7),   // Tipo 2 · turquesa
            new Color(0x2F5BEA)    // Tipo 3 · azul
    };

    public static final Font NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font NORMAL_B = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    public static final Font MONO_B = new Font(Font.MONOSPACED, Font.BOLD, 16);

    /** Mezcla un color con blanco: sirve para fondos suaves a partir del color de acento. */
    public static Color suave(Color c, double cantidad) {
        int r = (int) (c.getRed() + (255 - c.getRed()) * cantidad);
        int g = (int) (c.getGreen() + (255 - c.getGreen()) * cantidad);
        int b = (int) (c.getBlue() + (255 - c.getBlue()) * cantidad);
        return new Color(r, g, b);
    }
}
