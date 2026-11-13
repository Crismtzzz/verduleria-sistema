package com.verduleria.ui;

import com.verduleria.model.*;
import com.verduleria.service.CompraService;
import com.verduleria.service.ProductoService;
import com.verduleria.util.Validador;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Módulo de compras rediseñado (CU-COM-01 registrar compra sin proveedor y CU-COM-02 historial).
 * Navegación por píldoras, formulario compacto y resumen de total destacado.
 */
public class ComprasPanel extends JPanel implements Recargable {
    private final CompraService servicio = new CompraService();
    private final ProductoService productos = new ProductoService();

    // Nueva compra
    private final JTextField txtFecha = Tema.campoTexto(13);
    private final JComboBox<Producto> cmbProducto = new JComboBox<>();
    private final JTextField txtCantidad = Tema.campoTexto(7);
    private final JTextField txtPrecio = Tema.campoTexto(7);
    private final List<Linea> lineas = new ArrayList<>();
    private final Ui.Modelo mLineas = new Ui.Modelo(List.of("Producto", "Unidad", "Cantidad", "Precio compra ($)", "Importe ($)"));
    private final JTable tLineas = Ui.tabla(mLineas);
    private final JLabel lblTotal = new JLabel("Total: $0.00");

    // Historial
    private final Ui.Modelo mHist = new Ui.Modelo(List.of("Folio", "Fecha", "Registró", "Total ($)", "Estado"));
    private final JTable tHist = Ui.tabla(mHist);
    private final JTextField txtDesde = Tema.campoTexto(9);
    private final JTextField txtHasta = Tema.campoTexto(9);
    private List<Compra> compras = new ArrayList<>();

    public ComprasPanel() {
        super(new BorderLayout(0, 12));
        setBackground(Tema.CREMA);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        CardLayout subCards = new CardLayout();
        JPanel contenedorVistas = new JPanel(subCards);
        contenedorVistas.setOpaque(false);
        contenedorVistas.add(panelNueva(), "NUEVA");
        contenedorVistas.add(panelHistorial(), "HISTORIAL");

        PildorasSelector pildoras = new PildorasSelector(new String[]{"Nueva compra", "Historial de compras"}, idx -> {
            subCards.show(contenedorVistas, idx == 0 ? "NUEVA" : "HISTORIAL");
            if (idx == 1) cargarHistorial();
        });

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cabecera.setOpaque(false);
        cabecera.add(pildoras);

        add(cabecera, BorderLayout.NORTH);
        add(contenedorVistas, BorderLayout.CENTER);
        configurarAtajos();
    }

