package com.verduleria.ui;

import com.verduleria.model.Tabla;
import com.verduleria.service.ReporteService;
import com.verduleria.util.Validador;
import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Módulo de reportes rediseñado (CU-REP-01 a CU-REP-05 y saldos de adeudos).
 * Filtros organizados en tarjeta superior, tabla moderna con totales destacados y exportación CSV.
 */
public class ReportesPanel extends JPanel implements Recargable {
    private static final String[] REPORTES = {
        "Ventas", "Inventario", "Compras", "Movimientos de inventario", "Productos con baja existencia", "Adeudos de clientes"
    };
    private final ReporteService servicio = new ReporteService();
    private final JComboBox<String> cmbReporte = new JComboBox<>(REPORTES);
    private final JTextField txtDesde = Tema.campoTexto(9);
    private final JTextField txtHasta = Tema.campoTexto(9);
    private final Ui.Modelo modelo = new Ui.Modelo(List.of(" "));
    private final JTable tabla = Ui.tabla(modelo);
    private final JLabel lblResumen = new JLabel("Seleccione un reporte y presione Consultar.");
    private Tabla ultimo;

    public ReportesPanel() {
        super(new BorderLayout(0, 12));
        setBackground(Tema.CREMA);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        txtDesde.setToolTipText("AAAA-MM-DD (vacío = sin límite)");
        txtHasta.setToolTipText("AAAA-MM-DD (vacío = sin límite)");
        cmbReporte.setPreferredSize(new Dimension(220, 32));

        cmbReporte.addActionListener(e -> {
            boolean fechas = cmbReporte.getSelectedIndex() == 0 || cmbReporte.getSelectedIndex() == 2
                    || cmbReporte.getSelectedIndex() == 3;
            txtDesde.setEnabled(fechas);
            txtHasta.setEnabled(fechas);
        });

        // Tarjeta superior de filtros y exportación
        JPanel cardFiltros = Tema.tarjeta();
        cardFiltros.setLayout(new BorderLayout(10, 0));
        cardFiltros.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);

        JLabel lblRep = new JLabel("Reporte (F2):");
        lblRep.setFont(Tema.FUENTE_NEGRITA);
        lblRep.setForeground(Tema.TEXTO_PRINCIPAL);
        izquierda.add(lblRep);
        izquierda.add(cmbReporte);

        JLabel lblD = new JLabel("Desde:");
        lblD.setFont(Tema.FUENTE_NEGRITA);
        lblD.setForeground(Tema.TEXTO_PRINCIPAL);
        izquierda.add(lblD);
        izquierda.add(txtDesde);

        JLabel lblH = new JLabel("Hasta:");
        lblH.setFont(Tema.FUENTE_NEGRITA);
        lblH.setForeground(Tema.TEXTO_PRINCIPAL);
        izquierda.add(lblH);
        izquierda.add(txtHasta);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);

        JButton btnConsultar = Tema.botonPrimario("Consultar (F5)", Iconos.buscar(16, Color.WHITE), this::consultar);
        JButton btnExportar = Tema.botonSecundario("Exportar CSV (Ctrl+S)", Iconos.exportar(16, Tema.TEXTO_PRINCIPAL), this::exportar);
        derecha.add(btnConsultar);
        derecha.add(btnExportar);

        cardFiltros.add(izquierda, BorderLayout.WEST);
        cardFiltros.add(derecha, BorderLayout.EAST);

        // Tabla central
        JScrollPane scroll = Tema.scrollTabla(tabla);

        // Tarjeta inferior de resumen
        JPanel cardResumen = Tema.tarjeta();
        cardResumen.setLayout(new BorderLayout());
        cardResumen.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblResumen.setFont(Tema.FUENTE_NEGRITA);
        lblResumen.setForeground(Tema.TEXTO_TITULO);
        cardResumen.add(lblResumen, BorderLayout.CENTER);

        add(cardFiltros, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(cardResumen, BorderLayout.SOUTH);

        configurarAtajos();
    }

    private void configurarAtajos() {
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F2, "focoTipoReporte", () -> cmbReporte.requestFocusInWindow());
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F5, "consultarReporteF5", this::consultar);
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        Ui.atajo(this, java.awt.event.KeyEvent.VK_ENTER, ctrl, "consultarReporteCtrlEnter", this::consultar);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_S, ctrl, "exportarReporteCtrlS", this::exportar);
    }

    @Override public void recargar() { /* se consulta bajo demanda */ }

    private void consultar() {
        Ui.intentar(this, () -> {
            LocalDateTime d = Validador.fechaOpcional(txtDesde.getText(), "Desde");
            LocalDateTime h = Validador.fechaOpcional(txtHasta.getText(), "Hasta");
            int tipo = cmbReporte.getSelectedIndex();
            Tabla t = switch (tipo) {
                case 0 -> servicio.ventas(d, h);
                case 1 -> servicio.inventario();
                case 2 -> servicio.compras(d, h);
                case 3 -> servicio.movimientos(d, h);
                case 4 -> servicio.bajoStock();
                default -> servicio.adeudos();
            };
            ultimo = t;
            modelo.set(t.columnas(), t.filas());
            if (t.filas().isEmpty()) {
                lblResumen.setText("No hay información registrada con los criterios seleccionados.");
                return;
            }
            String col = switch (tipo) { case 0 -> "Total"; case 2 -> "Importe"; case 5 -> "Saldo pendiente"; default -> null; };
            String texto = "Total de registros: " + t.filas().size();
            if (col != null) {
                int idx = t.columnas().indexOf(col);
                BigDecimal suma = BigDecimal.ZERO;
                for (Object[] f : t.filas()) {
                    if (f[idx] instanceof BigDecimal b) {
                        suma = suma.add(b);
                    } else if (f[idx] instanceof Number n) {
                        suma = suma.add(BigDecimal.valueOf(n.doubleValue()));
                    }
                }
                texto += "      |      " + col + " acumulado: " + Ui.dinero(suma);
            }
            lblResumen.setText(texto);
        });
    }

    private void exportar() {
        if (ultimo == null || ultimo.filas().isEmpty()) { Ui.aviso(this, "Primero consulte un reporte con resultados."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("reporte.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (Writer w = Files.newBufferedWriter(fc.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
            w.write('\uFEFF'); // BOM para UTF-8 en Excel
            w.write(String.join(",", ultimo.columnas().stream().map(ReportesPanel::csv).toList()) + "\r\n");
            for (Object[] f : ultimo.filas()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < f.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(csv(f[i] == null ? "" : f[i] instanceof BigDecimal b ? b.toPlainString() : f[i].toString()));
                }
                w.write(sb + "\r\n");
            }
            Ui.info(this, "Reporte exportado exitosamente a:\n" + fc.getSelectedFile());
        } catch (IOException e) {
            Ui.error(this, "No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    private static String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
