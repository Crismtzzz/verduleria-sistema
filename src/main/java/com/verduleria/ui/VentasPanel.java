package com.verduleria.ui;

import com.verduleria.model.*;
import com.verduleria.service.ClienteService;
import com.verduleria.service.ProductoService;
import com.verduleria.service.VentaService;
import com.verduleria.util.NegocioException;
import com.verduleria.util.Validador;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Módulo de ventas rediseñado (CU-VEN-01 registrar venta y CU-VEN-02 historial).
 * Navegación por píldoras, tarjeta de formulario, cobro en efectivo con cálculo automático de cambio,
 * bloqueo ante monto insuficiente, inhabilitación en venta a crédito y registro contable.
 */
public class VentasPanel extends JPanel implements Recargable {
    private static final Cliente MOSTRADOR = new Cliente(0, "(Mostrador / sin cliente)", null, true, BigDecimal.ZERO);

    private final VentaService servicio = new VentaService();
    private final ProductoService productos = new ProductoService();
    private final ClienteService clientes = new ClienteService();

    // Nueva venta
    private final JComboBox<Producto> cmbProducto = new JComboBox<>();
    private final JLabel lblInfo = new JLabel(" ");
    private final JTextField txtCantidad = Tema.campoTexto(7);
    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JCheckBox chkCredito = new JCheckBox("Venta a crédito (fiado)");
    private final List<Linea> lineas = new ArrayList<>();
    private final Ui.Modelo mLineas = new Ui.Modelo(List.of("Producto", "Unidad", "Cantidad", "Precio ($)", "Importe ($)"));
    private final JTable tLineas = Ui.tabla(mLineas);
    private final JLabel lblTotal = new JLabel("Total: $0.00");

    // Cobro en efectivo y cambio
    private final JTextField txtEfectivo = Tema.campoTexto(8);
    private final JLabel lblCambio = new JLabel("Cambio: $0.00");

    // Historial
    private final Ui.Modelo mHist = new Ui.Modelo(List.of("Folio", "Fecha", "Cliente", "Atendió", "Total ($)", "Método", "Efectivo ($)", "Cambio ($)", "Estado"));
    private final JTable tHist = Ui.tabla(mHist);
    private final JTextField txtDesde = Tema.campoTexto(9);
    private final JTextField txtHasta = Tema.campoTexto(9);
    private List<Venta> ventas = new ArrayList<>();

    // Estado vacío visual cuando el carrito no tiene productos
    private final JPanel panelVacio = new JPanel();

