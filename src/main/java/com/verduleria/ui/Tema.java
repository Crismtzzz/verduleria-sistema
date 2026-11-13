package com.verduleria.ui;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Paleta de colores, tipografías y componentes visuales unificados para Verdulería Jiménez.
 * Colores oficiales:
 * - Verde oscuro:  #184D36 (Menú lateral, encabezados, botones principales)
 * - Verde salvia:  #83A96B (Selección de módulos, insignias activas, acentos)
 * - Crema:         #F7F5EF (Fondo general de las pantallas)
 * - Terracota:     #C47D51 (Detalles, alertas de stock, acentos cálidos)
 * - Gris suave:    #E6E9E1 (Bordes, separadores, fondos secundarios)
 */
public final class Tema {

    // --- Colores Principales ---
    public static final Color VERDE_OSCURO       = new Color(0x18, 0x4D, 0x36); // #184D36
    public static final Color VERDE_OSCURO_HOVER = new Color(0x1F, 0x5E, 0x42);
    public static final Color VERDE_SALVIA       = new Color(0x83, 0xA9, 0x6B); // #83A96B
    public static final Color VERDE_SALVIA_CLARO = new Color(0xE8, 0xF2, 0xE4);
    public static final Color CREMA              = new Color(0xF7, 0xF5, 0xEF); // #F7F5EF
    public static final Color BLANCO_TARJETA     = new Color(0xFF, 0xFF, 0xFF);
    public static final Color TERRACOTA          = new Color(0xC4, 0x7D, 0x51); // #C47D51
    public static final Color TERRACOTA_CLARO    = new Color(0xFA, 0xEB, 0xE3);
    public static final Color GRIS_SUAVE         = new Color(0xE6, 0xE9, 0xE1); // #E6E9E1
    public static final Color GRIS_BORDE         = new Color(0xD8, 0xDC, 0xD3);

    // --- Colores de Texto ---
    public static final Color TEXTO_TITULO       = new Color(0x14, 0x2E, 0x22);
    public static final Color TEXTO_PRINCIPAL    = new Color(0x22, 0x2B, 0x25);
    public static final Color TEXTO_SECUNDARIO   = new Color(0x6A, 0x7A, 0x70);
    public static final Color TEXTO_BLANCO       = Color.WHITE;
    public static final Color TEXTO_MENU         = new Color(0xD5, 0xE4, 0xD8);

    // --- Insignias / Estados ---
    public static final Color BADGE_VERDE_FONDO   = new Color(0xDC, 0xEE, 0xD8);
    public static final Color BADGE_VERDE_TEXTO   = new Color(0x1B, 0x5E, 0x20);
    public static final Color BADGE_ROJO_FONDO    = new Color(0xFA, 0xEB, 0xE3);
    public static final Color BADGE_ROJO_TEXTO    = new Color(0xB8, 0x4A, 0x2F);
    public static final Color BADGE_AMARILLO_FONDO = new Color(0xFE, 0xF4, 0xDE);
    public static final Color BADGE_AMARILLO_TEXTO = new Color(0x9E, 0x60, 0x05);

    public static final Color ROJO_BADGE_FONDO    = BADGE_ROJO_FONDO;
    public static final Color ROJO_BADGE_TEXTO    = BADGE_ROJO_TEXTO;
    public static final Color VERDE_BADGE_FONDO   = BADGE_VERDE_FONDO;
    public static final Color VERDE_BADGE_TEXTO   = BADGE_VERDE_TEXTO;

    // --- Tipografías ---
    public static final Font FUENTE_TITULO_GRANDE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FUENTE_TITULO        = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FUENTE_SUBTITULO     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_NORMAL        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_PEQUE         = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FUENTE_PEQUE_BOLD    = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FUENTE_KPI_VALOR     = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FUENTE_KPI_LABEL     = new Font("Segoe UI", Font.BOLD, 12);

    private Tema() { }

