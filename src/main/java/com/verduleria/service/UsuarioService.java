package com.verduleria.service;

import com.verduleria.model.*;
import com.verduleria.repository.UsuarioRepository;
import com.verduleria.util.*;
import java.util.List;

/** RF-AUT-04: administración de usuarios (solo administrador). */
public class UsuarioService {
    private final UsuarioRepository repo = new UsuarioRepository();

    public static String validarNombreUsuario(String usuario) {
        String u = Validador.texto(usuario, "Usuario", 60);
        if (u.length() < 3) throw new NegocioException("El usuario debe tener al menos 3 caracteres.");
        if (!u.matches("[A-Za-z0-9._-]+"))
            throw new NegocioException("El usuario solo puede contener letras, números, punto, guion y guion bajo.");
        return u;
    }

    public static void validarPassword(String password) {
        if (password == null || password.length() < 8)
            throw new NegocioException("La contraseña debe tener al menos 8 caracteres.");
        if (password.length() > 100) throw new NegocioException("La contraseña es demasiado larga.");
    }

    public List<Usuario> listar() {
        Sesion.exigir(Permiso.USUARIOS);
        return Tx.ejecutar(repo::listar);
    }

    public List<Rol> roles() {
        Sesion.exigir(Permiso.USUARIOS);
        return Tx.ejecutar(repo::roles);
    }

    public int registrar(String nombre, String usuario, String password, Rol rol) {
        Sesion.exigir(Permiso.USUARIOS);
        String n = Validador.texto(nombre, "Nombre", 100);
        String u = validarNombreUsuario(usuario);
        validarPassword(password);
        if (rol == null) throw new NegocioException("Seleccione un rol.");
        return Tx.ejecutar(c -> {
            if (repo.buscarPorUsuario(c, u).isPresent())
                throw new NegocioException("El nombre de usuario «" + u + "» ya existe. Elija uno diferente.");
            return repo.insertar(c, rol.id(), n, u, PasswordUtil.hash(password));
        });
    }

    public void cambiarActivo(Usuario objetivo, boolean activo) {
        Usuario yo = Sesion.exigir(Permiso.USUARIOS);
        if (!activo && objetivo.id() == yo.id())
            throw new NegocioException("No puede deshabilitar su propia cuenta.");
        Tx.ejecutar(c -> { repo.cambiarActivo(c, objetivo.id(), activo); return null; });
    }

    public void restablecerPassword(Usuario objetivo, String nueva) {
        Sesion.exigir(Permiso.USUARIOS);
        validarPassword(nueva);
        Tx.ejecutar(c -> { repo.cambiarPassword(c, objetivo.id(), PasswordUtil.hash(nueva)); return null; });
    }
}
