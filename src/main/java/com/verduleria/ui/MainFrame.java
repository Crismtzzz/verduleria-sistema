package com.verduleria.ui;

import com.verduleria.model.Permiso;
import com.verduleria.model.Usuario;
import com.verduleria.service.AuthService;
import com.verduleria.util.Sesion;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana principal rediseñada con barra de navegación lateral verde oscuro (#184D36),
 * encabezado dinámico con información del usuario y contenedor con CardLayout.
 * Conserva estrictamente todos los atajos de teclado y la validación de permisos por rol.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);
    private final MenuLateral menuLateral;

    private final JLabel lblModuloTitulo = new JLabel("Inicio");
    private final JLabel lblModuloSubtitulo = new JLabel("Resumen general y métricas del negocio");
    private final JLabel lblModuloIcono = new JLabel(Iconos.hoja(24, Tema.VERDE_OSCURO));

    private final Map<String, JComponent> modulos = new LinkedHashMap<>();
    private final List<String> ordenModulos = new ArrayList<>();

    public MainFrame(Usuario usuario) {
        super("Verdulería Jiménez – Sistema de Gestión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1220, 760);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.CREMA);

        // --- Configuración de Paneles y Permisos ---
        modulos.put("inicio", new InicioPanel());
        if (Sesion.tiene(Permiso.VENTAS)) modulos.put("ventas", new VentasPanel());
        if (Sesion.tiene(Permiso.COMPRAS)) modulos.put("compras", new ComprasPanel());
        if (Sesion.tiene(Permiso.PRODUCTOS_VER)) modulos.put("productos", new ProductosPanel());
        if (Sesion.tiene(Permiso.INVENTARIO_VER)) modulos.put("inventario", new InventarioPanel());
        if (Sesion.tiene(Permiso.CLIENTES)) modulos.put("clientes", new ClientesPanel());
        if (Sesion.tiene(Permiso.REPORTES)) modulos.put("reportes", new ReportesPanel());
        if (Sesion.tiene(Permiso.USUARIOS)) modulos.put("usuarios", new UsuariosPanel());

        panelContenido.setOpaque(false);
        for (Map.Entry<String, JComponent> e : modulos.entrySet()) {
            panelContenido.add(e.getValue(), e.getKey());
            ordenModulos.add(e.getKey());
        }

        // --- Menú Lateral Izquierdo ---
        menuLateral = new MenuLateral(usuario, this::mostrarModulo, this::cerrarSesion);

        // --- Encabezado Superior Derecho ---
        JPanel encabezado = crearEncabezado(usuario);

        // --- Estructura Central ---
        JPanel areaPrincipal = new JPanel(new BorderLayout());
        areaPrincipal.setOpaque(false);
        areaPrincipal.add(encabezado, BorderLayout.NORTH);
        areaPrincipal.add(panelContenido, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(menuLateral, BorderLayout.WEST);
        add(areaPrincipal, BorderLayout.CENTER);

        configurarAtajosGlobales();

        // Mostrar Inicio por defecto y recargar sus datos
        mostrarModulo("inicio");
    }

    private JPanel crearEncabezado(Usuario usuario) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 10, 20));

        // Bloque izquierdo: Icono + Título + Subtítulo
        JPanel izquierda = new JPanel(new BorderLayout(10, 0));
        izquierda.setOpaque(false);
        lblModuloTitulo.setFont(Tema.FUENTE_TITULO_GRANDE);
        lblModuloTitulo.setForeground(Tema.TEXTO_TITULO);

        lblModuloSubtitulo.setFont(Tema.FUENTE_SUBTITULO);
        lblModuloSubtitulo.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(lblModuloTitulo);
        textos.add(lblModuloSubtitulo);

        izquierda.add(lblModuloIcono, BorderLayout.WEST);
        izquierda.add(textos, BorderLayout.CENTER);

        // Bloque derecho: Botón Ayuda F1 + Pastilla de Usuario
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);

        JButton btnAyuda = Tema.botonSecundario("Atajos (F1)", Iconos.inicio(16, Tema.VERDE_OSCURO),
                () -> AyudaTecladoDialog.mostrar(this));

        // Pastilla del usuario actual (avatar + nombre + rol)
        JPanel userPill = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.BLANCO_TARJETA);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(Tema.GRIS_BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userPill.setOpaque(false);
        userPill.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel iconUser = new JLabel(Iconos.usuarios(18, Tema.VERDE_OSCURO));
        JPanel txtUser = new JPanel(new GridLayout(2, 1));
        txtUser.setOpaque(false);
        JLabel lblU = new JLabel("Usuario: " + usuario.usuario());
        lblU.setFont(Tema.FUENTE_NEGRITA);
        lblU.setForeground(Tema.TEXTO_PRINCIPAL);
        JLabel lblR = new JLabel(usuario.rol());
        lblR.setFont(Tema.FUENTE_PEQUE_BOLD);
        lblR.setForeground(Tema.VERDE_SALVIA);
        txtUser.add(lblU);
        txtUser.add(lblR);

        userPill.add(iconUser, BorderLayout.WEST);
        userPill.add(txtUser, BorderLayout.CENTER);

        derecha.add(btnAyuda);
        derecha.add(userPill);

        p.add(izquierda, BorderLayout.WEST);
        p.add(derecha, BorderLayout.EAST);
        return p;
    }

    public void mostrarModulo(String id) {
        if (!modulos.containsKey(id)) return;
        cardLayout.show(panelContenido, id);
        menuLateral.seleccionar(id);

        // Actualizar textos del encabezado
        switch (id) {
            case "inicio" -> {
                lblModuloTitulo.setText("Inicio");
                lblModuloSubtitulo.setText("Resumen del negocio y métricas comerciales");
                lblModuloIcono.setIcon(Iconos.hoja(24, Tema.VERDE_OSCURO));
            }
            case "ventas" -> {
                lblModuloTitulo.setText("Ventas");
                lblModuloSubtitulo.setText("Registro y cobro de productos en punto de venta");
                lblModuloIcono.setIcon(Iconos.ventas(24, Tema.VERDE_OSCURO));
            }
            case "compras" -> {
                lblModuloTitulo.setText("Compras");
                lblModuloSubtitulo.setText("Registro de compras al mayoreo en la Central de Abastos");
                lblModuloIcono.setIcon(Iconos.compras(24, Tema.VERDE_OSCURO));
            }
            case "productos" -> {
                lblModuloTitulo.setText("Productos");
                lblModuloSubtitulo.setText("Administra el catálogo de productos de la verdulería");
                lblModuloIcono.setIcon(Iconos.productos(24, Tema.VERDE_OSCURO));
            }
            case "inventario" -> {
                lblModuloTitulo.setText("Inventario");
                lblModuloSubtitulo.setText("Control de existencias, mermas y movimientos de almacén");
                lblModuloIcono.setIcon(Iconos.inventario(24, Tema.VERDE_OSCURO));
            }
            case "clientes" -> {
                lblModuloTitulo.setText("Clientes");
                lblModuloSubtitulo.setText("Administración de clientes frecuentes y control de adeudos");
                lblModuloIcono.setIcon(Iconos.clientes(24, Tema.VERDE_OSCURO));
            }
            case "reportes" -> {
                lblModuloTitulo.setText("Reportes");
                lblModuloSubtitulo.setText("Consultas ejecutivas y exportación de información comercial");
                lblModuloIcono.setIcon(Iconos.reportes(24, Tema.VERDE_OSCURO));
            }
            case "usuarios" -> {
                lblModuloTitulo.setText("Usuarios");
                lblModuloSubtitulo.setText("Administración de usuarios y permisos del personal");
                lblModuloIcono.setIcon(Iconos.usuarios(24, Tema.VERDE_OSCURO));
            }
        }

        JComponent comp = modulos.get(id);
        if (comp instanceof Recargable r) {
            r.recargar();
        }
    }

    private void configurarAtajosGlobales() {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // F1: Ayuda de atajos
        Ui.atajo(getRootPane(), KeyEvent.VK_F1, "ayudaF1", () -> AyudaTecladoDialog.mostrar(this));

        // F12: Cerrar sesión
        Ui.atajo(getRootPane(), KeyEvent.VK_F12, "cerrarSesionF12", this::cerrarSesion);

        // Atajos numéricos Ctrl + 1..8
        for (int i = 0; i < ordenModulos.size() && i < 9; i++) {
            final String modId = ordenModulos.get(i);
            int key = KeyEvent.VK_1 + i;
            Ui.atajo(getRootPane(), key, ctrl, "irMod" + (i + 1), () -> mostrarModulo(modId));
        }

        // Mnemónicos por letra: Alt + V, Alt + C, Alt + P, etc.
        Ui.atajo(getRootPane(), KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK, "altV", () -> mostrarModulo("ventas"));
        Ui.atajo(getRootPane(), KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK, "altC", () -> mostrarModulo("compras"));
        Ui.atajo(getRootPane(), KeyEvent.VK_P, KeyEvent.ALT_DOWN_MASK, "altP", () -> mostrarModulo("productos"));
        Ui.atajo(getRootPane(), KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK, "altI", () -> mostrarModulo("inventario"));
        Ui.atajo(getRootPane(), KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK, "altL", () -> mostrarModulo("clientes"));
        Ui.atajo(getRootPane(), KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK, "altR", () -> mostrarModulo("reportes"));
        Ui.atajo(getRootPane(), KeyEvent.VK_U, KeyEvent.ALT_DOWN_MASK, "altU", () -> mostrarModulo("usuarios"));

        // Ctrl + Tab / Ctrl + Shift + Tab para ciclar módulos
        Ui.atajo(getRootPane(), KeyEvent.VK_TAB, ctrl, "sigModulo", () -> {
            int idx = ordenModulos.indexOf(menuLateral.getSeleccionado());
            if (idx >= 0 && !ordenModulos.isEmpty()) {
                mostrarModulo(ordenModulos.get((idx + 1) % ordenModulos.size()));
            }
        });
        Ui.atajo(getRootPane(), KeyEvent.VK_TAB, ctrl | KeyEvent.SHIFT_DOWN_MASK, "antModulo", () -> {
            int idx = ordenModulos.indexOf(menuLateral.getSeleccionado());
            if (idx >= 0 && !ordenModulos.isEmpty()) {
                mostrarModulo(ordenModulos.get((idx - 1 + ordenModulos.size()) % ordenModulos.size()));
            }
        });
    }

    private void cerrarSesion() {
        if (!Ui.confirmar(this, "¿Desea cerrar la sesión actual?")) return;
        new AuthService().cerrarSesion();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
