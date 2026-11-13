package com.verduleria.ui;

import com.verduleria.model.*;
import com.verduleria.service.InventarioService;
import com.verduleria.util.Sesion;
import com.verduleria.util.Validador;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Módulo de inventario rediseñado (CU-INV-01 existencias, CU-INV-02 movimientos, CU-INV-03 merma).
 * Indicadores visuales textuales y por color para: Existencia suficiente, Stock bajo y Sin existencias.
 */
public class InventarioPanel extends JPanel implements Recargable {
    private final InventarioService servicio = new InventarioService();

    // --- Existencias
    private final Ui.Modelo mExist = new Ui.Modelo(List.of("ID", "Producto", "Unidad", "Existencia", "Stock mínimo", "Estado"));
    private final JTable tExist = Ui.tabla(mExist);
    private final JTextField txtBuscar = Tema.campoTexto(16);
    private List<Producto> productos = new ArrayList<>();

    // --- Movimientos
    private final Ui.Modelo mMov = new Ui.Modelo(List.of("Fecha", "Producto", "Tipo", "Cantidad", "Usuario", "Motivo"));
    private final JTable tMov = Ui.tabla(mMov);
    private final JTextField txtDesde = Tema.campoTexto(9);
    private final JTextField txtHasta = Tema.campoTexto(9);

    public InventarioPanel() {
        super(new BorderLayout(0, 12));
        setBackground(Tema.CREMA);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        boolean puedeMerma = Sesion.tiene(Permiso.INVENTARIO_MERMA);

        CardLayout subCards = new CardLayout();
        JPanel contenedorVistas = new JPanel(subCards);
        contenedorVistas.setOpaque(false);
        contenedorVistas.add(panelExistencias(puedeMerma), "EXISTENCIAS");
        contenedorVistas.add(panelMovimientos(), "MOVIMIENTOS");

        PildorasSelector pildoras = new PildorasSelector(new String[]{"Existencias", "Movimientos"}, idx -> {
            subCards.show(contenedorVistas, idx == 0 ? "EXISTENCIAS" : "MOVIMIENTOS");
            if (idx == 1) cargarMovimientos();
        });

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cabecera.setOpaque(false);
        cabecera.add(pildoras);

        add(cabecera, BorderLayout.NORTH);
        add(contenedorVistas, BorderLayout.CENTER);

        configurarAtajos(puedeMerma);
    }

