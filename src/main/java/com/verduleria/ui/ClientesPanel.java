package com.verduleria.ui;

import com.verduleria.model.*;
import com.verduleria.service.ClienteService;
import com.verduleria.util.Validador;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Módulo de clientes rediseñado (CU-CLI-01 a CU-CLI-05 registro, consulta, adeudos y pagos).
 * Barra de acciones en tarjeta, estados por insignia y estado de cuenta moderno.
 */
public class ClientesPanel extends JPanel implements Recargable {
    private final ClienteService servicio = new ClienteService();
    private final Ui.Modelo modelo = new Ui.Modelo(List.of("ID", "Cliente", "Teléfono", "Saldo pendiente ($)", "Activo"));
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtBuscar = Tema.campoTexto(16);
    private List<Cliente> datos = new ArrayList<>();

    public ClientesPanel() {
        super(new BorderLayout(0, 12));
        setBackground(Tema.CREMA);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        txtBuscar.setToolTipText("Buscar cliente por nombre o teléfono");
        txtBuscar.addActionListener(e -> recargar());

        // Barra de acciones en tarjeta
        JPanel cardBarra = Tema.tarjeta();
        cardBarra.setLayout(new BorderLayout(10, 0));
        cardBarra.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);
        JLabel lblB = new JLabel("Buscar cliente:");
        lblB.setFont(Tema.FUENTE_NEGRITA);
        lblB.setForeground(Tema.TEXTO_PRINCIPAL);
        izquierda.add(lblB);
        izquierda.add(txtBuscar);