    public VentasPanel() {
        super(new BorderLayout(0, 12));
        setBackground(Tema.CREMA);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        CardLayout subCards = new CardLayout();
        JPanel contenedorVistas = new JPanel(subCards);
        contenedorVistas.setOpaque(false);
        contenedorVistas.add(panelNueva(), "NUEVA");
        contenedorVistas.add(panelHistorial(), "HISTORIAL");

        PildorasSelector pildoras = new PildorasSelector(new String[]{"Nueva venta", "Historial de ventas"}, idx -> {
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
        Ui.atajo(this, KeyEvent.VK_F2, "focoProducto", () -> cmbProducto.requestFocusInWindow());
        Ui.atajo(this, KeyEvent.VK_F3, "focoCantidad", () -> {
            txtCantidad.requestFocusInWindow();
            txtCantidad.selectAll();
        });
        Ui.atajo(this, KeyEvent.VK_F4, "toggleCredito", () -> {
            chkCredito.setSelected(!chkCredito.isSelected());
            actualizarEstadoCredito();
        });
        Ui.atajo(this, KeyEvent.VK_F6, "focoCliente", () -> cmbCliente.requestFocusInWindow());
        Ui.atajo(this, KeyEvent.VK_F7, "focoEfectivo", () -> {
            if (txtEfectivo.isEnabled()) {
                txtEfectivo.requestFocusInWindow();
                txtEfectivo.selectAll();
            }
        });
        Ui.atajo(this, KeyEvent.VK_DELETE, "quitarRenglon", this::quitar);
        Ui.atajo(this, KeyEvent.VK_F5, "cancelarVenta", this::limpiar);
        Ui.atajo(this, KeyEvent.VK_F9, "confirmarVentaF9", this::confirmar);
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        Ui.atajo(this, KeyEvent.VK_ENTER, ctrl, "confirmarVentaCtrlEnter", this::confirmar);
    }

    private JPanel panelNueva() {
        lblTotal.setFont(Tema.FUENTE_KPI_VALOR);
        lblTotal.setForeground(Tema.VERDE_OSCURO);

        cmbProducto.setPreferredSize(new Dimension(280, 32));
        cmbCliente.setPreferredSize(new Dimension(240, 32));
        cmbProducto.setToolTipText("Seleccione un producto (Atajo: F2)");
        txtCantidad.setToolTipText("Ingrese la cantidad y presione Enter (Atajo: F3)");
        chkCredito.setToolTipText("Marque para venta fiada/a crédito (Atajo: F4)");
        chkCredito.setFont(Tema.FUENTE_NORMAL);
        chkCredito.setOpaque(false);
        cmbCliente.setToolTipText("Seleccione cliente (Atajo: F6)");

        cmbProducto.addActionListener(e -> mostrarInfo());
        txtCantidad.addActionListener(e -> agregar());
        chkCredito.addActionListener(e -> actualizarEstadoCredito());

        // Configuración de cobro en efectivo y cambio
        txtEfectivo.setPreferredSize(new Dimension(95, 30));
        txtEfectivo.setFont(Tema.FUENTE_NEGRITA);
        txtEfectivo.setToolTipText("Ingrese el efectivo entregado por el cliente (Atajo: F7, Enter para cobrar)");
        txtEfectivo.addActionListener(e -> confirmar());
        txtEfectivo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calcularCambio(); }
            @Override public void removeUpdate(DocumentEvent e) { calcularCambio(); }
            @Override public void changedUpdate(DocumentEvent e) { calcularCambio(); }
        });

        lblCambio.setFont(Tema.FUENTE_NEGRITA.deriveFont(14f));
        lblCambio.setForeground(Tema.VERDE_OSCURO);

        // Tarjeta de Controles Superiores
        JPanel cardControles = Tema.tarjeta();
        cardControles.setLayout(new GridLayout(2, 1, 0, 8));

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        fila1.setOpaque(false);
        JLabel lblProd = new JLabel("Producto (F2):");
        lblProd.setFont(Tema.FUENTE_NEGRITA);
        lblProd.setForeground(Tema.TEXTO_PRINCIPAL);
        fila1.add(lblProd);
        fila1.add(cmbProducto);

        JLabel lblCant = new JLabel("Cantidad (F3):");
        lblCant.setFont(Tema.FUENTE_NEGRITA);
        lblCant.setForeground(Tema.TEXTO_PRINCIPAL);
        fila1.add(lblCant);
        fila1.add(txtCantidad);

        JButton btnAgregar = Tema.botonPrimario("Agregar (Enter)", Iconos.mas(16, Color.WHITE), this::agregar);
        fila1.add(btnAgregar);

        lblInfo.setFont(Tema.FUENTE_NORMAL);
        lblInfo.setForeground(Tema.TEXTO_SECUNDARIO);
        fila1.add(lblInfo);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        fila2.setOpaque(false);
        JLabel lblCli = new JLabel("Cliente (F6):");
        lblCli.setFont(Tema.FUENTE_NEGRITA);
        lblCli.setForeground(Tema.TEXTO_PRINCIPAL);
        fila2.add(lblCli);
        fila2.add(cmbCliente);
        fila2.add(chkCredito);

        cardControles.add(fila1);
        cardControles.add(fila2);

        // Tabla Central con soporte para estado vacío
        JScrollPane scroll = Tema.scrollTabla(tLineas);
        configurarPanelVacio();

        JPanel centroTabla = new JPanel(new CardLayout());
        centroTabla.setOpaque(false);
        centroTabla.add(scroll, "TABLA");
        centroTabla.add(panelVacio, "VACIO");
        ((CardLayout) centroTabla.getLayout()).show(centroTabla, "VACIO");

        tLineas.getModel().addTableModelListener(e -> {
            CardLayout cl = (CardLayout) centroTabla.getLayout();
            if (lineas.isEmpty()) cl.show(centroTabla, "VACIO");
            else cl.show(centroTabla, "TABLA");
        });

