package com.verduleria.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.verduleria.model.Usuario;
import com.verduleria.service.AuthService;
import com.verduleria.util.DatosException;

/**
 * CU-AUT-01: pantalla de inicio de sesión rediseñada para Verdulería Jiménez.
 * En el primer uso solicita crear el administrador.
 */
public class LoginFrame extends JFrame {
    private final AuthService auth = new AuthService();
    private final JTextField txtUsuario = Tema.campoTexto(18);
    private final JPasswordField txtPassword = Tema.campoPassword(18);

    public LoginFrame() {
        super("Verdulería Jiménez – Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Tema.inicializar();

        // Contenedor raíz con GridBagLayout para centrar la tarjeta vertical y horizontalmente
        JPanel raiz = new JPanel(new GridBagLayout());
        raiz.setBackground(Tema.CREMA);
        raiz.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Tarjeta central de login
        JPanel card = Tema.tarjeta();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 36, 28, 36));
        card.setPreferredSize(new Dimension(380, 470));
        card.setMaximumSize(new Dimension(380, 470));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Logo e isotipo
        JLabel lblLogo = new JLabel(Iconos.logo(56));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Verdulería Jiménez");
        lblTitulo.setFont(Tema.FUENTE_TITULO_GRANDE);
        lblTitulo.setForeground(Tema.TEXTO_TITULO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Sistema de Gestión Comercial");
        lblSub.setFont(Tema.FUENTE_SUBTITULO);
        lblSub.setForeground(Tema.TEXTO_SECUNDARIO);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Separador sutil
        JSeparator sep = new JSeparator();
        sep.setForeground(Tema.GRIS_BORDE);
        sep.setMaximumSize(new Dimension(300, 2));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subpanel del formulario: agrupa campos y etiquetas con alineación izquierda consistente
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(300, 140));

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(Tema.FUENTE_NEGRITA);
        lblUser.setForeground(Tema.TEXTO_PRINCIPAL);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsuario.setMaximumSize(new Dimension(300, 36));
        txtUsuario.setPreferredSize(new Dimension(300, 36));

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(Tema.FUENTE_NEGRITA);
        lblPass.setForeground(Tema.TEXTO_PRINCIPAL);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(300, 36));
        txtPassword.setPreferredSize(new Dimension(300, 36));

        txtUsuario.addActionListener(e -> txtPassword.requestFocusInWindow());

        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtUsuario);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtPassword);

        // Botón de ingreso
        JButton btnIngresar = Tema.botonPrimario("  Iniciar Sesión  ", Iconos.confirmar(16, Color.WHITE), this::ingresar);
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIngresar.setMaximumSize(new Dimension(300, 40));
        btnIngresar.setPreferredSize(new Dimension(300, 40));
        getRootPane().setDefaultButton(btnIngresar);

        JLabel lblFooter = new JLabel("Verdulería Jiménez © 2026 • Acceso seguro");
        lblFooter.setFont(Tema.FUENTE_PEQUE);
        lblFooter.setForeground(Tema.TEXTO_SECUNDARIO);
        lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ensamblado en la tarjeta
        card.add(lblLogo);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitulo);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(14));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        card.add(formPanel);
        card.add(Box.createVerticalStrut(20));

        card.add(btnIngresar);
        card.add(Box.createVerticalStrut(18));
        card.add(lblFooter);

        // Agregar tarjeta al contenedor raíz centrado
        raiz.add(card);
        setContentPane(raiz);
        pack();
        setSize(480, 570);
        setMinimumSize(new Dimension(460, 550));
        setLocationRelativeTo(null);
        setResizable(false);

        SwingUtilities.invokeLater(this::verificarPrimerUso);
    }

    private void verificarPrimerUso() {
        try {
            if (auth.requiereConfiguracionInicial()) crearAdministrador();
        } catch (DatosException e) {
            Ui.error(this, "No se pudo conectar con la base de datos.\n"
                    + "Verifique que MySQL esté en ejecución y que db.properties sea correcto.\n\n" + e.getMessage());
        }
    }

    private void crearAdministrador() {
        JTextField nombre = Tema.campoTexto(20);
        JTextField usuario = Tema.campoTexto(20);
        JPasswordField pass = Tema.campoPassword(20);
        JPasswordField pass2 = Tema.campoPassword(20);
        Ui.info(this, "Es la primera vez que se usa el sistema.\nCree la cuenta del administrador.");
        boolean ok = Ui.formulario(this, "Crear administrador",
                new String[]{"Nombre completo:", "Usuario:", "Contraseña (mín. 8):", "Repetir contraseña:"},
                new JComponent[]{nombre, usuario, pass, pass2}, () -> {
                    String p = new String(pass.getPassword());
                    if (!p.equals(new String(pass2.getPassword())))
                        throw new com.verduleria.util.NegocioException("Las contraseñas no coinciden.");
                    auth.crearAdministradorInicial(nombre.getText(), usuario.getText(), p);
                });
        if (ok) {
            txtUsuario.setText(usuario.getText().trim());
            Ui.info(this, "Administrador creado. Inicie sesión con sus credenciales.");
        }
    }

    private void ingresar() {
        Usuario[] u = new Usuario[1];
        boolean ok = Ui.intentar(this,
                () -> u[0] = auth.iniciarSesion(txtUsuario.getText(), new String(txtPassword.getPassword())));
        txtPassword.setText("");
        if (!ok) return;
        new MainFrame(u[0]).setVisible(true);
        dispose();
    }
}
