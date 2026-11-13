package com.verduleria.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Componente visual de tarjeta KPI / Indicador numérico del dashboard.
 * Muestra un ícono en un círculo coloreado, valor destacado en negrita y etiquetas explicativas.
 */
public class Tarjeta extends JPanel {

    private final JLabel lblValor;
    private final JLabel lblTitulo;
    private final JLabel lblSubtitulo;

    public Tarjeta(Icon icono, String valorInicial, String titulo, String subtitulo, Color colorCirculo) {
        this(icono, colorCirculo, valorInicial, titulo, subtitulo);
    }

    public Tarjeta(Icon icono, Color colorCirculo, String valorInicial, String titulo, String subtitulo) {
        setOpaque(false);
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(12, 16, 12, 16));

        // Círculo decorativo con el ícono
        JPanel circulo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorCirculo);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        circulo.setOpaque(false);
        circulo.setPreferredSize(new Dimension(48, 48));
        circulo.setLayout(new GridBagLayout());
        JLabel lblIcono = new JLabel(icono);
        circulo.add(lblIcono);

        // Bloque de texto derecho
        JPanel texto = new JPanel();
        texto.setOpaque(false);
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));

        lblValor = new JLabel(valorInicial);
        lblValor.setFont(Tema.FUENTE_KPI_VALOR);
        lblValor.setForeground(Tema.TEXTO_TITULO);

        lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.FUENTE_KPI_LABEL);
        lblTitulo.setForeground(Tema.TEXTO_PRINCIPAL);

        lblSubtitulo = new JLabel(subtitulo == null ? "" : subtitulo);
        lblSubtitulo.setFont(Tema.FUENTE_PEQUE);
        lblSubtitulo.setForeground(Tema.TEXTO_SECUNDARIO);

        texto.add(lblValor);
        texto.add(Box.createVerticalStrut(2));
        texto.add(lblTitulo);
        if (subtitulo != null && !subtitulo.isBlank()) {
            texto.add(Box.createVerticalStrut(1));
            texto.add(lblSubtitulo);
        }

        add(circulo, BorderLayout.WEST);
        add(texto, BorderLayout.CENTER);
    }

    public void setValor(String valor) {
        lblValor.setText(valor);
    }

    public void actualizar(String valor, String subtitulo) {
        lblValor.setText(valor);
        if (subtitulo != null) lblSubtitulo.setText(subtitulo);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.BLANCO_TARJETA);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.setColor(Tema.GRIS_BORDE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
}
