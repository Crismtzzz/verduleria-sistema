package com.verduleria.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Diálogo modal con la guía completa de comandos y atajos de teclado de Verdulería Jiménez.
 */
public final class AyudaTecladoDialog extends JDialog {

    private AyudaTecladoDialog(Window parent) {
        super(parent, "Comandos y Atajos de Teclado – Verdulería Jiménez", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 560);
        setLocationRelativeTo(parent);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Tema.CREMA);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Encabezado
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        head.setOpaque(false);
        head.add(new JLabel(Iconos.hoja(24, Tema.VERDE_OSCURO)));
        JLabel tit = new JLabel("Guía Rápida de Atajos de Teclado");
        tit.setFont(Tema.FUENTE_TITULO);
        tit.setForeground(Tema.TEXTO_TITULO);
        head.add(tit);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Tema.FUENTE_NEGRITA);
        tabs.addTab("Navegación General", panelTabla(new String[][]{
                {"F1", "Abrir esta ventana de ayuda y comandos de teclado"},
                {"Ctrl + 1 al 8", "Ir directamente al módulo (1: Inicio, 2: Ventas, 3: Compras, 4: Productos, ...)"},
                {"Alt + V", "Módulo de Ventas"},
                {"Alt + C", "Módulo de Compras"},
                {"Alt + P", "Módulo de Productos"},
                {"Alt + I", "Módulo de Inventario"},
                {"Alt + L", "Módulo de Clientes y Adeudos"},
                {"Alt + R", "Módulo de Reportes"},
                {"Alt + U", "Módulo de Usuarios y Permisos"},
                {"Ctrl + Tab", "Avanzar al siguiente módulo"},
                {"Ctrl + Shift + Tab", "Regresar al módulo anterior"},
                {"F12", "Cerrar sesión de forma rápida"}
        }));

        tabs.addTab("Ventas (Punto de Venta)", panelTabla(new String[][]{
                {"F2", "Enfocar el selector de Producto"},
                {"F3", "Enfocar el campo de Cantidad"},
                {"Enter (en Cantidad)", "Agregar el producto al carrito de venta"},
                {"F4", "Alternar casilla de «Venta a crédito (fiado)»"},
                {"F6", "Enfocar el selector de Cliente"},
                {"F7", "Enfocar el campo de «Efectivo recibido» para registrar el cobro"},
                {"Enter (en Efectivo)", "Confirmar la venta directamente desde el campo de efectivo"},
                {"Supr / Delete", "Quitar el renglón seleccionado del carrito"},
                {"F5", "Limpiar carrito / Cancelar venta actual"},
                {"F9  o  Ctrl + Enter", "Confirmar la venta y descontar existencias de inventario"}
        }));

        tabs.addTab("Compras", panelTabla(new String[][]{
                {"F2", "Enfocar el selector de Producto"},
                {"F3", "Enfocar el campo de Cantidad"},
                {"F4", "Enfocar el campo de Precio de compra"},
                {"Enter (en Precio)", "Agregar el renglón a la compra"},
                {"Supr / Delete", "Quitar el renglón seleccionado de la lista"},
                {"F5", "Limpiar lista / Cancelar compra actual"},
                {"F9  o  Ctrl + Enter", "Confirmar la compra e incrementar existencias en inventario"}
        }));

        tabs.addTab("Productos e Inventario", panelTabla(new String[][]{
                {"F2", "Nuevo producto (en Productos) / Registrar merma (en Inventario)"},
                {"F3", "Enfocar el campo de búsqueda de producto"},
                {"F4", "Modificar / actualizar precio del producto seleccionado"},
                {"F8", "Eliminar definitivamente el producto seleccionado (solo Administrador)"},
                {"Alt + D", "Activar o desactivar producto seleccionado"},
                {"F5", "Recargar lista y existencias actualizadas"},
                {"Alt + M", "Registrar merma (atajo rápido en inventario)"}
        }));

        tabs.addTab("Clientes, Reportes y Usuarios", panelTabla(new String[][]{
                {"F2 (Clientes)", "Registrar nuevo cliente"},
                {"F3 (Clientes)", "Modificar cliente seleccionado"},
                {"F4 (Clientes)", "Registrar adeudo al cliente seleccionado"},
                {"F6 (Clientes)", "Registrar pago / abono de adeudo"},
                {"F7 (Clientes)", "Consultar estado de cuenta detallado"},
                {"F5 (Reportes)", "Ejecutar y consultar el reporte seleccionado"},
                {"Ctrl + S (Reportes)", "Exportar los datos del reporte a archivo CSV"},
                {"F2 (Usuarios)", "Registrar nuevo usuario"},
                {"F4 (Usuarios)", "Habilitar o deshabilitar usuario seleccionado"},
                {"F6 (Usuarios)", "Restablecer contraseña de usuario"}
        }));

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.setOpaque(false);
        JButton cerrar = Tema.botonPrimario("Cerrar (Esc)", Iconos.cancelar(16, Color.WHITE), this::dispose);
        sur.add(cerrar);

        Ui.atajo(getRootPane(), KeyEvent.VK_ESCAPE, "cerrarAyuda", this::dispose);
        Ui.atajo(getRootPane(), KeyEvent.VK_F1, "cerrarAyudaF1", this::dispose);

        content.add(head, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        content.add(sur, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private static JScrollPane panelTabla(String[][] datos) {
        String[] cols = {"Tecla / Atajo", "Acción que realiza"};
        DefaultTableModel m = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        Tema.configurarTablaModerna(t);
        t.setRowHeight(28);
        t.getColumnModel().getColumn(0).setPreferredWidth(210);
        t.getColumnModel().getColumn(1).setPreferredWidth(550);
        return Tema.scrollTabla(t);
    }

    public static void mostrar(Window parent) {
        new AyudaTecladoDialog(parent).setVisible(true);
    }
}
