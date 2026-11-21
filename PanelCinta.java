package chomsky.ui;

import chomsky.modelo.Lenguaje;
import chomsky.modelo.MaquinaTuring;
import chomsky.modelo.Paso;
import chomsky.modelo.Veredicto;

import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Módulo de los niveles 1 y 0. Los dos usan el mismo motor (MaquinaTuring) y el mismo dibujo de cinta;
 * la diferencia es solo el modo:
 *   acotada = true   Nivel 1 (autómata linealmente acotado): cinta fija "<w>" con delimitadores.
 *   acotada = false  Nivel 0 (máquina de Turing): cinta infinita, la vista se centra en el cabezal.
 */
public class PanelCinta extends PanelModulo {

    private final boolean acotada;
    private final List<Lenguaje<MaquinaTuring>> lista;
    private MaquinaTuring mt;

    public PanelCinta(boolean acotada, String titulo, String subtitulo, Color acento,
                      List<Lenguaje<MaquinaTuring>> lista, Runnable alMenu) {
        super(titulo, subtitulo, acento, alMenu);
        this.acotada = acotada;
        this.lista = lista;
        inicializar();
    }

    @Override
    protected List<?> lenguajes() {
        return lista;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void seleccionar(Object lenguaje) {
        Lenguaje<MaquinaTuring> l = (Lenguaje<MaquinaTuring>) lenguaje;
        mt = l.maquina();
        establecerDescripcion(l.descripcion());
        establecerEjemplos(l.ejemplos());
    }

    @Override
    protected List<Paso> simular(String w) {
        return mt.simular(w);
    }

    @Override
    protected String validar(String w) {
        for (char c : w.toCharArray()) {
            if (mt.sigma().indexOf(c) < 0) {
                return "El símbolo '" + c + "' no pertenece al alfabeto de entrada Σ = {" + unir(mt.sigma()) + "}";
            }
        }
        return null;
    }

    @Override
    protected String resumenFinal(Paso ultimo) {
        if (ultimo.veredicto() != Veredicto.ACEPTADA) return "";
        if (acotada) {
            return "memoria usada: " + ultimo.cinta().size() + " celdas = |w| + 2 (la cinta nunca creció)";
        }
        String res = MaquinaTuring.contenido(ultimo.cinta());
        return res.isEmpty() ? "" : "resultado en la cinta: " + res;
    }

    @Override
    protected Lienzo crearLienzo() {
        return new LienzoCinta();
    }

    private static String unir(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(c);
        }
        return sb.toString();
    }

    private class LienzoCinta extends Lienzo {

