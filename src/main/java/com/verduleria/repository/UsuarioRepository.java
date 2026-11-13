package com.verduleria.repository;

import com.verduleria.model.Rol;
import com.verduleria.model.Usuario;
import com.verduleria.util.Sql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {
    public record Credencial(Usuario usuario, String hash) { }

    private static final String BASE =
            "SELECT u.id_usuario, u.id_rol, r.nombre AS rol, u.nombre, u.usuario, u.activo, u.password_hash "
          + "FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol ";

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id_usuario"), rs.getInt("id_rol"), rs.getString("rol"),
                rs.getString("nombre"), rs.getString("usuario"), rs.getBoolean("activo"));
    }

    public int contar(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public Optional<Credencial> buscarPorUsuario(Connection c, String usuario) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE u.usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Credencial(mapear(rs), rs.getString("password_hash")));
            }
        }
    }

    public List<Usuario> listar(Connection c) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(BASE + "ORDER BY u.nombre")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public int insertar(Connection c, int idRol, String nombre, String usuario, String hash) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO usuarios (id_rol, nombre, usuario, password_hash, activo) VALUES (?,?,?,?,TRUE)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idRol);
            ps.setString(2, nombre);
            ps.setString(3, usuario);
            ps.setString(4, hash);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        }
    }

    public void cambiarActivo(Connection c, int idUsuario, boolean activo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET activo = ? WHERE id_usuario = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void cambiarPassword(Connection c, int idUsuario, String hash) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public List<Rol> roles(Connection c) throws SQLException {
        List<Rol> lista = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT id_rol, nombre FROM roles ORDER BY id_rol")) {
            while (rs.next()) lista.add(new Rol(rs.getInt(1), rs.getString(2)));
        }
        return lista;
    }

    public Optional<Rol> rolPorNombre(Connection c, String nombre) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id_rol, nombre FROM roles WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new Rol(rs.getInt(1), rs.getString(2))) : Optional.empty();
            }
        }
    }
}
