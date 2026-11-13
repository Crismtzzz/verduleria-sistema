package com.verduleria.ui;

import com.verduleria.model.Linea;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Diálogo de solo lectura con los renglones de una compra o venta. */
final class DetalleDialog {
    private DetalleDialog() { }

    static void mostrar(Component parent, String titulo, List<Linea> lineas, BigDecimal total, String encabezado) {
        Ui.Modelo m = new Ui.Modelo(List.of("Producto", "Unidad", "Cantidad", "Precio ($)", "Importe ($)"));
        List<Object[]> filas = new ArrayList<>();
        for (Linea l : lineas) filas.add(new Object[]{l.producto(), l.unidad(), l.cantidad(), l.precio(), l.subtotal()});
        m.set(filas);

        JTable t = Ui.tabla(m);
        JScrollPane sp = Tema.scrollTabla(t);
        sp.setPreferredSize(new Dimension(620, 260));

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Tema.CREMA);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblEnc = new JLabel(encabezado);
        lblEnc.setFont(Tema.FUENTE_NEGRITA);
        lblEnc.setForeground(Tema.TEXTO_PRINCIPAL);

        JLabel lt = new JLabel("Total: " + Ui.dinero(total), SwingConstants.RIGHT);
        lt.setFont(Tema.FUENTE_KPI_VALOR);
        lt.setForeground(Tema.VERDE_OSCURO);

        panel.add(lblEnc, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        panel.add(lt, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, panel, titulo + " – Verdulería Jiménez", JOptionPane.PLAIN_MESSAGE);
    }
}
