package chomsky;

import chomsky.modelo.Catalogo;
import chomsky.ui.MenuPrincipal;
import chomsky.ui.PanelAFD;
import chomsky.ui.PanelAP;
import chomsky.ui.PanelCinta;
import chomsky.ui.Tema;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * Punto de entrada. Una sola ventana con un CardLayout: una "carta" para el menú principal
 * y una por cada módulo. Cambiar de módulo es solo mostrar otra carta.
 */
public class Main {

    private static final String MENU = "menu";

    public static JFrame crearVentana() {
        JFrame ventana = new JFrame("Jerarquía de Chomsky · Simulador");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cartas = new CardLayout();
        JPanel raiz = new JPanel(cartas);
        Runnable alMenu = () -> cartas.show(raiz, MENU);

        raiz.add(new MenuPrincipal(nivel -> cartas.show(raiz, "nivel" + nivel)), MENU);
        raiz.add(new PanelAFD(alMenu), "nivel3");
        raiz.add(new PanelAP(alMenu), "nivel2");
        raiz.add(new PanelCinta(true, "Nivel 1 · Lenguajes sensibles al contexto",
                "Autómata Linealmente Acotado (LBA): una Máquina de Turing cuya cinta mide solo |w| + 2 celdas",
                Tema.NIVEL[1], Catalogo.alas(), alMenu), "nivel1");
        raiz.add(new PanelCinta(false, "Nivel 0 · Lenguajes recursivamente enumerables",
                "Máquina de Turing (MT): cinta infinita, cabezal que lee, escribe y se mueve",
                Tema.NIVEL[0], Catalogo.turings(), alMenu), "nivel0");

        ventana.setContentPane(raiz);
        ventana.setMinimumSize(new Dimension(1000, 680));
        ventana.setSize(1180, 760);
        ventana.setLocationRelativeTo(null);
        return ventana;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // si falla, se usa el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> crearVentana().setVisible(true));
    }
}
