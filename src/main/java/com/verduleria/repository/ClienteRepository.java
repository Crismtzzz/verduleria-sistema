package com.verduleria.repository;

import com.verduleria.model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteRepository {
    private static final String BASE =
            "SELECT c.id_cliente, c.nombre, c.telefono, c.activo, "
          + "COALESCE((SELECT SUM(s.saldo_pendiente) FROM vw_saldos_adeudos s WHERE s.id_cliente = c.id_cliente), 0) AS saldo "
          + "FROM clientes c ";

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"), rs.getString("telefono"),
                rs.getBoolean("activo"), rs.getBigDecimal("saldo"));
    }

    public List<Cliente> listar(Connection c, String texto, boolean soloActivos) throws SQLException {
        String t = texto == null ? "" : texto.trim();
        String sql = BASE + "WHERE (? = '' OR c.nombre LIKE ? OR COALESCE(c.telefono,'') LIKE ?) "
                + (soloActivos ? "AND c.activo = TRUE " : "") + "ORDER BY c.nombre";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t);
            ps.setString(2, "%" + t + "%");
            ps.setString(3, "%" + t + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Cliente> buscar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE c.id_cliente = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public int insertar(Connection c, String nombre, String telefono) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO clientes (nombre, telefono, activo) VALUES (?,?,TRUE)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        }
    }

    public void actualizar(Connection c, int id, String nombre, String telefono, boolean activo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE clientes SET nombre = ?, telefono = ?, activo = ? WHERE id_cliente = ?")) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setBoolean(3, activo);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }
}