        // Tarjeta Inferior de Resumen, Cobro en Efectivo, Cambio y Acciones
        JPanel cardSur = Tema.tarjeta();
        cardSur.setLayout(new BorderLayout(14, 0));
        cardSur.setBorder(new EmptyBorder(8, 16, 8, 16));

        JButton btnQuitar = Tema.botonSecundario("Quitar renglón (Supr)", Iconos.eliminar(16, Tema.TEXTO_PRINCIPAL), this::quitar);
        cardSur.add(btnQuitar, BorderLayout.WEST);

        // Bloque central de cobro: Total | Efectivo recibido | Cambio a entregar
        JPanel bloqueCobro = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        bloqueCobro.setOpaque(false);

        bloqueCobro.add(lblTotal);

        JLabel sep1 = new JLabel("|");
        sep1.setForeground(Tema.GRIS_BORDE);
        bloqueCobro.add(sep1);

        JLabel lblEf = new JLabel("Efectivo recibido (F7):");
        lblEf.setFont(Tema.FUENTE_NEGRITA);
        lblEf.setForeground(Tema.TEXTO_PRINCIPAL);
        bloqueCobro.add(lblEf);
        bloqueCobro.add(txtEfectivo);

        JLabel sep2 = new JLabel("|");
        sep2.setForeground(Tema.GRIS_BORDE);
        bloqueCobro.add(sep2);

        bloqueCobro.add(lblCambio);

        cardSur.add(bloqueCobro, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        JButton btnConfirmar = Tema.botonPrimario("Confirmar venta (F9)", Iconos.confirmar(16, Color.WHITE), this::confirmar);
        JButton btnCancelar = Tema.botonSecundario("Cancelar (F5)", Iconos.cancelar(16, Tema.TEXTO_PRINCIPAL), this::limpiar);
        derecha.add(btnConfirmar);
        derecha.add(btnCancelar);
        cardSur.add(derecha, BorderLayout.EAST);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.add(cardControles, BorderLayout.NORTH);
        p.add(centroTabla, BorderLayout.CENTER);
        p.add(cardSur, BorderLayout.SOUTH);
        return p;
    }

    private void actualizarEstadoCredito() {
        boolean credito = chkCredito.isSelected();
        txtEfectivo.setEnabled(!credito);
        if (credito) {
            txtEfectivo.setText("");
            lblCambio.setText("Cambio: — (Venta a crédito)");
            lblCambio.setForeground(Tema.TEXTO_SECUNDARIO);
        } else {
            calcularCambio();
            if (!lineas.isEmpty()) {
                txtEfectivo.requestFocusInWindow();
            }
        }
    }

    private void calcularCambio() {
        BigDecimal total = VentaService.total(lineas);
        lblTotal.setText("Total: " + Ui.dinero(total));

        if (chkCredito.isSelected()) {
            txtEfectivo.setEnabled(false);
            lblCambio.setText("Cambio: — (Venta a crédito)");
            lblCambio.setForeground(Tema.TEXTO_SECUNDARIO);
            return;
        }

        txtEfectivo.setEnabled(true);
        String txt = txtEfectivo.getText().trim();
        if (txt.isEmpty()) {
            lblCambio.setText("Cambio: $0.00");
            lblCambio.setForeground(Tema.TEXTO_SECUNDARIO);
            return;
        }

        try {
            BigDecimal ef = Validador.decimal(txt, "Efectivo recibido", 2);
            if (ef.compareTo(total) >= 0) {
                BigDecimal cambio = ef.subtract(total);
                lblCambio.setText("Cambio: " + Ui.dinero(cambio));
                lblCambio.setForeground(Tema.VERDE_OSCURO);
            } else {
                BigDecimal falta = total.subtract(ef);
                lblCambio.setText("Falta: " + Ui.dinero(falta));
                lblCambio.setForeground(Tema.TERRACOTA);
            }
        } catch (Exception ex) {
            lblCambio.setText("Cambio: —");
            lblCambio.setForeground(Tema.TERRACOTA);
        }
    }

