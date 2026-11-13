package com.verduleria;

import com.verduleria.ui.LoginFrame;
import com.verduleria.ui.Tema;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Tema.inicializar();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