        JButton btnBuscar = Tema.botonPrimario("Buscar", Iconos.buscar(16, Color.WHITE), this::recargar);
        izquierda.add(btnBuscar);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);

        JButton btnNuevo = Tema.botonPrimario("+ Nuevo (F2)", Iconos.mas(16, Color.WHITE), () -> formulario(null));
        JButton btnMod = Tema.botonSecundario("Modificar (F3)", Iconos.editar(16, Tema.TEXTO_PRINCIPAL), this::modificar);
        JButton btnAdeudo = Tema.botonTerracota("Registrar adeudo (F4)", Iconos.alerta(16, Color.WHITE), this::adeudo);
        JButton btnPago = Tema.botonSecundario("Registrar pago (F6)", Iconos.dinero(16, Tema.TEXTO_PRINCIPAL), this::pago);
        JButton btnEstado = Tema.botonSecundario("Estado de cuenta (F7)", Iconos.reportes(16, Tema.TEXTO_PRINCIPAL), this::estadoCuenta);

        derecha.add(btnNuevo);
        derecha.add(btnMod);
        derecha.add(btnAdeudo);
        derecha.add(btnPago);
        derecha.add(btnEstado);

        cardBarra.add(izquierda, BorderLayout.WEST);
        cardBarra.add(derecha, BorderLayout.EAST);

        // Tabla con insignias para Activo y resaltado de saldo
        Tema.aplicarBadge(tabla, "Activo");
        configurarColumnaSaldo();

        JScrollPane scroll = Tema.scrollTabla(tabla);

        add(cardBarra, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        configurarAtajos();
    }

    private void configurarColumnaSaldo() {
        int idx = tabla.getColumnModel().getColumnIndex("Saldo pendiente ($)");
        tabla.getColumnModel().getColumn(idx).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof BigDecimal saldo) {
                    setText(Ui.dinero(saldo));
                    if (!isSelected && saldo.signum() > 0) {
                        setForeground(Tema.TERRACOTA);
                        setFont(Tema.FUENTE_NEGRITA);
                    } else if (!isSelected) {
                        setForeground(Tema.TEXTO_PRINCIPAL);
                        setFont(Tema.FUENTE_NORMAL);
                    }
                }
                return this;
            }
        });
    }

    private void configurarAtajos() {
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F2, "nuevoClienteF2", () -> formulario(null));
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        Ui.atajo(this, java.awt.event.KeyEvent.VK_N, ctrl, "nuevoClienteCtrlN", () -> formulario(null));
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F, ctrl, "buscarClienteCtrlF", () -> {
            txtBuscar.requestFocusInWindow();
            txtBuscar.selectAll();
        });
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F3, "modificarClienteF3", this::modificar);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F4, "adeudoClienteF4", this::adeudo);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F6, "pagoClienteF6", this::pago);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F7, "estadoCuentaF7", this::estadoCuenta);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F5, "recargarClientesF5", this::recargar);
    }

    @Override public void recargar() {
        Ui.intentar(this, () -> {
            datos = servicio.listar(txtBuscar.getText(), false);
            List<Object[]> filas = new ArrayList<>();
            for (Cliente c : datos)
                filas.add(new Object[]{c.id(), c.nombre(), c.telefono() == null ? "" : c.telefono(), c.saldo(), c.activo() ? "Sí" : "No"});
            modelo.set(filas);
        });
    }

    private Cliente seleccionado() {
        int i = Ui.seleccionada(tabla);
        if (i < 0) { Ui.aviso(this, "Seleccione un cliente de la tabla."); return null; }
        return datos.get(i);
    }

    private void modificar() {
        Cliente c = seleccionado();
        if (c != null) formulario(c);
    }

    private void formulario(Cliente existente) {
        JTextField nombre = Tema.campoTexto(22);
        JTextField tel = Tema.campoTexto(14);
        JCheckBox activo = new JCheckBox("Cliente activo", true);
        activo.setFont(Tema.FUENTE_NORMAL);
        activo.setOpaque(false);
        if (existente != null) {
            nombre.setText(existente.nombre());
            tel.setText(existente.telefono() == null ? "" : existente.telefono());
            activo.setSelected(existente.activo());
        }
        boolean ok = Ui.formulario(this, existente == null ? "Registrar cliente" : "Modificar cliente",
                new String[]{"Nombre:", "Teléfono (opcional):", ""}, new JComponent[]{nombre, tel, activo}, () -> {
                    if (existente == null) servicio.registrar(nombre.getText(), tel.getText());
                    else servicio.modificar(existente.id(), nombre.getText(), tel.getText(), activo.isSelected());
                });
        if (ok) recargar();
    }

    private void adeudo() {
        Cliente c = seleccionado();
        if (c == null) return;
        JTextField monto = Tema.campoTexto(10);
        JTextField desc = Tema.campoTexto(24);
        boolean ok = Ui.formulario(this, "Registrar adeudo a " + c.nombre(),
                new String[]{"Cliente:", "Saldo actual:", "Monto del adeudo ($):", "Descripción:"},
                new JComponent[]{new JLabel(c.nombre()), new JLabel(Ui.dinero(c.saldo())), monto, desc},
                () -> servicio.registrarAdeudo(c, monto.getText(), desc.getText()));
        if (ok) { Ui.info(this, "Adeudo registrado con éxito."); recargar(); }
    }

    private void pago() {
        Cliente c = seleccionado();
        if (c == null) return;
        if (c.saldo().signum() <= 0) { Ui.aviso(this, "El cliente no tiene adeudos pendientes."); return; }
        JTextField monto = Tema.campoTexto(10);
        BigDecimal[] nuevo = new BigDecimal[1];
        boolean ok = Ui.formulario(this, "Registrar pago de " + c.nombre(),
                new String[]{"Cliente:", "Saldo pendiente:", "Monto recibido ($):"},
                new JComponent[]{new JLabel(c.nombre()), new JLabel(Ui.dinero(c.saldo())), monto},
                () -> nuevo[0] = servicio.registrarPago(c, monto.getText()));
        if (ok) { Ui.info(this, "Pago registrado correctamente.\nNuevo saldo: " + Ui.dinero(nuevo[0])); recargar(); }
    }

    private void estadoCuenta() {
        Cliente c = seleccionado();
        if (c == null) return;
        Ui.intentar(this, () -> {
            ClienteService.Estado e = servicio.estadoDeCuenta(c);
            Ui.Modelo mA = new Ui.Modelo(List.of("Adeudo", "Fecha", "Venta", "Original ($)", "Pagado ($)", "Saldo ($)", "Descripción"));
            List<Object[]> fa = new ArrayList<>();
            for (Adeudo a : e.adeudos())
                fa.add(new Object[]{a.id(), a.fecha().format(Validador.FECHA_HORA), a.idVenta() == null ? "" : "#" + a.idVenta(),
                        a.montoOriginal(), a.totalPagado(), a.saldo(), a.descripcion() == null ? "" : a.descripcion()});
            mA.set(fa);

            Ui.Modelo mP = new Ui.Modelo(List.of("Pago", "Adeudo", "Fecha", "Monto ($)", "Recibió"));
            List<Object[]> fp = new ArrayList<>();
            for (PagoAdeudo p : e.pagos())
                fp.add(new Object[]{p.id(), p.idAdeudo(), p.fecha().format(Validador.FECHA_HORA), p.monto(), p.usuario()});
            mP.set(fp);

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBackground(Tema.CREMA);
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel cardEncabezado = Tema.tarjeta();
            cardEncabezado.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 2));
            JLabel lblNom = new JLabel("Cliente: " + e.cliente().nombre());
            lblNom.setFont(Tema.FUENTE_TITULO);
            lblNom.setForeground(Tema.TEXTO_TITULO);

            JLabel lblSal = new JLabel("Saldo pendiente: " + Ui.dinero(e.cliente().saldo()));
            lblSal.setFont(Tema.FUENTE_TITULO);
            lblSal.setForeground(e.cliente().saldo().signum() > 0 ? Tema.TERRACOTA : Tema.VERDE_OSCURO);

            cardEncabezado.add(lblNom);
            cardEncabezado.add(lblSal);

            JPanel centro = new JPanel(new GridLayout(2, 1, 0, 10));
            centro.setOpaque(false);

            JTable tAdeudos = Ui.tabla(mA);
            JScrollPane s1 = Tema.scrollTabla(tAdeudos);
            s1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Tema.GRIS_BORDE), "Adeudos registrados"));

            JTable tPagos = Ui.tabla(mP);
            JScrollPane s2 = Tema.scrollTabla(tPagos);
            s2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Tema.GRIS_BORDE), "Pagos y abonos recibidos"));

            centro.add(s1);
            centro.add(s2);
            centro.setPreferredSize(new Dimension(740, 380));

            panel.add(cardEncabezado, BorderLayout.NORTH);
            panel.add(centro, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(this, panel, "Estado de cuenta – " + c.nombre(), JOptionPane.PLAIN_MESSAGE);
        });
    }
}
