package chomsky.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel principal: muestra los cuatro niveles de la jerarquía como tarjetas.
 * Se dibuja como una pirámide de contención: Tipo 3 ⊂ Tipo 2 ⊂ Tipo 1 ⊂ Tipo 0.
 */
public class MenuPrincipal extends JPanel {

    public interface Alseleccionar {
        void abrir(int nivel);
    }

    private static final String[][] TARJETAS = {
            // nivel, nombre, máquina, gramática, memoria
            {"3", "Lenguajes regulares", "Autómata Finito (AFD)",
                    "Gramática regular", "Sin memoria: solo el estado actual",
                    "Cantidad par de 1s · terminan en \"ab\" · múltiplos de N en binario (N parametrizable)"},
            {"2", "Lenguajes libres de contexto", "Autómata de Pila (AP)",
                    "Gramática libre de contexto", "Pila LIFO ilimitada",
                    "aⁿbⁿ · paréntesis y corchetes balanceados · w c wᴿ (palíndromo con marca)"},
            {"1", "Lenguajes sensibles al contexto", "Autómata Linealmente Acotado (LBA)",
                    "Gramática sensible al contexto", "Cinta acotada a |w| + 2 celdas",
                    "aⁿbⁿcⁿ (no es libre de contexto, pero cabe en una cinta acotada)"},
            {"0", "Lenguajes recursivamente enumerables", "Máquina de Turing (MT)",
                    "Gramática sin restricciones", "Cinta infinita",
                    "Palíndromos · suma unaria · incremento binario (la cinta crece sola)"}
    };

    public MenuPrincipal(Alseleccionar alElegir) {
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);

        JLabel titulo = new JLabel("Jerarquía de Chomsky", SwingConstants.CENTER);
        titulo.setFont(Tema.TITULO.deriveFont(30f));
        titulo.setForeground(Tema.TEXTO);
        JLabel sub = new JLabel("Simulador interactivo de los cuatro modelos computacionales · elige un nivel",
                SwingConstants.CENTER);
        sub.setFont(Tema.NORMAL.deriveFont(15f));
        sub.setForeground(Tema.TEXTO_SUAVE);
        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBorder(BorderFactory.createEmptyBorder(26, 20, 10, 20));
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        cabecera.add(titulo);
        cabecera.add(javax.swing.Box.createVerticalStrut(6));
        cabecera.add(sub);
        add(cabecera, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(2, 2, 18, 18));
        rejilla.setOpaque(false);
        rejilla.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));
        // Orden de lectura: Tipo 3, Tipo 2 arriba; Tipo 1, Tipo 0 abajo (de menos a más poderoso).
        for (String[] t : TARJETAS) {
            rejilla.add(new Tarjeta(t, alElegir));
        }
        add(rejilla, BorderLayout.CENTER);

        JLabel pie = new JLabel("Cada nivel contiene al anterior: Tipo 3 ⊂ Tipo 2 ⊂ Tipo 1 ⊂ Tipo 0. "
                + "Más poder expresivo = más memoria disponible.", SwingConstants.CENTER);
        pie.setFont(Tema.NORMAL);
        pie.setForeground(Tema.TEXTO_SUAVE);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 10, 18, 10));
        add(pie, BorderLayout.SOUTH);
    }

    /** Tarjeta clicable de un nivel. */
    private static class Tarjeta extends JPanel {
        private final int nivel;
        private final String[] datos;
        private boolean encima = false;

        Tarjeta(String[] datos, Alseleccionar alElegir) {
            this.datos = datos;
            this.nivel = Integer.parseInt(datos[0]);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(300, 150));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alElegir.abrir(nivel);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Dibujo.suavizar(g);
            Color acento = Tema.NIVEL[nivel];
            int w = getWidth(), h = getHeight();
            RoundRectangle2D caja = new RoundRectangle2D.Double(2, 2, w - 5, h - 5, 20, 20);
            g.setColor(encima ? Tema.suave(acento, 0.9) : Color.WHITE);
            g.fill(caja);
            g.setColor(encima ? acento : Tema.BORDE);
            g.setStroke(new BasicStroke(encima ? 3f : 1.5f));
            g.draw(caja);

            // Insignia con el número del nivel
            g.setColor(acento);
            g.fill(new RoundRectangle2D.Double(18, 18, 62, 62, 16, 16));
            g.setColor(Color.WHITE);
            g.setFont(Tema.TITULO.deriveFont(11f));
            Dibujo.centrado(g, "TIPO", 49, 34);
            g.setFont(Tema.TITULO.deriveFont(30f));
            Dibujo.centrado(g, String.valueOf(nivel), 49, 58);

            g.setColor(Tema.TEXTO);
            g.setFont(Tema.NORMAL_B.deriveFont(17f));
            g.drawString("Nivel " + nivel, 96, 38);
            g.setFont(Tema.NORMAL.deriveFont(13f));
            g.setColor(Tema.TEXTO_SUAVE);
            Dibujo.parrafo(g, datos[1], 96, 58, w - 112);

            g.setColor(acento.darker());
            g.setFont(Tema.NORMAL_B.deriveFont(14f));
            g.drawString(datos[2], 20, 108);
            g.setFont(Tema.NORMAL.deriveFont(12f));
            g.setColor(Tema.TEXTO_SUAVE);
            g.drawString(datos[3] + "  ·  " + datos[4], 20, 128);
            g.setColor(Tema.BORDE);
            g.drawLine(20, 144, w - 20, 144);
            g.setColor(Tema.TEXTO);
            g.setFont(Tema.NORMAL_B.deriveFont(12f));
            g.drawString("Ejemplos en este módulo:", 20, 164);
            g.setFont(Tema.NORMAL.deriveFont(13f));
            g.setColor(Tema.TEXTO_SUAVE);
            Dibujo.parrafo(g, datos[5], 20, 184, w - 40);
            g.setFont(Tema.NORMAL_B.deriveFont(12f));
            g.setColor(acento);
            g.drawString("Clic para abrir  ›", 20, h - 16);
            g.dispose();
        }
    }
}
