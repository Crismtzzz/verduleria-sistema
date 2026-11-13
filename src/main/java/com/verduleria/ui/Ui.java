package com.verduleria.ui;

import com.verduleria.util.DatosException;
import com.verduleria.util.NegocioException;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/** Utilidades compartidas de la capa de presentación. */
public final class Ui {
    public static final Color VERDE = new Color(0x2E7D32);
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private Ui() { }

    public static String dinero(BigDecimal d) { return d == null ? "" : MONEDA.format(d); }

    /** Cantidades sin ceros sobrantes (12.500 → 12.5). */
    public static String cantidad(BigDecimal d) {
        return d == null ? "" : d.stripTrailingZeros().toPlainString();
    }

    public static void info(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component p, String msg) {
        return JOptionPane.showConfirmDialog(p, msg, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Ejecuta una acción mostrando al usuario los errores de validación o de base de datos. */
    public static boolean intentar(Component parent, Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (NegocioException e) {
            aviso(parent, e.getMessage());
        } catch (DatosException e) {
            error(parent, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            error(parent, "Ocurrió un error inesperado: " + e);
        }
        return false;
    }

    /**
     * Muestra un formulario modal con diseño moderno (estilo Verdulería Jiménez).
     * Si la validación falla, conserva los datos capturados y alerta al usuario.
     * @return true si el usuario confirmó y la acción terminó bien.
     */
    public static boolean formulario(Component parent, String titulo, String[] etiquetas, JComponent[] campos,
                                     Runnable alAceptar) {
        Window win = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(win, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Tema.CREMA);
        content.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Encabezado: Hoja verde + Título
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        head.setOpaque(false);
        head.add(new JLabel(Iconos.hoja(22, Tema.VERDE_OSCURO)));
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(Tema.FUENTE_TITULO);
        lblTit.setForeground(Tema.TEXTO_TITULO);
        head.add(lblTit);

        // Contenedor tipo tarjeta
        JPanel card = Tema.tarjeta();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < etiquetas.length; i++) {
            g.gridx = 0; g.gridy = i; g.fill = GridBagConstraints.NONE; g.weightx = 0;
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(Tema.FUENTE_NEGRITA);
            lbl.setForeground(Tema.TEXTO_PRINCIPAL);
            card.add(lbl, g);

            g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            card.add(campos[i], g);
        }

        boolean[] resultado = new boolean[]{false};
        JButton btnAceptar = Tema.botonPrimario("Guardar", Iconos.confirmar(16, Color.WHITE), () -> {
            try {
                alAceptar.run();
                resultado[0] = true;
                dlg.dispose();
            } catch (NegocioException e) {
                aviso(dlg, e.getMessage());
            } catch (DatosException e) {
                error(dlg, e.getMessage());
            }
        });

        JButton btnCancelar = Tema.botonSecundario("Cancelar", Iconos.cancelar(16, Tema.TEXTO_PRINCIPAL), dlg::dispose);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        foot.setOpaque(false);
        foot.add(btnAceptar);
        foot.add(btnCancelar);

        dlg.getRootPane().setDefaultButton(btnAceptar);
        atajo(dlg.getRootPane(), java.awt.event.KeyEvent.VK_ESCAPE, "cerrarForm", dlg::dispose);

        content.add(head, BorderLayout.NORTH);
        content.add(card, BorderLayout.CENTER);
        content.add(foot, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(480, dlg.getHeight()));
        dlg.setLocationRelativeTo(win);
        dlg.setVisible(true);

        return resultado[0];
    }

    public static JTextField campo(int columnas) { return Tema.campoTexto(columnas); }

    public static JButton boton(String texto, Runnable accion) {
        return Tema.botonSecundario(texto, null, accion);
    }

    public static JButton boton(String texto, int mnemotecnico, Runnable accion) {
        JButton b = boton(texto, accion);
        if (mnemotecnico > 0) b.setMnemonic(mnemotecnico);
        return b;
    }

    /** Asocia un atajo de teclado global a un componente (se activa mientras la ventana tenga foco). */
    public static void atajo(JComponent comp, int keyCode, int modifiers, String nombre, Runnable accion) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyCode, modifiers);
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, nombre);
        comp.getActionMap().put(nombre, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { accion.run(); }
        });
    }

    public static void atajo(JComponent comp, int keyCode, String nombre, Runnable accion) {
        atajo(comp, keyCode, 0, nombre, accion);
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 15f));
        return l;
    }

    /** Modelo de tabla de solo lectura. */
    public static class Modelo extends AbstractTableModel {
        private List<String> columnas;
        private List<Object[]> filas = new ArrayList<>();

        public Modelo(List<String> columnas) { this.columnas = columnas; }

        public void set(List<String> columnas, List<Object[]> filas) {
            this.columnas = columnas;
            this.filas = filas;
            fireTableStructureChanged();
        }

        public void set(List<Object[]> filas) {
            this.filas = filas;
            fireTableDataChanged();
        }

        public Object[] fila(int i) { return filas.get(i); }
        @Override public int getRowCount() { return filas.size(); }
        @Override public int getColumnCount() { return columnas.size(); }
        @Override public String getColumnName(int c) { return columnas.get(c); }
        @Override public Object getValueAt(int r, int c) { return filas.get(r)[c]; }
        @Override public Class<?> getColumnClass(int c) {
            for (Object[] f : filas) if (f[c] != null) return f[c].getClass();
            return Object.class;
        }
    }

    /** Muestra BigDecimal alineado a la derecha; cantidades (3 decimales) sin ceros sobrantes. */
    private static final class NumeroRenderer extends DefaultTableCellRenderer {
        NumeroRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
        @Override protected void setValue(Object v) {
            if (v instanceof BigDecimal d) setText(d.scale() == 3 ? cantidad(d) : d.toPlainString());
            else super.setValue(v);
        }
    }

    public static JTable tabla(Modelo modelo) {
        JTable t = new JTable(modelo);
        configurar(t);
        return t;
    }

    public static void configurar(JTable t) {
        Tema.configurarTablaModerna(t);
        t.setDefaultRenderer(BigDecimal.class, new NumeroRenderer());
    }

    /** Índice de modelo de la fila seleccionada, o -1. */
    public static int seleccionada(JTable t) {
        int v = t.getSelectedRow();
        return v < 0 ? -1 : t.convertRowIndexToModel(v);
    }

    public static JPanel barra(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        for (Component c : comps) p.add(c);
        return p;
    }

    public static JPanel margen(JComponent c, int m) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(m, m, m, m));
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
