package chomsky.ui;

import chomsky.modelo.AFD;
import chomsky.modelo.Catalogo;
import chomsky.modelo.Lenguaje;
import chomsky.modelo.Paso;
import chomsky.modelo.Veredicto;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Módulo del Nivel 3: dibuja el grafo del AFD, resalta el estado actual y la flecha usada,
 * y muestra la cadena con el símbolo que se está leyendo.
 */
public class PanelAFD extends PanelModulo {

    private AFD afd;
    private boolean parametrico;
    private final JSpinner spN = new JSpinner(new SpinnerNumberModel(3, 2, 8, 1));
    private final JLabel lblN = new JLabel("N =");

    public PanelAFD(Runnable alMenu) {
        super("Nivel 3 · Lenguajes regulares",
                "Autómata Finito Determinista (AFD): solo recuerda en qué estado está, no tiene memoria auxiliar",
                Tema.NIVEL[3], alMenu);
        lblN.setFont(Tema.NORMAL_B);
        agregarOpcion(lblN);
        agregarOpcion(spN);
        spN.addChangeListener(e -> {
            if (parametrico) {
                cargarMultiplos();
                reiniciar();
            }
        });
        inicializar();
    }

    @Override
    protected List<?> lenguajes() {
        return Catalogo.afds();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void seleccionar(Object lenguaje) {
        Lenguaje<AFD> l = (Lenguaje<AFD>) lenguaje;
        parametrico = l.nombre().equals(Catalogo.NOMBRE_MULTIPLOS);
        lblN.setVisible(parametrico);
        spN.setVisible(parametrico);
        if (parametrico) {
            cargarMultiplos();
        } else {
            afd = l.maquina();
            establecerDescripcion(l.descripcion());
            establecerEjemplos(l.ejemplos());
        }
    }

    /** Reconstruye el autómata con el N elegido en el selector (así el lenguaje es parametrizable). */
    private void cargarMultiplos() {
        Lenguaje<AFD> l = Catalogo.multiplos((Integer) spN.getValue());
        afd = l.maquina();
        establecerDescripcion(l.descripcion());
        establecerEjemplos(l.ejemplos());
    }

    @Override
    protected List<Paso> simular(String w) {
        return afd.simular(w);
    }

    @Override
    protected Lienzo crearLienzo() {
        return new LienzoAFD();
    }

    private class LienzoAFD extends Lienzo {

        private final int R = 26;

        @Override
        protected void pintar(Graphics2D g, int w, int h) {
            if (afd == null) return;
            int pos = paso == null ? 0 : paso.pos();
            int altoCadena = Dibujo.cadena(g, entrada, pos, Tema.ACTIVO, 20, 14, w - 40);

            // Estado actual
            g.setFont(Tema.NORMAL_B.deriveFont(15f));
            g.setColor(Tema.TEXTO);
            String actual = paso == null ? "—" : paso.estado();
            g.drawString("Estado actual: " + actual, 20, 14 + altoCadena + 22);

            // Posiciones de los estados sobre una elipse
            int top = 14 + altoCadena + 44, bottom = h - 34;
            double cx = w / 2.0, cy = (top + bottom) / 2.0;
            double rx = Math.min(240, (w - 2 * (R * 2 + 50)) / 2.0);
            double ry = Math.max(40, (bottom - top) / 2.0 - R * 2);
            List<String> estados = afd.estados();
            int n = estados.size();
            Map<String, Point2D> pos2d = new HashMap<>();
            for (int i = 0; i < n; i++) {
                double a = Math.PI + 2 * Math.PI * i / n;
                double x = n == 1 ? cx : cx + rx * Math.cos(a);
                double y = n == 1 ? cy : cy + ry * Math.sin(a);
                pos2d.put(estados.get(i), new Point2D.Double(x, y));
            }
            Point2D centro = new Point2D.Double(cx, cy);

            // Transiciones
            for (AFD.Arista ar : afd.aristas()) {
                boolean activa = paso != null && paso.veredicto() == Veredicto.EN_CURSO && paso.desde() != null
                        && paso.desde().equals(ar.desde()) && paso.estado().equals(ar.hacia());
                g.setStroke(new BasicStroke(activa ? 3.2f : 1.6f));
                Color c = activa ? Tema.ACTIVO.darker() : new Color(0x6B7280);
                g.setColor(c);
                Point2D a = pos2d.get(ar.desde()), b = pos2d.get(ar.hacia());
                if (ar.desde().equals(ar.hacia())) {
                    // El lazo del estado inicial va hacia arriba para no chocar con la flecha de entrada.
                    lazo(g, a, centro, ar.etiqueta(), c, ar.desde().equals(afd.inicial()));
                } else {
                    curva(g, a, b, ar.etiqueta(), c);
                }
            }

            // Flecha de entrada al estado inicial
            Point2D ini = pos2d.get(afd.inicial());
            double dx = ini.getX() - cx, dy = ini.getY() - cy;
            double l = Math.hypot(dx, dy);
            if (l < 1) { dx = -1; dy = 0; l = 1; }
            dx /= l;
            dy /= l;
            g.setStroke(new BasicStroke(2f));
            g.setColor(Tema.TEXTO);
            g.drawLine((int) (ini.getX() + dx * (R + 38)), (int) (ini.getY() + dy * (R + 38)),
                    (int) (ini.getX() + dx * (R + 2)), (int) (ini.getY() + dy * (R + 2)));
            Dibujo.flecha(g, ini.getX() + dx * R, ini.getY() + dy * R, -dx, -dy, 11);

            // Estados
            g.setFont(Tema.NORMAL_B.deriveFont(15f));
            for (String e : estados) {
                Point2D p = pos2d.get(e);
                boolean esActual = paso != null && paso.estado().equals(e);
                Color relleno = esActual ? colorVeredicto() : Color.WHITE;
                Ellipse2D circ = new Ellipse2D.Double(p.getX() - R, p.getY() - R, 2 * R, 2 * R);
                g.setColor(relleno);
                g.fill(circ);
                g.setStroke(new BasicStroke(esActual ? 3f : 2f));
                g.setColor(esActual ? relleno.darker().darker() : Tema.TEXTO);
                g.draw(circ);
                if (afd.finales().contains(e)) {
                    g.setStroke(new BasicStroke(1.6f));
                    g.draw(new Ellipse2D.Double(p.getX() - R + 5, p.getY() - R + 5, 2 * R - 10, 2 * R - 10));
                }
                g.setColor(Tema.TEXTO);
                Dibujo.centrado(g, e, p.getX(), p.getY());
            }

            // Leyenda
            g.setStroke(new BasicStroke(1f));
            g.setFont(Tema.NORMAL.deriveFont(12f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString("Círculo doble = estado de aceptación  ·  amarillo = estado actual  ·  "
                    + "flecha gruesa = transición recién usada  ·  flecha de entrada = estado inicial", 20, h - 12);
        }

        private Point2D punto(Point2D c, double ang, double r) {
            return new Point2D.Double(c.getX() + r * Math.cos(ang), c.getY() + r * Math.sin(ang));
        }

        /** Flecha curva entre dos estados distintos; la curva evita que se solapen las de ida y vuelta. */
        private void curva(Graphics2D g, Point2D a, Point2D b, String etiqueta, Color color) {
            double dx = b.getX() - a.getX(), dy = b.getY() - a.getY();
            double dist = Math.hypot(dx, dy);
            double nx = -dy / dist, ny = dx / dist;
            double off = Math.min(42, dist * 0.22);
            Point2D c = new Point2D.Double((a.getX() + b.getX()) / 2 + nx * off, (a.getY() + b.getY()) / 2 + ny * off);
            double sx = c.getX() - a.getX(), sy = c.getY() - a.getY(), sl = Math.hypot(sx, sy);
            double ex = c.getX() - b.getX(), ey = c.getY() - b.getY(), el = Math.hypot(ex, ey);
            Point2D ini = new Point2D.Double(a.getX() + sx / sl * R, a.getY() + sy / sl * R);
            Point2D fin = new Point2D.Double(b.getX() + ex / el * R, b.getY() + ey / el * R);
            g.draw(new QuadCurve2D.Double(ini.getX(), ini.getY(), c.getX(), c.getY(), fin.getX(), fin.getY()));
            Dibujo.flecha(g, fin.getX(), fin.getY(), fin.getX() - c.getX(), fin.getY() - c.getY(), 11);
            g.setFont(Tema.NORMAL_B);
            Dibujo.etiqueta(g, etiqueta, 0.25 * ini.getX() + 0.5 * c.getX() + 0.25 * fin.getX(),
                    0.25 * ini.getY() + 0.5 * c.getY() + 0.25 * fin.getY(), Color.WHITE, Tema.TEXTO);
            g.setColor(color);
        }

        /** Lazo de un estado hacia sí mismo, dibujado hacia afuera del grafo. */
        private void lazo(Graphics2D g, Point2D a, Point2D centro, String etiqueta, Color color, boolean haciaArriba) {
            double ox = a.getX() - centro.getX(), oy = a.getY() - centro.getY();
            if (Math.hypot(ox, oy) < 1) { ox = 0; oy = -1; }
            double ang = haciaArriba ? -Math.PI / 2 : Math.atan2(oy, ox);
            Point2D s = punto(a, ang - 0.55, R), e = punto(a, ang + 0.55, R);
            Point2D c1 = punto(a, ang - 0.5, R * 2.7), c2 = punto(a, ang + 0.5, R * 2.7);
            g.draw(new CubicCurve2D.Double(s.getX(), s.getY(), c1.getX(), c1.getY(), c2.getX(), c2.getY(),
                    e.getX(), e.getY()));
            Dibujo.flecha(g, e.getX(), e.getY(), e.getX() - c2.getX(), e.getY() - c2.getY(), 11);
            double mx = (s.getX() + 3 * c1.getX() + 3 * c2.getX() + e.getX()) / 8;
            double my = (s.getY() + 3 * c1.getY() + 3 * c2.getY() + e.getY()) / 8;
            g.setFont(Tema.NORMAL_B);
            Dibujo.etiqueta(g, etiqueta, mx + Math.cos(ang) * 4, my + Math.sin(ang) * 4, Color.WHITE, Tema.TEXTO);
            g.setColor(color);
        }
    }
}
