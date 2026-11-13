package com.verduleria.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Selector de subpestañas estilo píldora moderna (como se observa en los módulos
 * de Ventas, Compras e Inventario de las imágenes de referencia).
 */
public class PildorasSelector extends JPanel {

    private final List<BotonPildora> botones = new ArrayList<>();
    private final Consumer<Integer> alCambiar;
    private int seleccionado = 0;

    public PildorasSelector(String[] titulos, Consumer<Integer> alCambiar) {
        this.alCambiar = alCambiar;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

        for (int i = 0; i < titulos.length; i++) {
            final int index = i;
            BotonPildora btn = new BotonPildora(titulos[i], i == 0);
            btn.addActionListener(e -> seleccionar(index));
            botones.add(btn);
            add(btn);
        }
    }

    public void seleccionar(int index) {
        this.seleccionado = index;
        for (int i = 0; i < botones.size(); i++) {
            botones.get(i).setActivo(i == index);
        }
        if (alCambiar != null) alCambiar.accept(index);
    }

    public int getSeleccionado() { return seleccionado; }

    private static class BotonPildora extends JButton {
        private boolean activo;

        BotonPildora(String texto, boolean activo) {
            super(texto);
            this.activo = activo;
            setFont(Tema.FUENTE_NEGRITA);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(6, 16, 6, 16));
            actualizarColores();
        }

        void setActivo(boolean activo) {
            this.activo = activo;
            actualizarColores();
            repaint();
        }

        private void actualizarColores() {
            setForeground(activo ? Color.WHITE : Tema.TEXTO_PRINCIPAL);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (activo) {
                g2.setColor(Tema.VERDE_OSCURO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            } else {
                g2.setColor(Tema.BLANCO_TARJETA);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(Tema.GRIS_BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

