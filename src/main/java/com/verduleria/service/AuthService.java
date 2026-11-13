package com.verduleria.service;

import com.verduleria.model.Rol;
import com.verduleria.model.Usuario;
import com.verduleria.repository.UsuarioRepository;
import com.verduleria.repository.UsuarioRepository.Credencial;
import com.verduleria.util.*;
import java.util.Optional;

/** RF-AUT-01, 02, 05: inicio y cierre de sesión. */
public class AuthService {
    private final UsuarioRepository repo = new UsuarioRepository();

    /** ¿No existe ningún usuario? Entonces hay que crear el administrador inicial. */
    public boolean requiereConfiguracionInicial() {
        return Tx.ejecutar(repo::contar) == 0;
    }

    public void crearAdministradorInicial(String nombre, String usuario, String password) {
        String n = Validador.texto(nombre, "Nombre", 100);
        String u = UsuarioService.validarNombreUsuario(usuario);
        UsuarioService.validarPassword(password);
        Tx.ejecutar(c -> {
            if (repo.contar(c) > 0) throw new NegocioException("Ya existen usuarios registrados.");
            Rol admin = repo.rolPorNombre(c, Usuario.ADMINISTRADOR)
                    .orElseThrow(() -> new NegocioException("Falta el rol Administrador. Ejecute sql/schema.sql."));
            return repo.insertar(c, admin.id(), n, u, PasswordUtil.hash(password));
        });
    }

    public Usuario iniciarSesion(String usuario, String password) {
        String u = usuario == null ? "" : usuario.trim();
        if (u.isEmpty() || password == null || password.isEmpty())
            throw new NegocioException("Ingrese su usuario y contraseña.");
        Optional<Credencial> cred = Tx.ejecutar(c -> repo.buscarPorUsuario(c, u));
        // Mismo mensaje si el usuario no existe o la contraseña es incorrecta.
        if (cred.isEmpty() || !PasswordUtil.verificar(password, cred.get().hash()))
            throw new NegocioException("Usuario o contraseña incorrectos.");
        if (!cred.get().usuario().activo())
            throw new NegocioException("La cuenta no está habilitada. Contacte al administrador.");
        Sesion.iniciar(cred.get().usuario());
        return cred.get().usuario();
    }

    public void cerrarSesion() { Sesion.cerrar(); }
}
