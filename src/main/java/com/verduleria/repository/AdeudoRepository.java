package com.verduleria.repository;

import com.verduleria.model.Adeudo;
import com.verduleria.model.PagoAdeudo;
import com.verduleria.util.Sql;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdeudoRepository {

    public int insertar(Connection c, int idCliente, Integer idVenta, LocalDateTime fecha, BigDecimal monto,
                        String descripcion) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO adeudos (id_cliente, id_venta, fecha, monto_original, descripcion) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCliente);
            if (idVenta == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, idVenta);
            ps.setTimestamp(3, Sql.ts(fecha));
            ps.setBigDecimal(4, monto);
            ps.setString(5, descripcion);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        }
    }

    /**
     * Bloquea los adeudos del cliente y devuelve los que tienen saldo pendiente,
     * del más antiguo al más reciente.
     */
    public List<Adeudo> pendientesBloqueados(Connection c, int idCliente) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_adeudo FROM adeudos WHERE id_cliente = ? FOR UPDATE")) {
            ps.setInt(1, idCliente);
            ps.executeQuery().close();
        }
        List<Adeudo> todos = listarPorCliente(c, idCliente);
        List<Adeudo> pendientes = new ArrayList<>();
        for (int i = todos.size() - 1; i >= 0; i--)   // listarPorCliente viene del más reciente al más antiguo
            if (todos.get(i).saldo().signum() > 0) pendientes.add(todos.get(i));
        return pendientes;
    }

    public List<Adeudo> listarPorCliente(Connection c, int idCliente) throws SQLException {
        List<Adeudo> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_adeudo, id_cliente, id_venta, fecha, monto_original, total_pagado, saldo_pendiente, descripcion "
              + "FROM vw_saldos_adeudos WHERE id_cliente = ? ORDER BY fecha DESC, id_adeudo DESC")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Adeudo(rs.getInt("id_adeudo"), rs.getInt("id_cliente"), Sql.entero(rs, "id_venta"),
                            Sql.fecha(rs, "fecha"), rs.getBigDecimal("monto_original"), rs.getBigDecimal("total_pagado"),
                            rs.getBigDecimal("saldo_pendiente"), rs.getString("descripcion")));
                }
            }
        }
        return lista;
    }

    public void insertarPago(Connection c, int idAdeudo, int idUsuario, LocalDateTime fecha, BigDecimal monto) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO pagos_adeudo (id_adeudo, id_usuario, fecha, monto) VALUES (?,?,?,?)")) {
            ps.setInt(1, idAdeudo);
            ps.setInt(2, idUsuario);
            ps.setTimestamp(3, Sql.ts(fecha));
            ps.setBigDecimal(4, monto);
            ps.executeUpdate();
        }
    }

    public List<PagoAdeudo> pagosPorCliente(Connection c, int idCliente) throws SQLException {
        List<PagoAdeudo> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT p.id_pago, p.id_adeudo, p.fecha, p.monto, u.nombre FROM pagos_adeudo p "
              + "JOIN adeudos a ON a.id_adeudo = p.id_adeudo JOIN usuarios u ON u.id_usuario = p.id_usuario "
              + "WHERE a.id_cliente = ? ORDER BY p.fecha DESC, p.id_pago DESC")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new PagoAdeudo(rs.getInt(1), rs.getInt(2), Sql.fecha(rs, "fecha"), rs.getBigDecimal(4), rs.getString(5)));
            }
        }
        return lista;
    }
}