        private double cabAnim = 0;      // posición (fraccionaria) del cabezal mientras se anima
        private double cabObjetivo = 0;
        private final Timer animacion = new Timer(16, e -> {
            cabAnim += (cabObjetivo - cabAnim) * 0.3;
            if (Math.abs(cabObjetivo - cabAnim) < 0.01) {
                cabAnim = cabObjetivo;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        @Override
        public void mostrar(Paso p, String entrada) {
            Paso vieja = paso;
            super.mostrar(p, entrada);
            if (p == null) {
                cabAnim = cabObjetivo = 0;
                animacion.stop();
                return;
            }
            double desde = p.cabezal();
            if (vieja != null) {
                desde = vieja.cabezal();
                // Si la cinta infinita creció a la izquierda, todos los índices se corren una posición.
                if (!acotada && p.cinta().size() == vieja.cinta().size() + 1
                        && p.cabezal() == 0 && vieja.cabezal() == 0) {
                    desde = 1;
                }
            }
            cabObjetivo = p.cabezal();
            if (Math.abs(desde - cabObjetivo) > 1.5) desde = cabObjetivo;   // saltos grandes: sin animar
            cabAnim = desde;
            if (cabAnim != cabObjetivo) animacion.restart();
        }

        private List<Character> previa() {
            List<Character> r = new ArrayList<>();
            if (acotada) r.add(MaquinaTuring.IZQ);
            for (char c : entrada.toCharArray()) r.add(c);
            if (acotada) r.add(MaquinaTuring.DER);
            else if (r.isEmpty()) r.add(MaquinaTuring.BLANCO);
            return r;
        }

        @Override
        protected void pintar(Graphics2D g, int w, int h) {
            if (mt == null) return;
            List<Character> cinta = paso == null ? previa() : paso.cinta();
            int cabezal = paso == null ? 0 : paso.cabezal();
            int n = cinta.size();

            // ---- Encabezado: alfabetos y explicación del modo ----
            g.setFont(Tema.NORMAL_B);
            g.setColor(Tema.TEXTO);
            StringBuilder gamma = new StringBuilder();
            for (char c : mt.gamma()) {
                if (gamma.length() > 0) gamma.append(", ");
                gamma.append(c);
            }
            g.drawString("Σ (entrada) = {" + unir(mt.sigma()) + "}     Γ (cinta) = {" + gamma + "}", 20, 24);
            g.setFont(Tema.NORMAL.deriveFont(12f));
            g.setColor(Tema.TEXTO_SUAVE);
            String modo = acotada
                    ? "Cinta ACOTADA: la entrada va entre los delimitadores < y >. Hay exactamente |w| + 2 celdas "
                    + "y el cabezal no puede salirse ni borrar los delimitadores."
                    : "Cinta INFINITA en ambos sentidos: '_' es el símbolo blanco. Si el cabezal se sale de lo "
                    + "escrito, la cinta se extiende con un blanco nuevo.";
            int y = Dibujo.parrafo(g, modo, 20, 44, w - 40);

            // ---- Cinta ----
            double cw = acotada ? Math.max(22, Math.min(54, (w - 60.0) / n)) : 48;
            double ch = Math.min(cw, 48);
            double tapeY = y + 22;
            double x0 = (w - cw * n) / 2.0;                 // solo para la cinta acotada
            double cx0 = w / 2.0 - cw / 2.0;                 // posición de la celda bajo el cabezal (cinta infinita)

            int desdeI = acotada ? 0 : (int) Math.floor(cabAnim - (w / 2.0) / cw) - 1;
            int hastaI = acotada ? n - 1 : (int) Math.ceil(cabAnim + (w / 2.0) / cw) + 1;
            for (int i = desdeI; i <= hastaI; i++) {
                double x = acotada ? x0 + i * cw : cx0 + (i - cabAnim) * cw;
                boolean real = i >= 0 && i < n;
                char c = real ? cinta.get(i) : MaquinaTuring.BLANCO;
                boolean delim = acotada && real && (c == MaquinaTuring.IZQ || c == MaquinaTuring.DER);
                boolean bajoCabezal = real && i == cabezal && paso != null;
                celda(g, x, tapeY, cw, ch, c, real, delim, bajoCabezal);
                if (acotada) {
                    g.setFont(Tema.NORMAL.deriveFont(11f));
                    g.setColor(Tema.TEXTO_SUAVE);
                    Dibujo.centrado(g, String.valueOf(i), x + cw / 2, tapeY - 8);
                }
            }
            if (!acotada) {
                g.setFont(Tema.NORMAL_B);
                Dibujo.etiqueta(g, "◄ infinita", 44, tapeY + ch + 14, Tema.FONDO, Tema.TEXTO_SUAVE);
                Dibujo.etiqueta(g, "infinita ►", w - 44, tapeY + ch + 14, Tema.FONDO, Tema.TEXTO_SUAVE);
            }

            // ---- Cabezal de lectura/escritura y estado activo ----
            double hx = acotada ? x0 + cabAnim * cw + cw / 2 : w / 2.0;
            double hy = tapeY + ch + 3;
            Color c = paso == null ? Tema.TEXTO_SUAVE : colorVeredicto();
            g.setColor(Tema.TEXTO);
            Path2D tri = new Path2D.Double();
            tri.moveTo(hx, hy);
            tri.lineTo(hx - 9, hy + 14);
            tri.lineTo(hx + 9, hy + 14);
            tri.closePath();
            g.fill(tri);
            g.setColor(c);
            RoundRectangle2D pill = new RoundRectangle2D.Double(hx - 36, hy + 14, 72, 32, 14, 14);
            g.fill(pill);
            g.setColor(Tema.TEXTO);
            g.setStroke(new BasicStroke(2f));
            g.draw(pill);
            g.setFont(Tema.NORMAL_B.deriveFont(16f));
            Dibujo.centrado(g, paso == null ? "—" : paso.estado(), hx, hy + 30);
            g.setFont(Tema.NORMAL.deriveFont(11f));
            g.setColor(Tema.TEXTO_SUAVE);
            Dibujo.centrado(g, "cabezal lee/escribe · estado activo", hx, hy + 58);

            // ---- Descripción del paso y reglas ----
            int x = 20, ancho = w - 40;
            int yy = (int) (hy + 80);
            if (paso != null) {
                g.setFont(Tema.NORMAL_B);
                g.setColor(Tema.TEXTO);
                g.drawString("Lo que acaba de pasar:", x, yy);
                g.setFont(Tema.NORMAL);
                yy = Dibujo.parrafo(g, paso.descripcion(), x, yy + 20, ancho) + 8;
            }
            g.setFont(Tema.NORMAL_B);
            g.setColor(Tema.TEXTO);
            g.drawString("Reglas δ(estado, símbolo leído) = (nuevo estado, símbolo escrito, movimiento L/R/S)", x, yy);
            Dibujo.reglas(g, mt.reglas(), paso, x, yy + 20, ancho, h - yy - 28);
        }

        private void celda(Graphics2D g, double x, double y, double cw, double ch, char c,
                           boolean real, boolean delim, boolean activa) {
            RoundRectangle2D r = new RoundRectangle2D.Double(x + 1, y, cw - 2, ch, 8, 8);
            if (!real) {
                g.setColor(new Color(0xF9FAFB));
                g.fill(r);
                g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                        new float[]{4f, 4f}, 0f));
                g.setColor(new Color(0xD1D5DB));
                g.draw(r);
                g.setFont(Tema.MONO_B);
                g.setColor(new Color(0xD1D5DB));
                Dibujo.centrado(g, "_", x + cw / 2, y + ch / 2);
                return;
            }
            Color fondo = delim ? Tema.suave(Tema.NIVEL[1], 0.5) : Color.WHITE;
            if (activa) fondo = paso.veredicto() == Veredicto.EN_CURSO ? Tema.ACTIVO : colorVeredicto();
            g.setColor(fondo);
            g.fill(r);
            g.setStroke(new BasicStroke(activa ? 3f : delim ? 2.2f : 1.5f));
            g.setColor(activa ? Tema.TEXTO : delim ? Tema.NIVEL[1].darker() : Tema.TEXTO_SUAVE);
            g.draw(r);
            g.setFont(Tema.MONO_B.deriveFont((float) Math.min(22, ch * 0.55)));
            g.setColor(c == MaquinaTuring.BLANCO ? Tema.TEXTO_SUAVE : Tema.TEXTO);
            Dibujo.centrado(g, String.valueOf(c), x + cw / 2, y + ch / 2);
        }
    }
}
