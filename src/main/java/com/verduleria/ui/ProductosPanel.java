package com.verduleria.ui;

import com.verduleria.model.*;
import com.verduleria.service.ProductoService;
import com.verduleria.service.VentaService;
import com.verduleria.util.Sesion;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Módulo de productos rediseñado estilo Dashboard (CU-PRO-01, 02, 03).
 * Incluye tarjetas de métricas reales, barra de búsqueda con filtros y tabla moderna con insignias.
 */
public class ProductosPanel extends JPanel implements Recargable {
    private final ProductoService servicio = new ProductoService();
    private final VentaService ventaService = new VentaService();

    // Tarjetas informativas superiores (según Imagen 1)
    private final Tarjeta cardActivos = new Tarjeta(Iconos.productos(22, Tema.VERDE_OSCURO), "0", "Productos activos", "En catálogo", Tema.VERDE_SALVIA_CLARO);
    private final Tarjeta cardBajoStock = new Tarjeta(Iconos.alerta(22, Tema.TERRACOTA), "0", "Productos con stock bajo", "Por reabastecer", Tema.TERRACOTA_CLARO);
    private final Tarjeta cardVentasHoy = new Tarjeta(Iconos.ventas(22, Tema.VERDE_OSCURO), "0", "Ventas de hoy", "Transacciones", Tema.VERDE_SALVIA_CLARO);
    private final Tarjeta cardTotalVentas = new Tarjeta(Iconos.dinero(22, Tema.VERDE_OSCURO), "$0.00", "Total de ventas hoy", "Ingresos del día", Tema.VERDE_SALVIA_CLARO);

    private final Ui.Modelo modelo = new Ui.Modelo(List.of("ID", "Producto", "Categoría", "Unidad",
            "Precio venta ($)", "Stock mínimo", "Existencia", "Activo"));
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtBuscar = Tema.campoTexto(16);
    private final JCheckBox soloActivos = new JCheckBox("Solo activos", false);
    private List<Producto> datos = new ArrayList<>();