    private void configurarAtajos() {
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F2, "focoProducto", () -> cmbProducto.requestFocusInWindow());
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F3, "focoCantidad", () -> {
            txtCantidad.requestFocusInWindow();
            txtCantidad.selectAll();
        });
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F4, "focoPrecio", () -> {
            txtPrecio.requestFocusInWindow();
            txtPrecio.selectAll();
        });
        Ui.atajo(this, java.awt.event.KeyEvent.VK_DELETE, "quitarRenglon", this::quitar);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F5, "cancelarCompra", this::limpiar);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F9, "confirmarCompraF9", this::confirmar);
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        Ui.atajo(this, java.awt.event.KeyEvent.VK_ENTER, ctrl, "confirmarCompraCtrlEnter", this::confirmar);
    }

    private JPanel panelNueva() {
        lblTotal.setFont(Tema.FUENTE_KPI_VALOR);
        lblTotal.setForeground(Tema.VERDE_OSCURO);

        cmbProducto.setPreferredSize(new Dimension(260, 32));
        cmbProducto.setToolTipText("Seleccione un producto (Atajo: F2)");
        txtCantidad.setToolTipText("Cantidad adquirida (Atajo: F3)");
        txtPrecio.setToolTipText("Precio unitario de compra y Enter (Atajo: F4)");

        // Tarjeta de Formulario
        JPanel cardForm = Tema.tarjeta();
        cardForm.setLayout(new BorderLayout(0, 6));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        form.setOpaque(false);

        JLabel lblF = new JLabel("Fecha:");
        lblF.setFont(Tema.FUENTE_NEGRITA);
        lblF.setForeground(Tema.TEXTO_PRINCIPAL);
        form.add(lblF);
        form.add(txtFecha);

        JLabel lblP = new JLabel("Producto (F2):");
        lblP.setFont(Tema.FUENTE_NEGRITA);
        lblP.setForeground(Tema.TEXTO_PRINCIPAL);
        form.add(lblP);
        form.add(cmbProducto);

        JLabel lblC = new JLabel("Cantidad (F3):");
        lblC.setFont(Tema.FUENTE_NEGRITA);
        lblC.setForeground(Tema.TEXTO_PRINCIPAL);
        form.add(lblC);
        form.add(txtCantidad);

        JLabel lblPr = new JLabel("Precio compra (F4):");
        lblPr.setFont(Tema.FUENTE_NEGRITA);
        lblPr.setForeground(Tema.TEXTO_PRINCIPAL);
        form.add(lblPr);
        form.add(txtPrecio);

        JButton btnAgregar = Tema.botonPrimario("Agregar (Enter)", Iconos.mas(16, Color.WHITE), this::agregar);
        form.add(btnAgregar);

        txtPrecio.addActionListener(e -> agregar());

        JLabel nota = new JLabel("  Mercancía adquirida en Central de Abastos: no requiere proveedor.");
        nota.setFont(Tema.FUENTE_PEQUE);
        nota.setForeground(Tema.TEXTO_SECUNDARIO);

        cardForm.add(form, BorderLayout.NORTH);
        cardForm.add(nota, BorderLayout.SOUTH);

        // Tabla
        JScrollPane scroll = Tema.scrollTabla(tLineas);

        // Tarjeta Inferior de Resumen
        JPanel cardSur = Tema.tarjeta();
        cardSur.setLayout(new BorderLayout(14, 0));
        cardSur.setBorder(new EmptyBorder(10, 16, 10, 16));

        JButton btnQuitar = Tema.botonSecundario("Quitar renglón (Supr)", Iconos.eliminar(16, Tema.TEXTO_PRINCIPAL), this::quitar);
        cardSur.add(btnQuitar, BorderLayout.WEST);

        lblTotal.setHorizontalAlignment(SwingConstants.CENTER);
        cardSur.add(lblTotal, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        JButton btnConfirmar = Tema.botonPrimario("Confirmar compra (F9)", Iconos.confirmar(16, Color.WHITE), this::confirmar);
        JButton btnCancelar = Tema.botonSecundario("Cancelar (F5)", Iconos.cancelar(16, Tema.TEXTO_PRINCIPAL), this::limpiar);
        derecha.add(btnConfirmar);
        derecha.add(btnCancelar);
        cardSur.add(derecha, BorderLayout.EAST);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.add(cardForm, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(cardSur, BorderLayout.SOUTH);
        return p;
    }

    private JPanel panelHistorial() {
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

        JButton btnConsultar = Tema.botonPrimario("Consultar", Iconos.buscar(16, Color.WHITE), this::cargarHistorial);
        JButton btnDetalle = Tema.botonSecundario("Ver detalle", Iconos.compras(16, Tema.TEXTO_PRINCIPAL), this::verDetalle);
        cardFiltros.add(btnConsultar);
        cardFiltros.add(btnDetalle);

        Tema.aplicarBadge(tHist, "Estado");
        JScrollPane scroll = Tema.scrollTabla(tHist);

        tHist.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) verDetalle(); }
        });

        p.add(cardFiltros, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    @Override public void recargar() {
        if (txtFecha.getText().isBlank()) txtFecha.setText(LocalDateTime.now().format(Validador.FECHA_HORA));
        Ui.intentar(this, () -> {
            Object sel = cmbProducto.getSelectedItem();
            cmbProducto.removeAllItems();
            for (Producto p : productos.activosPara(Permiso.COMPRAS)) cmbProducto.addItem(p);
            if (sel != null) cmbProducto.setSelectedItem(sel);
        });
        cargarHistorial();
    }

    private void agregar() {
        Ui.intentar(this, () -> {
            Linea l = CompraService.crearLinea((Producto) cmbProducto.getSelectedItem(), txtCantidad.getText(), txtPrecio.getText());
            lineas.add(l);
            txtCantidad.setText("");
            txtPrecio.setText("");
            refrescarLineas();
            txtCantidad.requestFocus();
        });
    }

    private void quitar() {
        int i = Ui.seleccionada(tLineas);
        if (i < 0) { Ui.aviso(this, "Seleccione el renglón que desea quitar."); return; }
        lineas.remove(i);
        refrescarLineas();
    }

    private void refrescarLineas() {
        List<Object[]> filas = new ArrayList<>();
        for (Linea l : lineas)
            filas.add(new Object[]{l.producto(), l.unidad(), l.cantidad(), l.precio(), l.subtotal()});
        mLineas.set(filas);
        lblTotal.setText("Total: " + Ui.dinero(CompraService.total(lineas)));
    }

    private void limpiar() {
        lineas.clear();
        refrescarLineas();
    }

    private void confirmar() {
        if (lineas.isEmpty()) { Ui.aviso(this, "Agregue al menos un producto a la compra."); return; }
        BigDecimal total = CompraService.total(lineas);
        if (!Ui.confirmar(this, "¿Confirmar la compra por " + Ui.dinero(total) + "?\nSe incrementarán las existencias.")) return;
        int[] folio = new int[1];
        boolean ok = Ui.intentar(this, () -> {
            LocalDateTime fecha = Validador.fechaHora(txtFecha.getText(), "Fecha");
            folio[0] = servicio.confirmar(fecha, new ArrayList<>(lineas));
        });
        if (ok) {
            Ui.info(this, "Compra registrada con folio #" + folio[0] + ".");
            limpiar();
            txtFecha.setText(LocalDateTime.now().format(Validador.FECHA_HORA));
            cargarHistorial();
        }
    }

    private void cargarHistorial() {
        Ui.intentar(this, () -> {
            compras = servicio.historial(Validador.fechaOpcional(txtDesde.getText(), "Desde"),
                    Validador.fechaOpcional(txtHasta.getText(), "Hasta"));
            List<Object[]> filas = new ArrayList<>();
            for (Compra c : compras)
                filas.add(new Object[]{c.id(), c.fecha().format(Validador.FECHA_HORA), c.usuario(), c.total(), c.estado()});
            mHist.set(filas);
        });
    }

    private void verDetalle() {
        int i = Ui.seleccionada(tHist);
        if (i < 0) { Ui.aviso(this, "Seleccione una compra del historial."); return; }
        Compra c = compras.get(i);
        Ui.intentar(this, () -> DetalleDialog.mostrar(this, "Compra #" + c.id(), servicio.detalle(c.id()), c.total(),
                "Fecha: " + c.fecha().format(Validador.FECHA_HORA) + "   Registró: " + c.usuario()));
    }
}