    private void configurarPanelVacio() {
        panelVacio.setLayout(new BoxLayout(panelVacio, BoxLayout.Y_AXIS));
        panelVacio.setBackground(Tema.BLANCO_TARJETA);
        panelVacio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.GRIS_BORDE, 1, true),
                new EmptyBorder(40, 20, 40, 20)));

        JLabel iconVacio = new JLabel(Iconos.ventas(64, Tema.GRIS_BORDE));
        iconVacio.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txt1 = new JLabel("Agrega productos a la venta");
        txt1.setFont(Tema.FUENTE_TITULO);
        txt1.setForeground(Tema.TEXTO_SECUNDARIO);
        txt1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txt2 = new JLabel("Seleccione un producto arriba, indique la cantidad y presione Enter o F9");
        txt2.setFont(Tema.FUENTE_NORMAL);
        txt2.setForeground(Tema.TEXTO_SECUNDARIO);
        txt2.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelVacio.add(Box.createVerticalGlue());
        panelVacio.add(iconVacio);
        panelVacio.add(Box.createVerticalStrut(12));
        panelVacio.add(txt1);
        panelVacio.add(Box.createVerticalStrut(6));
        panelVacio.add(txt2);
        panelVacio.add(Box.createVerticalGlue());
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
        JButton btnDetalle = Tema.botonSecundario("Ver detalle", Iconos.productos(16, Tema.TEXTO_PRINCIPAL), this::verDetalle);
        cardFiltros.add(btnConsultar);
        cardFiltros.add(btnDetalle);

        Tema.aplicarBadge(tHist, "Estado");
        Tema.aplicarBadge(tHist, "Método");
        JScrollPane scroll = Tema.scrollTabla(tHist);

        tHist.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) verDetalle(); }
        });

        p.add(cardFiltros, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    @Override public void recargar() {
        Ui.intentar(this, () -> {
            Object selP = cmbProducto.getSelectedItem();
            cmbProducto.removeAllItems();
            for (Producto p : productos.activosPara(Permiso.VENTAS)) cmbProducto.addItem(p);
            if (selP != null) cmbProducto.setSelectedItem(selP);

            Object selC = cmbCliente.getSelectedItem();
            cmbCliente.removeAllItems();
            cmbCliente.addItem(MOSTRADOR);
            for (Cliente c : clientes.activosParaVenta()) cmbCliente.addItem(c);
            if (selC != null) cmbCliente.setSelectedItem(selC);
        });
        mostrarInfo();
        cargarHistorial();
    }

    private void mostrarInfo() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        lblInfo.setText(p == null ? " " : "  Precio: " + Ui.dinero(p.precioVenta()) + " / " + p.unidad()
                + "   Disponible: " + Ui.cantidad(p.existencia()));
    }

    private void agregar() {
        Ui.intentar(this, () -> {
            Producto p = (Producto) cmbProducto.getSelectedItem();
            Linea l = VentaService.crearLinea(p, txtCantidad.getText());
            BigDecimal yaAgregado = lineas.stream().filter(x -> x.idProducto() == l.idProducto())
                    .map(Linea::cantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (yaAgregado.add(l.cantidad()).compareTo(p.existencia()) > 0)
                throw new com.verduleria.util.NegocioException("Existencia insuficiente de «" + p.nombre()
                        + "»: disponible " + Ui.cantidad(p.existencia()) + " " + p.unidad() + ".");
            lineas.add(l);
            txtCantidad.setText("");
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
        calcularCambio();
    }

    private void limpiar() {
        lineas.clear();
        chkCredito.setSelected(false);
        txtEfectivo.setText("");
        txtEfectivo.setEnabled(true);
        refrescarLineas();
    }

    private void confirmar() {
        if (lineas.isEmpty()) { Ui.aviso(this, "Agregue al menos un producto a la venta."); return; }
        Cliente sel = (Cliente) cmbCliente.getSelectedItem();
        Cliente cliente = sel == null || sel.id() == 0 ? null : sel;
        boolean credito = chkCredito.isSelected();
        if (credito && cliente == null) {
            Ui.aviso(this, "Una venta a crédito requiere seleccionar un cliente.");
            return;
        }

        BigDecimal total = VentaService.total(lineas);
        BigDecimal efectivo = null;
        BigDecimal cambio = null;

        if (!credito) {
            String txtEf = txtEfectivo.getText().trim();
            if (txtEf.isEmpty()) {
                Ui.aviso(this, "Ingrese el efectivo recibido del cliente para cobrar (Atajo: F7).");
                txtEfectivo.requestFocusInWindow();
                return;
            }
            try {
                efectivo = Validador.decimal(txtEf, "Efectivo recibido", 2);
            } catch (NegocioException e) {
                Ui.aviso(this, e.getMessage());
                txtEfectivo.requestFocusInWindow();
                txtEfectivo.selectAll();
                return;
            }

            if (efectivo.compareTo(total) < 0) {
                BigDecimal falta = total.subtract(efectivo);
                Ui.aviso(this, "El efectivo recibido (" + Ui.dinero(efectivo) + ") es insuficiente.\n"
                        + "Total a pagar: " + Ui.dinero(total) + "\n"
                        + "Faltan: " + Ui.dinero(falta));
                txtEfectivo.requestFocusInWindow();
                txtEfectivo.selectAll();
                return;
            }
            cambio = efectivo.subtract(total);
        }

        StringBuilder msg = new StringBuilder();
        msg.append("¿Confirmar la venta por ").append(Ui.dinero(total)).append("?\n");
        if (credito) {
            msg.append("Se registrará un adeudo a crédito para ").append(cliente.nombre()).append(".");
        } else {
            msg.append("Efectivo recibido: ").append(Ui.dinero(efectivo)).append("\n");
            msg.append("Cambio a entregar: ").append(Ui.dinero(cambio));
        }

        if (!Ui.confirmar(this, msg.toString())) return;

        int[] folio = new int[1];
        final BigDecimal efFinal = efectivo;
        final BigDecimal caFinal = cambio;
        boolean ok = Ui.intentar(this, () -> folio[0] = servicio.confirmar(cliente, credito, new ArrayList<>(lineas), efFinal, caFinal));
        if (ok) {
            String resumen = "Venta registrada con folio #" + folio[0] + ".\nTotal: " + Ui.dinero(total);
            if (!credito) {
                resumen += "\nEfectivo recibido: " + Ui.dinero(efFinal) + "\nCambio entregado: " + Ui.dinero(caFinal);
            }
            Ui.info(this, resumen);
            limpiar();
            recargar();      // refresca existencias y clientes
        } else {
            recargar();      // las existencias pudieron cambiar
        }
    }

    private void cargarHistorial() {
        Ui.intentar(this, () -> {
            ventas = servicio.historial(Validador.fechaOpcional(txtDesde.getText(), "Desde"),
                    Validador.fechaOpcional(txtHasta.getText(), "Hasta"));
            List<Object[]> filas = new ArrayList<>();
            for (Venta v : ventas) {
                String metodoStr = "CREDITO".equalsIgnoreCase(v.metodoPago()) ? "Crédito" : "Efectivo";
                String efStr = v.efectivoRecibido() == null ? "—" : Ui.dinero(v.efectivoRecibido());
                String caStr = v.cambio() == null ? "—" : Ui.dinero(v.cambio());
                filas.add(new Object[]{v.id(), v.fecha().format(Validador.FECHA_HORA), v.cliente(),
                        v.usuario(), v.total(), metodoStr, efStr, caStr, v.estado()});
            }
            mHist.set(filas);
        });
    }

    private void verDetalle() {
        int i = Ui.seleccionada(tHist);
        if (i < 0) { Ui.aviso(this, "Seleccione una venta del historial."); return; }
        Venta v = ventas.get(i);
        String infoPago = "CREDITO".equalsIgnoreCase(v.metodoPago())
                ? "Método: Venta a Crédito (Fiado)"
                : "Método: Efectivo   |   Recibido: " + (v.efectivoRecibido() == null ? "—" : Ui.dinero(v.efectivoRecibido()))
                  + "   |   Cambio: " + (v.cambio() == null ? "—" : Ui.dinero(v.cambio()));
        String encabezado = "Fecha: " + v.fecha().format(Validador.FECHA_HORA)
                + "   |   Cliente: " + v.cliente()
                + "   |   Atendió: " + v.usuario()
                + "\n" + infoPago;
        Ui.intentar(this, () -> DetalleDialog.mostrar(this, "Venta #" + v.id(), servicio.detalle(v.id()), v.total(), encabezado));
    }
}