    /** Inicializa FlatLaf con personalizaciones del tema Verdulería Jiménez. */
    public static void inicializar() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Panel.background", CREMA);
            UIManager.put("OptionPane.background", CREMA);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.trackArc", 8);
            UIManager.put("Table.selectionBackground", VERDE_SALVIA_CLARO);
            UIManager.put("Table.selectionForeground", TEXTO_PRINCIPAL);
            UIManager.put("Table.gridColor", GRIS_SUAVE);
            UIManager.put("Table.alternateRowColor", new Color(0xFC, 0xFB, 0xF7));
            UIManager.put("TableHeader.background", new Color(0xEE, 0xF2, 0xEB));
            UIManager.put("TableHeader.foreground", TEXTO_TITULO);
            UIManager.put("TableHeader.font", FUENTE_NEGRITA);
            UIManager.put("PopupMenu.background", BLANCO_TARJETA);
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
        }
    }

    /** Crea un panel con fondo blanco/crema redondeado y borde suave (estilo tarjeta). */
    public static JPanel tarjeta() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO_TARJETA);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(GRIS_BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        return p;
    }

    /** Botón primario: fondo verde oscuro #184D36 y texto blanco con esquinas redondeadas. */
    public static JButton botonPrimario(String texto, Icon icono, Runnable accion) {
        JButton b = new JButton(texto, icono) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? VERDE_OSCURO.darker()
                        : getModel().isRollover() ? VERDE_OSCURO_HOVER : VERDE_OSCURO;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(TEXTO_BLANCO);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.addActionListener(e -> accion.run());
        return b;
    }

    /** Botón secundario: fondo crema/blanco suave con borde gris y texto oscuro. */
    public static JButton botonSecundario(String texto, Icon icono, Runnable accion) {
        JButton b = new JButton(texto, icono) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? GRIS_SUAVE.darker()
                        : getModel().isRollover() ? GRIS_SUAVE : BLANCO_TARJETA;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(GRIS_BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(TEXTO_PRINCIPAL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.addActionListener(e -> accion.run());
        return b;
    }

    /** Botón terracota: fondo suave o acentuado para acciones de atención o mermas. */
    public static JButton botonTerracota(String texto, Icon icono, Runnable accion) {
        JButton b = new JButton(texto, icono) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? TERRACOTA.darker()
                        : getModel().isRollover() ? new Color(0xD6, 0x8E, 0x62) : TERRACOTA;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(TEXTO_BLANCO);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.addActionListener(e -> accion.run());
        return b;
    }

    /** Campo de texto moderno con bordes suaves y fondo blanco. */
    public static JTextField campoTexto(int columnas) {
        JTextField tf = new JTextField(columnas);
        tf.setFont(FUENTE_NORMAL);
        tf.setForeground(TEXTO_PRINCIPAL);
        tf.setBackground(BLANCO_TARJETA);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    /** Campo de contraseña moderno con bordes suaves y fondo blanco. */
    public static JPasswordField campoPassword(int columnas) {
        JPasswordField pf = new JPasswordField(columnas);
        pf.setFont(FUENTE_NORMAL);
        pf.setForeground(TEXTO_PRINCIPAL);
        pf.setBackground(BLANCO_TARJETA);
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    /** Insignia redondeada (Pill Badge) para mostrar estados visuales (Activo, Bajo, Sí, No). */
    public static JLabel badge(String texto, Color fondo, Color colorTexto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fondo);
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(FUENTE_PEQUE_BOLD);
        l.setForeground(colorTexto);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }

    /** Renderizador de celdas para columnas con insignias redondeadas ("Sí", "No", "Activo", "Bajo"). */
    public static class BadgeTableCellRenderer extends DefaultTableCellRenderer {
        private final JLabel badgeLabel = new JLabel("", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        public BadgeTableCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            badgeLabel.setFont(FUENTE_PEQUE_BOLD);
            badgeLabel.setOpaque(false);
        }

        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String val = value == null ? "" : value.toString().trim();
            badgeLabel.setText(val);
            if (val.equalsIgnoreCase("Sí") || val.equalsIgnoreCase("Activo") || val.equalsIgnoreCase("OK")
                    || val.equalsIgnoreCase("Habilitado") || val.equalsIgnoreCase("CONFIRMADA") || val.equalsIgnoreCase("PAGADO")) {
                badgeLabel.setBackground(BADGE_VERDE_FONDO);
                badgeLabel.setForeground(BADGE_VERDE_TEXTO);
            } else if (val.equalsIgnoreCase("No") || val.equalsIgnoreCase("Inactivo") || val.equalsIgnoreCase("Deshabilitado")
                    || val.equalsIgnoreCase("BAJO") || val.equalsIgnoreCase("Bajo") || val.equalsIgnoreCase("CANCELADA")) {
                badgeLabel.setBackground(BADGE_ROJO_FONDO);
                badgeLabel.setForeground(BADGE_ROJO_TEXTO);
            } else {
                badgeLabel.setBackground(GRIS_SUAVE);
                badgeLabel.setForeground(TEXTO_PRINCIPAL);
            }

            JPanel cont = new JPanel(new BorderLayout());
            cont.setOpaque(true);
            if (isSelected) {
                cont.setBackground(table.getSelectionBackground());
            } else {
                cont.setBackground(row % 2 == 0 ? BLANCO_TARJETA : new Color(0xFC, 0xFB, 0xF7));
            }
            cont.add(badgeLabel, BorderLayout.CENTER);
            return cont;
        }
    }

    /** Configura una JTable con el diseño moderno de Verdulería Jiménez. */
    public static void configurarTablaModerna(JTable tabla) {
        tabla.setFont(FUENTE_NORMAL);
        tabla.setRowHeight(32);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(GRIS_SUAVE);
        tabla.setBackground(BLANCO_TARJETA);
        tabla.setForeground(TEXTO_PRINCIPAL);
        tabla.setSelectionBackground(VERDE_SALVIA_CLARO);
        tabla.setSelectionForeground(TEXTO_PRINCIPAL);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader h = tabla.getTableHeader();
        h.setFont(FUENTE_NEGRITA);
        h.setBackground(new Color(0xEE, 0xF2, 0xEB));
        h.setForeground(TEXTO_TITULO);
        h.setPreferredSize(new Dimension(h.getWidth(), 34));
        h.setReorderingAllowed(false);
    }

    /** Aplica el BadgeTableCellRenderer a una columna específica por nombre. */
    public static void aplicarBadge(JTable tabla, String nombreColumna) {
        try {
            int idx = tabla.getColumnModel().getColumnIndex(nombreColumna);
            tabla.getColumnModel().getColumn(idx).setCellRenderer(new BadgeTableCellRenderer());
        } catch (IllegalArgumentException ignored) { }
    }

    /** Envuelve una tabla en un JScrollPane con estilo moderno y tarjeta contenedora. */
    public static JScrollPane scrollTabla(JTable tabla) {
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(new LineBorder(GRIS_BORDE, 1, true));
        sp.getViewport().setBackground(BLANCO_TARJETA);
        sp.setBackground(BLANCO_TARJETA);
        return sp;
    }
}
