package com.verduleria.ui;

import com.verduleria.model.Cliente;
import com.verduleria.model.Producto;
import com.verduleria.model.Tabla;
import com.verduleria.model.Venta;
import com.verduleria.service.ClienteService;
import com.verduleria.service.InventarioService;
import com.verduleria.service.ProductoService;
import com.verduleria.service.ReporteService;
import com.verduleria.service.VentaService;
import com.verduleria.util.Sesion;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pantalla principal (Dashboard) de Verdulería Jiménez.
 * Muestra tarjetas con métricas reales obtenidas de la base de datos (ventas de hoy,
 * productos activos, stock bajo y saldo pendiente), además de los movimientos recientes.
 */
public class InicioPanel extends JPanel implements Recargable {

    private final VentaService ventasService = new VentaService();
    private final ProductoService productoService = new ProductoService();
    private final ClienteService clienteService = new ClienteService();
    private final InventarioService inventarioService = new InventarioService();
    private final ReporteService reporteService = new ReporteService();

    private final Tarjeta cardVentas;
    private final Tarjeta cardProductos;
    private final Tarjeta cardStockBajo;
    private final Tarjeta cardAdeudos;

    private final Ui.Modelo mMovimientos = new Ui.Modelo(List.of("Fecha", "Movimiento", "Producto", "Cantidad"));
    private final JTable tMovimientos = new JTable(mMovimientos);

    public InicioPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // --- Fila Superior: 4 Tarjetas Informativas (KPIs) ---
        JPanel panelCards = new JPanel(new GridLayout(1, 4, 14, 0));
        panelCards.setOpaque(false);

        cardVentas = new Tarjeta(Iconos.ventas(22, Tema.VERDE_OSCURO), Tema.VERDE_SALVIA_CLARO,
                "$0.00", "Ventas de hoy", "0 ventas");
        cardProductos = new Tarjeta(Iconos.productos(22, Tema.VERDE_SALVIA), new Color(0xEF, 0xF5, 0xEB),
                "0", "Productos activos", "En catálogo");
        cardStockBajo = new Tarjeta(Iconos.alerta(22, Tema.TERRACOTA), Tema.TERRACOTA_CLARO,
                "0", "Stock bajo", "Por reabastecer");
        cardAdeudos = new Tarjeta(Iconos.dinero(22, Tema.TERRACOTA), new Color(0xFE, 0xF0, 0xE7),
                "$0.00", "Saldo por cobrar", "0 clientes");

        panelCards.add(cardVentas);
        panelCards.add(cardProductos);
        panelCards.add(cardStockBajo);
        panelCards.add(cardAdeudos);

        // --- Sección Central: Movimientos Recientes y Accesos Rápidos ---
        JPanel centro = new JPanel(new BorderLayout(14, 0));
        centro.setOpaque(false);

        // Tarjeta para la tabla de Movimientos Recientes
        JPanel cardTabla = Tema.tarjeta();
        cardTabla.setLayout(new BorderLayout(0, 10));

        JLabel lblTitTabla = new JLabel("Movimientos recientes de inventario");
        lblTitTabla.setFont(Tema.FUENTE_TITULO);
        lblTitTabla.setForeground(Tema.TEXTO_TITULO);
        cardTabla.add(lblTitTabla, BorderLayout.NORTH);

