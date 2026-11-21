package chomsky.ui;

import chomsky.modelo.Ejemplo;
import chomsky.modelo.Paso;
import chomsky.modelo.Veredicto;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.DefaultListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Esqueleto común de los cuatro módulos. Aquí vive todo lo que no cambia entre niveles:
 * selector de lenguaje, campo de cadena, botones Iniciar / Atrás / Paso / Auto, lista de ejemplos,
 * traza de ejecución y veredicto. Cada módulo solo aporta:
 *   - su lista de lenguajes y cómo cargarlos,
 *   - cómo simular una cadena (devuelve la lista de Paso),
 *   - su Lienzo (la parte gráfica: grafo, pila o cinta).
 *
 * Idea clave: el simulador calcula TODA la traza de golpe y esta clase solo se pasea por ella con un
 * índice. Por eso "Atrás", "Paso" y "Auto" son tan simples y nunca se desincronizan de la lógica.
 */
public abstract class PanelModulo extends JPanel {

    /** Zona de dibujo: cada módulo implementa su propia representación gráfica. */
    public abstract static class Lienzo extends JPanel {
        protected Paso paso;
        protected String entrada = "";

        protected Lienzo() {
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        /** Recibe el paso a dibujar (null = aún no se ha ejecutado nada). */
        public void mostrar(Paso p, String entrada) {
            this.paso = p;
            this.entrada = entrada == null ? "" : entrada;
            alCambiarPaso();
            repaint();
        }

        protected void alCambiarPaso() { }

        protected abstract void pintar(Graphics2D g, int ancho, int alto);

        /** Verde si aceptó, rojo si rechazó y ámbar mientras se ejecuta. */
        protected Color colorVeredicto() {
            if (paso == null || paso.veredicto() == Veredicto.EN_CURSO) return Tema.ACTIVO;
            return paso.veredicto() == Veredicto.ACEPTADA ? Tema.OK : Tema.ERROR;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Dibujo.suavizar(g);
            pintar(g, getWidth(), getHeight());
            g.dispose();
        }
    }

    private final JComboBox<Object> comboLenguajes = new JComboBox<>();
    private final JTextArea txtDescripcion = new JTextArea(3, 40);
    private final JPanel panelOpciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JTextField txtEntrada = new JTextField(20);
    private final DefaultListModel<Ejemplo> modeloEjemplos = new DefaultListModel<>();
    private final JList<Ejemplo> listaEjemplos = new JList<>(modeloEjemplos);
    private final JTextArea txtTraza = new JTextArea();
    private final JLabel lblVeredicto = new JLabel(" ");
    private final JButton btnIniciar = new JButton("Iniciar");
    private final JButton btnAtras = new JButton("< Atrás");
    private final JButton btnAdelante = new JButton("Paso >");
    private final JButton btnAuto = new JButton("Auto");
    private final JButton btnReiniciar = new JButton("Reiniciar");
    private final JSlider sldVelocidad = new JSlider(1, 10, 5);
    private final JPanel centro = new JPanel(new BorderLayout());
    private final Timer temporizador;

    protected Lienzo lienzo;
    private List<Paso> pasos = List.of();
    private int indice = -1;
    private String entradaActual = "";

    protected PanelModulo(String titulo, String subtitulo, Color acento, Runnable alMenu) {
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);

