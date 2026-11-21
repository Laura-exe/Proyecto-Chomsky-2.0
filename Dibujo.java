package chomsky.ui;

import chomsky.modelo.Paso;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/** Utilidades de dibujo reutilizadas por los lienzos de los cuatro módulos. */
final class Dibujo {

    private Dibujo() { }

    static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Escribe el texto centrado en (cx, cy). */
    static void centrado(Graphics2D g, String s, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }

    /** Punta de flecha con la punta en (x, y) apuntando en la dirección (dx, dy). */
    static void flecha(Graphics2D g, double x, double y, double dx, double dy, double largo) {
        double n = Math.hypot(dx, dy);
        if (n == 0) return;
        dx /= n;
        dy /= n;
        double px = -dy, py = dx;
        Path2D p = new Path2D.Double();
        p.moveTo(x, y);
        p.lineTo(x - dx * largo + px * largo * 0.45, y - dy * largo + py * largo * 0.45);
        p.lineTo(x - dx * largo - px * largo * 0.45, y - dy * largo - py * largo * 0.45);
        p.closePath();
        g.fill(p);
    }

    /** Texto con un rectángulo de fondo (para que las etiquetas de las flechas se lean). */
    static void etiqueta(Graphics2D g, String s, double cx, double cy, Color fondo, Color texto) {
        FontMetrics fm = g.getFontMetrics();
        double w = fm.stringWidth(s) + 8, h = fm.getHeight() - 2;
        g.setColor(fondo);
        g.fill(new RoundRectangle2D.Double(cx - w / 2, cy - h / 2, w, h, 8, 8));
        g.setColor(texto);
        centrado(g, s, cx, cy);
    }

    /**
     * Dibuja la cadena de entrada en casillas: las ya consumidas aparecen atenuadas, la actual resaltada
     * y debajo de ella una flechita. Devuelve la altura ocupada.
     */
    static int cadena(Graphics2D g, String s, int pos, Color resaltado, int x0, int y, int anchoDisponible) {
        if (s.isEmpty()) {
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.NORMAL);
            g.drawString("Cadena vacía (ε): no hay símbolos que leer", x0, y + 20);
            return 30;
        }
        int n = s.length();
        int c = Math.max(16, Math.min(38, anchoDisponible / (n + 2)));
        g.setFont(Tema.MONO_B.deriveFont((float) (c * 0.55)));
        int inicio = x0 + (anchoDisponible - c * n) / 2;
        for (int i = 0; i < n; i++) {
            Color fondo = i < pos ? new Color(0xE5E7EB) : (i == pos ? resaltado : Color.WHITE);
            RoundRectangle2D caja = new RoundRectangle2D.Double(inicio + i * c + 1, y, c - 2, c - 2, 8, 8);
            g.setColor(fondo);
            g.fill(caja);
            g.setColor(i == pos ? resaltado.darker() : Tema.BORDE);
            g.draw(caja);
            g.setColor(i < pos ? Tema.TEXTO_SUAVE : Tema.TEXTO);
            centrado(g, String.valueOf(s.charAt(i)), inicio + i * c + c / 2.0, y + c / 2.0 - 1);
        }
        g.setColor(resaltado.darker());
        if (pos < n) {
            flecha(g, inicio + pos * c + c / 2.0, y + c + 1, 0, -1, 9);
        } else {
            g.setFont(Tema.NORMAL_B);
            g.drawString("fin de la cadena", inicio + n * c + 8, y + c / 2 + 4);
        }
        return c + 14;
    }

    /** Dibuja el texto cortándolo en líneas del ancho dado. Devuelve la y siguiente a la última línea. */
    static int parrafo(Graphics2D g, String texto, int x, int y, int ancho) {
        FontMetrics fm = g.getFontMetrics();
        int linea = fm.getHeight();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) > ancho && actual.length() > 0) {
                g.drawString(actual.toString(), x, y);
                y += linea;
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) {
            g.drawString(actual.toString(), x, y);
            y += linea;
        }
        return y;
    }

    /**
     * Lista las reglas δ en columnas dentro del rectángulo dado y resalta la que se usó en el paso actual.
     * La regla usada se reconoce porque la descripción del paso empieza con ella.
     */
    static void reglas(Graphics2D g, List<String> reglas, Paso paso, int x, int y, int ancho, int alto) {
        g.setFont(Tema.MONO.deriveFont(12f));
        FontMetrics fm = g.getFontMetrics();
        int linea = fm.getHeight() + 3;
        int porColumna = Math.max(1, alto / linea);
        int anchoColumna = reglas.stream().mapToInt(fm::stringWidth).max().orElse(100) + 26;
        int columnas = Math.max(1, ancho / anchoColumna);
        for (int i = 0; i < reglas.size(); i++) {
            int col = i / porColumna, fila = i % porColumna;
            if (col >= columnas) {
                g.setColor(Tema.TEXTO_SUAVE);
                g.drawString("…", x + (columnas - 1) * anchoColumna, y + porColumna * linea);
                break;
            }
            String r = reglas.get(i);
            int rx = x + col * anchoColumna, ry = y + fila * linea;
            boolean activa = paso != null && paso.descripcion().startsWith(r + Paso.SEP);
            if (activa) {
                g.setColor(Tema.suave(Tema.ACTIVO, 0.55));
                g.fill(new RoundRectangle2D.Double(rx - 4, ry - fm.getAscent() - 1, fm.stringWidth(r) + 8, linea - 1, 6, 6));
            }
            g.setColor(activa ? Tema.TEXTO : Tema.TEXTO_SUAVE);
            g.drawString(r, rx, ry);
        }
    }
}