        Tema.configurarTablaModerna(tMovimientos);
        tMovimientos.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                String str = value == null ? "" : value.toString();
                Color fondo = Tema.VERDE_SALVIA_CLARO;
                Color texto = Tema.VERDE_OSCURO;
                if (str.contains("merma")) {
                    fondo = Tema.ROJO_BADGE_FONDO;
                    texto = Tema.ROJO_BADGE_TEXTO;
                } else if (str.contains("compra")) {
                    fondo = new Color(0xEA, 0xF1, 0xFA);
                    texto = new Color(0x1E, 0x56, 0x92);
                }
                JLabel b = Tema.badge(formatearTipo(str), fondo, texto);
                b.setHorizontalAlignment(SwingConstants.CENTER);
                return b;
            }
        });

        JScrollPane scroll = new JScrollPane(tMovimientos);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.GRIS_SUAVE));
        scroll.getViewport().setBackground(Tema.BLANCO_TARJETA);
        cardTabla.add(scroll, BorderLayout.CENTER);

        // Panel Lateral Derecho: Resumen / Accesos Directos
        JPanel cardLateral = Tema.tarjeta();
        cardLateral.setPreferredSize(new Dimension(280, 0));
        cardLateral.setLayout(new BoxLayout(cardLateral, BoxLayout.Y_AXIS));

        JLabel lblAtajos = new JLabel("Acciones recomendadas");
        lblAtajos.setFont(Tema.FUENTE_TITULO);
        lblAtajos.setForeground(Tema.TEXTO_TITULO);

        JLabel lblConsejo = new JLabel("<html><body style='width: 220px; color: #6A7A70; font-size: 11px;'>"
                + "Revise los productos con stock bajo y confirme las compras de Central de Abastos al inicio de la jornada."
                + "</body></html>");

        JLabel lblTeclas = new JLabel("<html><body style='width: 220px; color: #222B25; font-size: 12px; line-height: 1.6;'>"
                + "<b>Atajos clave:</b><br>"
                + "• <b>F1:</b> Guía de atajos de teclado<br>"
                + "• <b>Alt+V:</b> Ir a Ventas (Punto de venta)<br>"
                + "• <b>Alt+C:</b> Ir a Compras<br>"
                + "• <b>Alt+P:</b> Catálogo de productos<br>"
                + "• <b>Alt+I:</b> Almacén e inventario"
                + "</body></html>");

        cardLateral.add(lblAtajos);
        cardLateral.add(Box.createVerticalStrut(8));
        cardLateral.add(lblConsejo);
        cardLateral.add(Box.createVerticalStrut(16));
        cardLateral.add(new JSeparator(SwingConstants.HORIZONTAL));
        cardLateral.add(Box.createVerticalStrut(14));
        cardLateral.add(lblTeclas);
        cardLateral.add(Box.createVerticalGlue());

        centro.add(cardTabla, BorderLayout.CENTER);
        centro.add(cardLateral, BorderLayout.EAST);

        add(panelCards, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private static String formatearTipo(String t) {
        if (t == null) return "";
        return switch (t) {
            case "entrada_compra" -> "Compra";
            case "salida_venta" -> "Venta";
            case "merma" -> "Merma";
            case "ajuste_entrada" -> "Ajuste +";
            case "ajuste_salida" -> "Ajuste -";
            default -> t;
        };
    }

    @Override public void recargar() {
        Ui.intentar(this, () -> {
            LocalDate hoy = LocalDate.now();
            LocalDateTime inicioHoy = hoy.atStartOfDay();
            LocalDateTime finHoy = hoy.atTime(23, 59, 59);

            // 1. Ventas de hoy
            List<Venta> ventasHoy = ventasService.historial(inicioHoy, finHoy);
            BigDecimal totalHoy = ventasHoy.stream().map(Venta::total).reduce(BigDecimal.ZERO, BigDecimal::add);
            cardVentas.actualizar(Ui.dinero(totalHoy), ventasHoy.size() + " venta" + (ventasHoy.size() == 1 ? "" : "s"));

            // 2. Productos activos
            List<Producto> productosActivos = productoService.listar("", true);
            cardProductos.actualizar(String.valueOf(productosActivos.size()), "En catálogo");

            // 3. Productos con stock bajo
            Tabla bajoStock = reporteService.bajoStock();
            cardStockBajo.actualizar(String.valueOf(bajoStock.filas().size()), "Por reabastecer");

            // 4. Saldo por cobrar a clientes
            List<Cliente> clientes = clienteService.listar("", false);
            BigDecimal totalPendiente = BigDecimal.ZERO;
            int clientesConDeuda = 0;
            for (Cliente c : clientes) {
                if (c.saldo() != null && c.saldo().signum() > 0) {
                    totalPendiente = totalPendiente.add(c.saldo());
                    clientesConDeuda++;
                }
            }
            cardAdeudos.actualizar(Ui.dinero(totalPendiente), clientesConDeuda + " cliente" + (clientesConDeuda == 1 ? "" : "s"));

            // 5. Movimientos recientes (últimos 8)
            Tabla movs = inventarioService.movimientos(null, null);
            List<Object[]> filasMostrar = new ArrayList<>();
            int max = Math.min(8, movs.filas().size());
            for (int i = 0; i < max; i++) {
                Object[] orig = movs.filas().get(i);
                // columnas de reporte: Fecha, Producto, Tipo, Cantidad, Usuario, Motivo
                filasMostrar.add(new Object[]{orig[0], orig[2], orig[1], orig[3]});
            }
            mMovimientos.set(filasMostrar);
        });
    }
}