        // ---------- Cabecera: volver al menú, título, selector de lenguaje y descripción ----------
        JPanel cabecera = new JPanel(new BorderLayout(14, 8));
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Tema.BORDE), new EmptyBorder(10, 16, 10, 16)));

        JButton btnMenu = new JButton("< Menú principal");
        btnMenu.addActionListener(e -> {
            detener();
            alMenu.run();
        });
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.TITULO);
        lblTitulo.setForeground(acento);
        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setFont(Tema.NORMAL);
        lblSubtitulo.setForeground(Tema.TEXTO_SUAVE);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblTitulo);
        textos.add(lblSubtitulo);
        cabecera.add(btnMenu, BorderLayout.WEST);
        cabecera.add(textos, BorderLayout.CENTER);

        JPanel filaLenguaje = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaLenguaje.setOpaque(false);
        JLabel lblLenguaje = new JLabel("Lenguaje:");
        lblLenguaje.setFont(Tema.NORMAL_B);
        filaLenguaje.add(lblLenguaje);
        filaLenguaje.add(comboLenguajes);
        panelOpciones.setOpaque(false);
        filaLenguaje.add(panelOpciones);
        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setFont(Tema.NORMAL);
        txtDescripcion.setForeground(Tema.TEXTO);
        txtDescripcion.setBorder(new EmptyBorder(6, 2, 0, 2));
        JPanel configuracion = new JPanel(new BorderLayout());
        configuracion.setOpaque(false);
        configuracion.add(filaLenguaje, BorderLayout.NORTH);
        configuracion.add(txtDescripcion, BorderLayout.CENTER);
        cabecera.add(configuracion, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        // ---------- Barra de controles bajo el lienzo ----------
        txtEntrada.setFont(Tema.MONO_B);
        sldVelocidad.setPreferredSize(new Dimension(110, 24));
        JLabel lblCadena = new JLabel("Cadena:");
        lblCadena.setFont(Tema.NORMAL_B);
        JLabel lblVel = new JLabel("Velocidad:");
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        barra.setBackground(Color.WHITE);
        for (Component c : new Component[]{lblCadena, txtEntrada, btnIniciar, btnAtras, btnAdelante, btnAuto,
                btnReiniciar}) {
            barra.add(c);
        }
        txtEntrada.setColumns(14);
        lblVeredicto.setFont(Tema.NORMAL_B.deriveFont(15f));
        lblVeredicto.setBorder(new EmptyBorder(2, 14, 10, 14));
        // La velocidad va en la fila del veredicto para que la barra de botones no se desborde.
        JPanel velocidad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        velocidad.setOpaque(false);
        velocidad.setBorder(new EmptyBorder(0, 0, 8, 10));
        velocidad.add(lblVel);
        velocidad.add(sldVelocidad);
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setBackground(Color.WHITE);
        inferior.setBorder(new MatteBorder(1, 0, 0, 0, Tema.BORDE));
        inferior.add(barra, BorderLayout.NORTH);
        inferior.add(lblVeredicto, BorderLayout.CENTER);
        inferior.add(velocidad, BorderLayout.EAST);
        centro.add(inferior, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // ---------- Columna derecha: cadenas de prueba y traza ----------
        listaEjemplos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaEjemplos.setFont(Tema.MONO);
        listaEjemplos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int i, boolean sel, boolean foco) {
                Ejemplo e = (Ejemplo) valor;
                String texto = (e.cadena().isEmpty() ? "ε (vacía)" : e.cadena()) + "   →  " + (e.acepta() ? "ACEPTA" : "RECHAZA");
                JLabel l = (JLabel) super.getListCellRendererComponent(lista, texto, i, sel, foco);
                l.setToolTipText(e.nota());
                if (!sel) l.setForeground(e.acepta() ? Tema.OK.darker() : Tema.ERROR);
                return l;
            }
        });
        listaEjemplos.addListSelectionListener(e -> {
            Ejemplo ej = listaEjemplos.getSelectedValue();
            if (!e.getValueIsAdjusting() && ej != null) {
                txtEntrada.setText(ej.cadena());
                iniciar();
            }
        });
        JScrollPane spEjemplos = new JScrollPane(listaEjemplos);
        spEjemplos.setBorder(BorderFactory.createTitledBorder("Cadenas de prueba (clic para ejecutar)"));
        spEjemplos.setPreferredSize(new Dimension(340, 215));

        txtTraza.setEditable(false);
        txtTraza.setLineWrap(true);
        txtTraza.setWrapStyleWord(true);
        txtTraza.setFont(Tema.MONO.deriveFont(12f));
        JScrollPane spTraza = new JScrollPane(txtTraza);
        spTraza.setBorder(BorderFactory.createTitledBorder("Traza de ejecución"));

        JPanel derecha = new JPanel(new BorderLayout(0, 6));
        derecha.setOpaque(false);
        derecha.setBorder(new EmptyBorder(8, 0, 8, 8));
        derecha.setPreferredSize(new Dimension(350, 0));
        derecha.add(spEjemplos, BorderLayout.NORTH);
        derecha.add(spTraza, BorderLayout.CENTER);
        add(derecha, BorderLayout.EAST);

        // ---------- Comportamiento ----------
        temporizador = new Timer(600, e -> {
            if (indice < pasos.size() - 1) {
                indice++;
                refrescar();
            } else {
                detener();
            }
        });
        sldVelocidad.addChangeListener(e -> temporizador.setDelay(1150 - sldVelocidad.getValue() * 100));
        temporizador.setDelay(1150 - sldVelocidad.getValue() * 100);
        btnIniciar.addActionListener(e -> iniciar());
        txtEntrada.addActionListener(e -> iniciar());
        btnAtras.addActionListener(e -> {
            detener();
            if (indice > 0) {
                indice--;
                refrescar();
            }
        });
        btnAdelante.addActionListener(e -> {
            detener();
            if (indice < pasos.size() - 1) {
                indice++;
                refrescar();
            }
        });
        btnAuto.addActionListener(e -> {
            if (temporizador.isRunning()) {
                detener();
            } else if (!pasos.isEmpty()) {
                if (indice >= pasos.size() - 1) indice = 0;   // si ya terminó, vuelve a empezar
                btnAuto.setText("Pausa");
                refrescar();
                temporizador.start();
            }
        });
        btnReiniciar.addActionListener(e -> reiniciar());
    }

    // ================= Contrato que implementa cada módulo =================

    /** Lenguajes disponibles en el selector. */
    protected abstract List<?> lenguajes();

    /** Carga el lenguaje elegido: debe llamar a establecerDescripcion y establecerEjemplos. */
    protected abstract void seleccionar(Object lenguaje);

    /** Ejecuta la máquina sobre la cadena y devuelve la traza completa. */
    protected abstract List<Paso> simular(String w);

    protected abstract Lienzo crearLienzo();

    /** Devuelve un mensaje de error si la cadena no es válida para este módulo, o null. */
    protected String validar(String w) {
        return null;
    }

    /** Texto extra que se agrega al veredicto final (por ejemplo, el resultado en la cinta). */
    protected String resumenFinal(Paso ultimo) {
        return "";
    }

    // ================= Servicios para los módulos =================

    /** Debe llamarse al final del constructor de cada módulo, cuando sus campos ya existen. */
    protected final void inicializar() {
        lienzo = crearLienzo();
        centro.add(lienzo, BorderLayout.CENTER);
        for (Object l : lenguajes()) comboLenguajes.addItem(l);
        comboLenguajes.addActionListener(e -> cambiarLenguaje());
        cambiarLenguaje();
    }

    protected final void agregarOpcion(Component c) {
        panelOpciones.add(c);
    }

    protected final void establecerDescripcion(String texto) {
        txtDescripcion.setText(texto);
        txtDescripcion.setCaretPosition(0);
    }

    protected final void establecerEjemplos(List<Ejemplo> ejemplos) {
        listaEjemplos.clearSelection();
        modeloEjemplos.clear();
        for (Ejemplo e : ejemplos) modeloEjemplos.addElement(e);
    }

    /** Vuelve al estado inicial: sin ejecución, conservando la cadena escrita. */
    public final void reiniciar() {
        detener();
        pasos = List.of();
        indice = -1;
        entradaActual = txtEntrada.getText();
        txtTraza.setText("");
        mostrarMensaje("Escribe una cadena (o elige una de prueba) y pulsa Iniciar.", Tema.TEXTO_SUAVE);
        if (lienzo != null) lienzo.mostrar(null, entradaActual);
        actualizarBotones();
    }

    // ================= Internos =================

    private void cambiarLenguaje() {
        detener();
        Object elegido = comboLenguajes.getSelectedItem();
        if (elegido == null) return;
        seleccionar(elegido);
        reiniciar();
    }

    private void iniciar() {
        detener();
        String w = txtEntrada.getText().trim();
        if (w.equals("ε")) w = "";
        txtEntrada.setText(w);
        String error = validar(w);
        if (error != null) {
            pasos = List.of();
            indice = -1;
            txtTraza.setText("");
            lienzo.mostrar(null, w);
            mostrarMensaje(error, Tema.ERROR);
            actualizarBotones();
            return;
        }
        pasos = simular(w);
        entradaActual = w;
        indice = 0;
        refrescar();
    }

    private void detener() {
        temporizador.stop();
        btnAuto.setText("Auto");
    }

    private void refrescar() {
        Paso p = pasos.get(indice);
        lienzo.mostrar(p, entradaActual);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= indice; i++) {
            sb.append(i == indice ? ">> " : "   ").append(i).append(". ").append(pasos.get(i).descripcion()).append('\n');
        }
        txtTraza.setText(sb.toString());
        txtTraza.setCaretPosition(txtTraza.getDocument().getLength());

        if (p.veredicto() == Veredicto.EN_CURSO) {
            mostrarMensaje("Paso " + indice + " de " + (pasos.size() - 1) + "  ·  estado actual: " + p.estado(), Tema.TEXTO);
        } else {
            String extra = resumenFinal(p);
            boolean ok = p.veredicto() == Veredicto.ACEPTADA;
            mostrarMensaje((ok ? "CADENA ACEPTADA" : "CADENA RECHAZADA") + (extra.isEmpty() ? "" : "   ·   " + extra),
                    ok ? Tema.OK.darker() : Tema.ERROR);
            if (temporizador.isRunning()) detener();
        }
        actualizarBotones();
    }

    private void mostrarMensaje(String texto, Color color) {
        lblVeredicto.setText(texto);
        lblVeredicto.setForeground(color);
    }

    private void actualizarBotones() {
        boolean hay = !pasos.isEmpty();
        btnAtras.setEnabled(hay && indice > 0);
        btnAdelante.setEnabled(hay && indice < pasos.size() - 1);
        btnAuto.setEnabled(hay);
    }

    /** Copia defensiva de la lista de ejemplos actual (útil para pruebas y capturas). */
    public List<Ejemplo> ejemplosActuales() {
        List<Ejemplo> r = new ArrayList<>();
        for (int i = 0; i < modeloEjemplos.size(); i++) r.add(modeloEjemplos.get(i));
        return r;
    }

    /** Escribe una cadena y la ejecuta, como si el usuario la hubiera tecleado y pulsado Iniciar. */
    public void ejecutarCadena(String w) {
        txtEntrada.setText(w);
        iniciar();
    }

    /** Elige el lenguaje número i del selector (para pruebas y capturas de pantalla). */
    public void elegirLenguaje(int i) {
        comboLenguajes.setSelectedIndex(i);
    }

    /** Salta directamente al paso indicado (para capturas de pantalla). */
    public void irAlPaso(int i) {
        if (pasos.isEmpty()) return;
        indice = Math.max(0, Math.min(i, pasos.size() - 1));
        refrescar();
    }

    public int totalPasos() {
        return pasos.size();
    }
}