    public ProductosPanel() {
        super(new BorderLayout(0, 12));
        setBackground(Tema.CREMA);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Fila superior de Tarjetas KPI
        JPanel panelCards = new JPanel(new GridLayout(1, 4, 14, 0));
        panelCards.setOpaque(false);
        panelCards.add(cardActivos);
        panelCards.add(cardBajoStock);
        panelCards.add(cardVentasHoy);
        panelCards.add(cardTotalVentas);

        // 2. Barra de búsqueda y acciones (dentro de una tarjeta suave)
        JPanel panelAcciones = Tema.tarjeta();
        panelAcciones.setLayout(new BorderLayout(10, 0));
        panelAcciones.setBorder(new EmptyBorder(10, 14, 10, 14));

        txtBuscar.setToolTipText("Escriba para buscar por nombre o código (Atajo: F3)");
        soloActivos.setFont(Tema.FUENTE_NORMAL);
        soloActivos.setOpaque(false);
        soloActivos.setFocusPainted(false);

        JLabel lblBuscar = new JLabel("Buscar producto:");
        lblBuscar.setFont(Tema.FUENTE_NEGRITA);
        lblBuscar.setForeground(Tema.TEXTO_PRINCIPAL);

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);
        izquierda.add(lblBuscar);
        izquierda.add(txtBuscar);
        izquierda.add(soloActivos);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);

        boolean edita = Sesion.tiene(Permiso.PRODUCTOS_EDITAR);
        if (edita) {
            JButton btnNuevo = Tema.botonPrimario("Nuevo producto (F2)", Iconos.mas(16, Color.WHITE), () -> abrirFormulario(null));
            JButton btnModificar = Tema.botonSecundario("Modificar (F4)", Iconos.editar(16, Tema.TEXTO_PRINCIPAL), this::modificar);
            JButton btnAlternar = Tema.botonSecundario("Activar / Desactivar (Alt+D)", Iconos.alternar(16, Tema.TEXTO_PRINCIPAL), this::alternarActivo);
            JButton btnEliminar = Tema.botonTerracota("Eliminar (F8)", Iconos.papelera(16, Color.WHITE), this::eliminar);

            derecha.add(btnNuevo);
            derecha.add(btnModificar);
            derecha.add(btnAlternar);
            derecha.add(btnEliminar);
        }

        panelAcciones.add(izquierda, BorderLayout.WEST);
        panelAcciones.add(derecha, BorderLayout.EAST);

        txtBuscar.addActionListener(e -> recargar());
        soloActivos.addActionListener(e -> recargar());

        // 3. Tabla en contenedor tipo tarjeta
        Tema.aplicarBadge(tabla, "Activo");
        JScrollPane scrollTabla = Tema.scrollTabla(tabla);

        JPanel contenedorCentro = new JPanel(new BorderLayout(0, 12));
        contenedorCentro.setOpaque(false);
        contenedorCentro.add(panelAcciones, BorderLayout.NORTH);
        contenedorCentro.add(scrollTabla, BorderLayout.CENTER);

        add(panelCards, BorderLayout.NORTH);
        add(contenedorCentro, BorderLayout.CENTER);

        if (edita) {
            tabla.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) modificar();
                }
            });
        }
        configurarAtajos(edita);
    }

    private void configurarAtajos(boolean edita) {
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F3, "focoBuscar", () -> {
            txtBuscar.requestFocusInWindow();
            txtBuscar.selectAll();
        });
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F5, "recargarProductos", this::recargar);

        if (edita) {
            Ui.atajo(this, java.awt.event.KeyEvent.VK_F2, "nuevoProductoF2", () -> abrirFormulario(null));
            int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
            Ui.atajo(this, java.awt.event.KeyEvent.VK_N, ctrl, "nuevoProductoCtrlN", () -> abrirFormulario(null));
            Ui.atajo(this, java.awt.event.KeyEvent.VK_F4, "modificarProductoF4", this::modificar);
            Ui.atajo(this, java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_DOWN_MASK, "alternarActivoAltD", this::alternarActivo);
            Ui.atajo(this, java.awt.event.KeyEvent.VK_F8, "eliminarProductoF8", this::eliminar);
        }
    }

    @Override public void recargar() {
        Ui.intentar(this, () -> {
            datos = servicio.listar(txtBuscar.getText(), soloActivos.isSelected());
            List<Object[]> filas = new ArrayList<>();
            for (Producto p : datos) {
                filas.add(new Object[]{p.id(), p.nombre(), p.categoria() == null ? "" : p.categoria(), p.unidad(),
                        p.precioVenta(), p.stockMinimo(), p.existencia(), p.activo() ? "Sí" : "No"});
            }
            modelo.set(filas);
            actualizarMetricas();
        });
    }

    private void actualizarMetricas() {
        try {
            long activos = datos.stream().filter(Producto::activo).count();
            long bajoStock = datos.stream().filter(Producto::bajoStock).count();
            cardActivos.setValor(String.valueOf(activos));
            cardBajoStock.setValor(String.valueOf(bajoStock));

            // Ventas de hoy
            LocalDate hoy = LocalDate.now();
            LocalDateTime d = hoy.atStartOfDay();
            LocalDateTime h = hoy.atTime(LocalTime.MAX);
            List<Venta> vHoy = ventaService.historial(d, h);
            BigDecimal totalVentas = vHoy.stream().map(Venta::total).reduce(BigDecimal.ZERO, BigDecimal::add);
            cardVentasHoy.setValor(String.valueOf(vHoy.size()));
            cardTotalVentas.setValor(Ui.dinero(totalVentas));
        } catch (Exception ignored) { }
    }

    private Producto seleccionado() {
        int i = Ui.seleccionada(tabla);
        if (i < 0) { Ui.aviso(this, "Seleccione un producto de la tabla."); return null; }
        return datos.get(i);
    }

    private void modificar() {
        Producto p = seleccionado();
        if (p != null) abrirFormulario(p);
    }

    private void alternarActivo() {
        Producto p = seleccionado();
        if (p == null) return;
        String accion = p.activo() ? "desactivar" : "activar";
        if (!Ui.confirmar(this, "¿Desea " + accion + " el producto «" + p.nombre() + "»?")) return;
        if (Ui.intentar(this, () -> servicio.cambiarActivo(p.id(), !p.activo()))) recargar();
    }

    private void eliminar() {
        Producto p = seleccionado();
        if (p == null) return;
        boolean ok = Ui.confirmar(this,
                "<html><b>¿Eliminar definitivamente el producto?</b><br><br>"
                + "<b>Nombre:</b> " + p.nombre() + "<br>"
                + "<b>ID:</b> " + p.id() + "<br><br>"
                + "<span style='color:#B84A2F'>Esta acción es irreversible.<br>"
                + "Si el producto tiene historial, se rechazará automáticamente.</span></html>");
        if (!ok) return;
        if (Ui.intentar(this, () -> servicio.eliminar(p.id()))) {
            Ui.aviso(this, "El producto «" + p.nombre() + "» fue eliminado correctamente.");
            recargar();
        }
    }

    private void abrirFormulario(Producto existente) {
        JTextField nombre = Tema.campoTexto(22);
        JComboBox<Categoria> cat = new JComboBox<>();
        cat.addItem(new Categoria(0, "(Sin categoría)"));
        Ui.intentar(this, () -> servicio.categorias().forEach(cat::addItem));
        JComboBox<String> unidad = new JComboBox<>(Producto.UNIDADES);
        JTextField precio = Tema.campoTexto(10);
        JTextField minimo = Tema.campoTexto(10);
        minimo.setText("0");
        JCheckBox activo = new JCheckBox("Producto activo (disponible para ventas)", true);
        activo.setFont(Tema.FUENTE_NORMAL);
        activo.setOpaque(false);

        if (existente != null) {
            nombre.setText(existente.nombre());
            unidad.setSelectedItem(existente.unidad());
            precio.setText(existente.precioVenta().toPlainString());
            minimo.setText(Ui.cantidad(existente.stockMinimo()));
            activo.setSelected(existente.activo());
            for (int i = 0; i < cat.getItemCount(); i++) {
                if (existente.idCategoria() != null && cat.getItemAt(i).id() == existente.idCategoria()) {
                    cat.setSelectedIndex(i);
                }
            }
        }
        boolean ok = Ui.formulario(this, existente == null ? "Registrar producto" : "Modificar producto",
                new String[]{"Nombre:", "Categoría:", "Unidad de medida:", "Precio de venta ($):", "Stock mínimo:", ""},
                new JComponent[]{nombre, cat, unidad, precio, minimo, activo}, () -> {
                    Categoria c = (Categoria) cat.getSelectedItem();
                    Categoria sel = c == null || c.id() == 0 ? null : c;
                    if (existente == null) {
                        servicio.registrar(nombre.getText(), sel, (String) unidad.getSelectedItem(), precio.getText(), minimo.getText());
                    } else {
                        servicio.modificar(existente.id(), nombre.getText(), sel, (String) unidad.getSelectedItem(),
                                precio.getText(), minimo.getText(), activo.isSelected());
                    }
                });
        if (ok) recargar();
    }
}
