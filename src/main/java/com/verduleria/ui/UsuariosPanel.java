package com.verduleria.ui;

import com.verduleria.model.Rol;
import com.verduleria.model.Usuario;
import com.verduleria.service.UsuarioService;
import com.verduleria.util.NegocioException;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Administración de usuarios y permisos (CU-AUT-02).
 * Barra de acciones en tarjeta, estados con insignias visuales (Activo/Inactivo).
 */
public class UsuariosPanel extends JPanel implements Recargable {
    private final UsuarioService servicio = new UsuarioService();
    private final Ui.Modelo modelo = new Ui.Modelo(List.of("ID", "Nombre", "Usuario", "Rol", "Estado"));
    private final JTable tabla = Ui.tabla(modelo);
    private List<Usuario> datos = new ArrayList<>();

    public UsuariosPanel() {
        super(new BorderLayout(0, 12));
        setBackground(Tema.CREMA);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Barra de acciones en tarjeta
        JPanel cardBarra = Tema.tarjeta();
        cardBarra.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardBarra.setBorder(new EmptyBorder(8, 14, 8, 14));

        JButton btnNuevo = Tema.botonPrimario("+ Registrar usuario (F2)", Iconos.mas(16, Color.WHITE), this::nuevo);
        JButton btnAlternar = Tema.botonSecundario("Habilitar / Deshabilitar (F4)", Iconos.alternar(16, Tema.TEXTO_PRINCIPAL), this::alternar);
        JButton btnPass = Tema.botonSecundario("Restablecer contraseña (F6)", Iconos.editar(16, Tema.TEXTO_PRINCIPAL), this::restablecer);

        cardBarra.add(btnNuevo);
        cardBarra.add(btnAlternar);
        cardBarra.add(btnPass);

        // Tabla con insignias visuales
        Tema.aplicarBadge(tabla, "Estado");
        JScrollPane scroll = Tema.scrollTabla(tabla);

        add(cardBarra, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        configurarAtajos();
    }

    private void configurarAtajos() {
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F2, "nuevoUsuarioF2", this::nuevo);
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        Ui.atajo(this, java.awt.event.KeyEvent.VK_N, ctrl, "nuevoUsuarioCtrlN", this::nuevo);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F4, "alternarUsuarioF4", this::alternar);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F6, "restablecerPassF6", this::restablecer);
        Ui.atajo(this, java.awt.event.KeyEvent.VK_F5, "recargarUsuariosF5", this::recargar);
    }

    @Override public void recargar() {
        Ui.intentar(this, () -> {
            datos = servicio.listar();
            List<Object[]> filas = new ArrayList<>();
            for (Usuario u : datos)
                filas.add(new Object[]{u.id(), u.nombre(), u.usuario(), u.rol(), u.activo() ? "Activo" : "Inactivo"});
            modelo.set(filas);
        });
    }

    private Usuario seleccionado() {
        int i = Ui.seleccionada(tabla);
        if (i < 0) { Ui.aviso(this, "Seleccione un usuario de la tabla."); return null; }
        return datos.get(i);
    }

    private void nuevo() {
        JTextField nombre = Tema.campoTexto(22);
        JTextField usuario = Tema.campoTexto(16);
        JPasswordField pass = Tema.campoPassword(16);
        JPasswordField pass2 = Tema.campoPassword(16);
        JComboBox<Rol> rol = new JComboBox<>();
        Ui.intentar(this, () -> servicio.roles().forEach(rol::addItem));
        boolean ok = Ui.formulario(this, "Registrar usuario",
                new String[]{"Nombre completo:", "Usuario:", "Rol:", "Contraseña (mín. 8):", "Repetir contraseña:"},
                new JComponent[]{nombre, usuario, rol, pass, pass2}, () -> {
                    String p = new String(pass.getPassword());
                    if (!p.equals(new String(pass2.getPassword()))) throw new NegocioException("Las contraseñas no coinciden.");
                    servicio.registrar(nombre.getText(), usuario.getText(), p, (Rol) rol.getSelectedItem());
                });
        if (ok) { Ui.info(this, "Usuario registrado con éxito."); recargar(); }
    }

    private void alternar() {
        Usuario u = seleccionado();
        if (u == null) return;
        if (!Ui.confirmar(this, (u.activo() ? "¿Deshabilitar" : "¿Habilitar") + " la cuenta «" + u.usuario() + "»?")) return;
        if (Ui.intentar(this, () -> servicio.cambiarActivo(u, !u.activo()))) recargar();
    }

    private void restablecer() {
        Usuario u = seleccionado();
        if (u == null) return;
        JPasswordField pass = Tema.campoPassword(16);
        JPasswordField pass2 = Tema.campoPassword(16);
        boolean ok = Ui.formulario(this, "Restablecer contraseña de " + u.usuario(),
                new String[]{"Nueva contraseña (mín. 8):", "Repetir contraseña:"}, new JComponent[]{pass, pass2}, () -> {
                    String p = new String(pass.getPassword());
                    if (!p.equals(new String(pass2.getPassword()))) throw new NegocioException("Las contraseñas no coinciden.");
                    servicio.restablecerPassword(u, p);
                });
        if (ok) Ui.info(this, "Contraseña actualizada exitosamente.");
    }
}
