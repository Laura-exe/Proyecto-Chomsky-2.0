package chomsky.ui;

import chomsky.modelo.AP;
import chomsky.modelo.Catalogo;
import chomsky.modelo.Lenguaje;
import chomsky.modelo.Paso;

import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Módulo del Nivel 2: muestra la pila (memoria LIFO) como una columna de bloques.
 * Cuando algo se apila, el bloque cae desde arriba; cuando se desapila, el bloque sale hacia arriba
 * y se desvanece. Así se ve exactamente qué cambió en cada paso.
 */
public class PanelAP extends PanelModulo {

    private AP ap;

    public PanelAP(Runnable alMenu) {
        super("Nivel 2 · Lenguajes libres de contexto",
                "Autómata de Pila (AP): un estado de control + una pila LIFO de tamaño ilimitado",
                Tema.NIVEL[2], alMenu);
        inicializar();
    }

    @Override
    protected List<?> lenguajes() {
        return Catalogo.aps();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void seleccionar(Object lenguaje) {
        Lenguaje<AP> l = (Lenguaje<AP>) lenguaje;
        ap = l.maquina();
        establecerDescripcion(l.descripcion());
        establecerEjemplos(l.ejemplos());
    }

    @Override
    protected List<Paso> simular(String w) {
        return ap.simular(w);
    }

    @Override
    protected Lienzo crearLienzo() {
        return new LienzoAP();
    }

    private class LienzoAP extends Lienzo {

        private List<Character> anterior = List.of();
        private List<Character> actual = List.of();
        private int comun = 0;          // cuántos bloques (desde el fondo) no cambiaron
        private double t = 1;           // progreso de la animación: 0 = inicio, 1 = fin
        private final Timer animacion = new Timer(16, e -> {
            t = Math.min(1, t + 0.09);
            repaint();
            if (t >= 1) ((Timer) e.getSource()).stop();
        });

        @Override
        public void mostrar(Paso p, String entrada) {
            anterior = paso == null ? List.of() : paso.pila();
            actual = p == null ? List.of() : p.pila();
            int k = 0;
            while (k < anterior.size() && k < actual.size() && anterior.get(k).equals(actual.get(k))) k++;
            comun = k;
            boolean cambio = p != null && (anterior.size() != actual.size() || comun < actual.size());
            t = cambio ? 0 : 1;
            super.mostrar(p, entrada);
            if (cambio) animacion.restart();
        }

        @Override
        protected void pintar(Graphics2D g, int w, int h) {
            if (ap == null) return;
            int pos = paso == null ? 0 : paso.pos();
            int altoCadena = Dibujo.cadena(g, entrada, pos, Tema.ACTIVO, 20, 14, w - 40);

            int top = 14 + altoCadena + 30;
            int baseY = h - 30;
            int colX = Math.max(150, w / 5);   // centro de la columna de la pila

            // ---- Pila ----
            g.setFont(Tema.NORMAL_B.deriveFont(15f));
            g.setColor(Tema.TEXTO);
            Dibujo.centrado(g, "PILA (LIFO)", colX, top - 6);
            int maxBloques = Math.max(Math.max(anterior.size(), actual.size()), 6);
            double bh = Math.min(38, (baseY - top - 30) / (double) maxBloques);
            double bw = 96;
            g.setColor(Tema.BORDE);
            g.setStroke(new BasicStroke(3f));
            g.drawLine((int) (colX - bw / 2 - 8), top + 8, (int) (colX - bw / 2 - 8), baseY);
            g.drawLine((int) (colX + bw / 2 + 8), top + 8, (int) (colX + bw / 2 + 8), baseY);
            g.drawLine((int) (colX - bw / 2 - 8), baseY, (int) (colX + bw / 2 + 8), baseY);

            double e = 1 - Math.pow(1 - t, 3);   // suavizado de la animación (frena al final)
            // bloques que salen (estaban antes y ya no están)
            for (int i = comun; i < anterior.size(); i++) {
                bloque(g, anterior.get(i), colX, baseY - (i + 1) * bh - 2, bw, bh, 1 - e, -e * 50, false, false);
            }
            // bloques estables y bloques que entran
            for (int i = 0; i < actual.size(); i++) {
                boolean nuevo = i >= comun;
                boolean tope = i == actual.size() - 1;
                bloque(g, actual.get(i), colX, baseY - (i + 1) * bh - 2, bw, bh,
                        nuevo ? e : 1, nuevo ? -(1 - e) * 50 : 0, tope, i == 0);
            }
            if (!actual.isEmpty()) {
                double yTope = baseY - actual.size() * bh - 2;
                g.setColor(Tema.NIVEL[2].darker());
                g.setFont(Tema.NORMAL_B);
                g.drawString("tope", (int) (colX + bw / 2 + 22), (int) (yTope + bh / 2 + 4));
                Dibujo.flecha(g, colX + bw / 2 + 4, yTope + bh / 2, -1, 0, 9);
            } else if (paso != null) {
                g.setColor(Tema.ERROR);
                g.setFont(Tema.NORMAL_B);
                Dibujo.centrado(g, "pila vacía", colX, baseY - 20);
            }

            // ---- Panel derecho: estado, acción y reglas ----
            int x0 = colX + 140, ancho = w - x0 - 20;
            if (ancho < 200) return;
            String estado = paso == null ? "—" : paso.estado();
            boolean acepta = paso != null && ap.finales().contains(estado);
            g.setFont(Tema.NORMAL_B.deriveFont(15f));
            g.setColor(Tema.TEXTO);
            g.drawString("Estado actual:", x0, top);
            Color c = paso == null ? Tema.TEXTO_SUAVE : colorVeredicto();
            g.setColor(c);
            g.fill(new RoundRectangle2D.Double(x0 + 112, top - 19, 48, 26, 14, 14));
            g.setColor(Tema.TEXTO);
            Dibujo.centrado(g, estado, x0 + 136, top - 5);
            g.setFont(Tema.NORMAL.deriveFont(12f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString(acepta ? "(estado de aceptación)" : "(no es de aceptación)", x0 + 170, top);

            int y = top + 30;
            if (paso != null) {
                g.setFont(Tema.NORMAL_B);
                g.setColor(Tema.TEXTO);
                g.drawString("Lo que acaba de pasar:", x0, y);
                g.setFont(Tema.NORMAL);
                y = Dibujo.parrafo(g, paso.descripcion(), x0, y + 20, ancho) + 14;
            }
            g.setFont(Tema.NORMAL_B);
            g.setColor(Tema.TEXTO);
            g.drawString("Reglas δ(estado, entrada, tope) = (nuevo estado, lo que se apila)", x0, y);
            Dibujo.reglas(g, ap.reglas(), paso, x0, y + 20, ancho, baseY - y - 20);
        }

        private void bloque(Graphics2D g, char simbolo, double cx, double y, double bw, double bh,
                            double alfa, double dy, boolean tope, boolean fondo) {
            Color relleno = fondo ? new Color(0xE5E7EB) : Tema.suave(Tema.NIVEL[2], tope ? 0.2 : 0.55);
            g.setColor(conAlfa(relleno, alfa));
            RoundRectangle2D r = new RoundRectangle2D.Double(cx - bw / 2, y + dy, bw, bh - 3, 10, 10);
            g.fill(r);
            g.setStroke(new BasicStroke(tope ? 2.4f : 1.4f));
            g.setColor(conAlfa(tope ? Tema.NIVEL[2].darker() : Tema.TEXTO_SUAVE, alfa));
            g.draw(r);
            g.setFont(Tema.MONO_B.deriveFont(Math.min(20f, (float) bh * 0.6f)));
            g.setColor(conAlfa(Tema.TEXTO, alfa));
            Dibujo.centrado(g, String.valueOf(simbolo), cx, y + dy + (bh - 3) / 2);
        }

        private Color conAlfa(Color c, double a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.max(0, Math.min(255, a * 255)));
        }
    }
}
