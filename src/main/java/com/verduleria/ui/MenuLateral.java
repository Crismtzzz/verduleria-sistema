package com.verduleria.ui;

import com.verduleria.model.Permiso;
import com.verduleria.model.Usuario;
import com.verduleria.util.Sesion;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Menú lateral de navegación vertical en verde oscuro (#184D36) con selector
 * resaltado en verde salvia (#83A96B), información de usuario y botón de cerrar sesión.
 */
public class MenuLateral extends JPanel {

    public record ItemMenu(String id, String texto, Icon icono, Permiso permiso) { }

    private final List<BotonModulo> botones = new ArrayList<>();
    private final Consumer<String> alSeleccionar;
    private String idSeleccionado = "inicio";

    public MenuLateral(Usuario usuario, Consumer<String> alSeleccionar, Runnable alCerrarSesion) {
        this.alSeleccionar = alSeleccionar;
        setPreferredSize(new Dimension(215, 680));
        setBackground(Tema.VERDE_OSCURO);
        setLayout(new BorderLayout());

        // --- Encabezado: Logo y Nombre ---
        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBorder(new EmptyBorder(22, 16, 20, 16));

        JLabel lblLogo = new JLabel(Iconos.logo(38));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Verdulería");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Jiménez 🌿");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblSubtitulo.setForeground(Tema.VERDE_SALVIA_CLARO);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Sistema de Gestión");
        lblSub.setFont(Tema.FUENTE_PEQUE);
        lblSub.setForeground(Tema.TEXTO_MENU);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        cabecera.add(lblLogo);
        cabecera.add(Box.createVerticalStrut(6));
        cabecera.add(lblTitulo);
        cabecera.add(lblSubtitulo);
        cabecera.add(Box.createVerticalStrut(2));
        cabecera.add(lblSub);

        // --- Lista de Módulos ---
        JPanel listaModulos = new JPanel();
        listaModulos.setOpaque(false);
        listaModulos.setLayout(new BoxLayout(listaModulos, BoxLayout.Y_AXIS));
        listaModulos.setBorder(new EmptyBorder(6, 12, 10, 12));

        List<ItemMenu> items = List.of(
                new ItemMenu("inicio", "Inicio", Iconos.inicio(18, Color.WHITE), null),
                new ItemMenu("ventas", "Ventas", Iconos.ventas(18, Color.WHITE), Permiso.VENTAS),
                new ItemMenu("compras", "Compras", Iconos.compras(18, Color.WHITE), Permiso.COMPRAS),
                new ItemMenu("productos", "Productos", Iconos.productos(18, Color.WHITE), Permiso.PRODUCTOS_VER),
                new ItemMenu("inventario", "Inventario", Iconos.inventario(18, Color.WHITE), Permiso.INVENTARIO_VER),
                new ItemMenu("clientes", "Clientes", Iconos.clientes(18, Color.WHITE), Permiso.CLIENTES),
                new ItemMenu("reportes", "Reportes", Iconos.reportes(18, Color.WHITE), Permiso.REPORTES),
                new ItemMenu("usuarios", "Usuarios", Iconos.usuarios(18, Color.WHITE), Permiso.USUARIOS)
        );

        for (ItemMenu item : items) {
            if (item.permiso() == null || Sesion.tiene(item.permiso())) {
                BotonModulo btn = new BotonModulo(item);
                botones.add(btn);
                listaModulos.add(btn);
                listaModulos.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane scroll = new JScrollPane(listaModulos);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // --- Pie de página: Usuario y Cerrar sesión ---
        JPanel pie = new JPanel();
        pie.setOpaque(false);
        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));
        pie.setBorder(new EmptyBorder(12, 14, 18, 14));

        // Tarjeta pequeña del usuario
        JPanel cardUser = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x10, 0x3D, 0x29));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardUser.setOpaque(false);
        cardUser.setBorder(new EmptyBorder(6, 8, 6, 8));
        JLabel lblUserIcon = new JLabel(Iconos.usuarios(18, Tema.VERDE_SALVIA));
        JPanel infoUser = new JPanel(new GridLayout(2, 1));
        infoUser.setOpaque(false);
        JLabel lblNom = new JLabel(usuario.nombre());
        lblNom.setFont(Tema.FUENTE_NEGRITA);
        lblNom.setForeground(Color.WHITE);
        JLabel lblRol = new JLabel(usuario.rol());
        lblRol.setFont(Tema.FUENTE_PEQUE);
        lblRol.setForeground(Tema.TEXTO_MENU);
        infoUser.add(lblNom);
        infoUser.add(lblRol);
        cardUser.add(lblUserIcon, BorderLayout.WEST);
        cardUser.add(infoUser, BorderLayout.CENTER);

        // Botón cerrar sesión
        JButton btnSalir = new JButton("Cerrar sesión", Iconos.cerrarSesion(16, Color.WHITE)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0x23, 0x69, 0x4B) : new Color(0x13, 0x43, 0x2E));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(0x2E, 0x75, 0x55));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSalir.setFont(Tema.FUENTE_NORMAL);
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setContentAreaFilled(false);
        btnSalir.setOpaque(false);
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnSalir.setBorder(new EmptyBorder(6, 10, 6, 10));
        btnSalir.addActionListener(e -> alCerrarSesion.run());

        pie.add(cardUser);
        pie.add(Box.createVerticalStrut(10));
        pie.add(btnSalir);

        add(cabecera, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);

        // Seleccionar inicio por defecto
        seleccionar("inicio");
    }

    public void seleccionar(String id) {
        this.idSeleccionado = id;
        for (BotonModulo b : botones) {
            b.setSeleccionado(b.item.id().equalsIgnoreCase(id));
        }
        repaint();
    }

    public String getSeleccionado() { return idSeleccionado; }

    private class BotonModulo extends JButton {
        private final ItemMenu item;
        private boolean seleccionado = false;

        BotonModulo(ItemMenu item) {
            super(item.texto(), item.icono());
            this.item = item;
            setFont(Tema.FUENTE_NORMAL);
            setForeground(Tema.TEXTO_MENU);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(12);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(190, 40));
            setBorder(new EmptyBorder(8, 14, 8, 14));

            addActionListener(e -> {
                seleccionar(item.id());
                alSeleccionar.accept(item.id());
            });
        }

        void setSeleccionado(boolean sel) {
            this.seleccionado = sel;
            setFont(sel ? Tema.FUENTE_NEGRITA : Tema.FUENTE_NORMAL);
            setForeground(sel ? Color.WHITE : Tema.TEXTO_MENU);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (seleccionado) {
                // Píldora verde salvia #83A96B destacada
                g2.setColor(Tema.VERDE_SALVIA);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (getModel().isRollover()) {
                g2.setColor(Tema.VERDE_OSCURO_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