    private JPanel panelExistencias(boolean puedeMerma) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        // Barra de búsqueda y merma en tarjeta
        JPanel cardBarra = Tema.tarjeta();
        cardBarra.setLayout(new BorderLayout(10, 0));
        cardBarra.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);
        JLabel lblB = new JLabel("Buscar producto:");
        lblB.setFont(Tema.FUENTE_NEGRITA);
        lblB.setForeground(Tema.TEXTO_PRINCIPAL);
        izquierda.add(lblB);
        izquierda.add(txtBuscar);

        JButton btnBuscar = Tema.botonPrimario("Buscar (Enter)", Iconos.buscar(16, Color.WHITE), this::cargarExistencias);
        izquierda.add(btnBuscar);
        txtBuscar.addActionListener(e -> cargarExistencias());

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        if (puedeMerma) {
            JButton btnMerma = Tema.botonTerracota("Registrar merma (F2)", Iconos.alerta(16, Color.WHITE), this::registrarMerma);
            derecha.add(btnMerma);
        }

        cardBarra.add(izquierda, BorderLayout.WEST);
        cardBarra.add(derecha, BorderLayout.EAST);

        // Renderizador especializado para la columna Estado de Existencias
        configurarRendererEstadoInventario();
        JScrollPane scroll = Tema.scrollTabla(tExist);

        p.add(cardBarra, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void configurarRendererEstadoInventario() {
        int idx = tExist.getColumnModel().getColumnIndex("Estado");
        tExist.getColumnModel().getColumn(idx).setCellRenderer(new DefaultTableCellRenderer() {
            private final JLabel badge = new JLabel("", SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            {
                badge.setFont(Tema.FUENTE_PEQUE_BOLD);
                badge.setOpaque(false);
            }

            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                String val = value == null ? "" : value.toString().trim();
                badge.setText(val);
                if (val.equalsIgnoreCase("Suficiente")) {
                    badge.setBackground(Tema.BADGE_VERDE_FONDO);
                    badge.setForeground(Tema.BADGE_VERDE_TEXTO);
                } else if (val.equalsIgnoreCase("Stock bajo")) {
                    badge.setBackground(Tema.BADGE_AMARILLO_FONDO);
                    badge.setForeground(Tema.BADGE_AMARILLO_TEXTO);
                } else if (val.equalsIgnoreCase("Sin stock")) {
                    badge.setBackground(Tema.BADGE_ROJO_FONDO);
                    badge.setForeground(Tema.BADGE_ROJO_TEXTO);
                } else {
                    badge.setBackground(Tema.GRIS_SUAVE);
                    badge.setForeground(Tema.TEXTO_PRINCIPAL);
                }

                JPanel cont = new JPanel(new BorderLayout());
                cont.setOpaque(true);
                cont.setBackground(isSelected ? table.getSelectionBackground()
                        : (row % 2 == 0 ? Tema.BLANCO_TARJETA : new Color(0xFC, 0xFB, 0xF7)));
                cont.add(badge, BorderLayout.CENTER);
                return cont;
            }
        });
    }

    private JPanel panelMovimientos() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JPanel cardFiltros = Tema.tarjeta();
        cardFiltros.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));

        JLabel lblD = new JLabel("Desde:");
        lblD.setFont(Tema.FUENTE_NEGRITA);
        lblD.setForeground(Tema.TEXTO_PRINCIPAL);
        cardFiltros.add(lblD);
        cardFiltros.add(txtDesde);

        JLabel lblH = new JLabel("Hasta:");
        lblH.setFont(Tema.FUENTE_NEGRITA);
        lblH.setForeground(Tema.TEXTO_PRINCIPAL);
        cardFiltros.add(lblH);
        cardFiltros.add(txtHasta);

        JButton btnConsultar = Tema.botonPrimario("Consultar (F5)", Iconos.buscar(16, Color.WHITE), this::cargarMovimientos);
        cardFiltros.add(btnConsultar);

        Tema.aplicarBadge(tMov, "Tipo");
        JScrollPane scroll = Tema.scrollTabla(tMov);

        p.add(cardFiltros, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void configurarAtajos(boolean puedeMerma) {
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F3, "focoBuscarInv", () -> {
            txtBuscar.requestFocusInWindow();
            txtBuscar.selectAll();
        });
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F5, "recargarInv", this::recargar);
        if (puedeMerma) {
            Ui.atajo(this, java.awt.event.KeyEvent.VK_F2, "mermaF2", this::registrarMerma);
            Ui.atajo(this, java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_DOWN_MASK, "mermaAltM", this::registrarMerma);
        }
    }

    @Override public void recargar() {
        cargarExistencias();
        cargarMovimientos();
    }

    private void cargarExistencias() {
        Ui.intentar(this, () -> {
            productos = servicio.existencias(txtBuscar.getText());
            List<Object[]> filas = new ArrayList<>();
            for (Producto p : productos) {
                String estado;
                if (p.existencia().signum() <= 0) {
                    estado = "Sin stock";
                } else if (p.bajoStock()) {
                    estado = "Stock bajo";
                } else {
                    estado = "Suficiente";
                }
                filas.add(new Object[]{p.id(), p.nombre(), p.unidad(), p.existencia(), p.stockMinimo(), estado});
            }
            mExist.set(filas);
        });
    }

    private void cargarMovimientos() {
        Ui.intentar(this, () -> {
            Tabla t = servicio.movimientos(Validador.fechaOpcional(txtDesde.getText(), "Desde"),
                    Validador.fechaOpcional(txtHasta.getText(), "Hasta"));
            mMov.set(t.columnas(), t.filas());
        });
    }

    private void registrarMerma() {
        int i = Ui.seleccionada(tExist);
        if (i < 0) { Ui.aviso(this, "Seleccione en la tabla el producto afectado."); return; }
        Producto p = productos.get(i);
        JTextField cantidad = Tema.campoTexto(10);
        JTextField motivo = Tema.campoTexto(24);
        boolean ok = Ui.formulario(this, "Registrar merma",
                new String[]{"Producto:", "Existencia actual:", "Cantidad perdida (" + p.unidad() + "):", "Motivo:"},
                new JComponent[]{new JLabel(p.nombre()), new JLabel(Ui.cantidad(p.existencia())), cantidad, motivo},
                () -> servicio.registrarMerma(p, cantidad.getText(), motivo.getText()));
        if (ok) { Ui.info(this, "Merma registrada."); recargar(); }
    }
}
