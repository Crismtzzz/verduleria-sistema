package com.verduleria.repository;

import com.verduleria.model.Tabla;
import com.verduleria.util.Sql;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventarioRepository {

    /** Bloquea la fila de inventario (SELECT ... FOR UPDATE) y devuelve la existencia actual. */
    public Optional<BigDecimal> existenciaBloqueada(Connection c, int idProducto) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT existencia FROM inventario WHERE id_producto = ? FOR UPDATE")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getBigDecimal(1)) : Optional.empty();
            }
        }
    }

    /** Suma (delta positivo) o resta (delta negativo) a la existencia. */
    public void ajustar(Connection c, int idProducto, BigDecimal delta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE inventario SET existencia = existencia + ? WHERE id_producto = ?")) {
            ps.setBigDecimal(1, delta);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    public void registrarMovimiento(Connection c, int idProducto, int idUsuario, LocalDateTime fecha, String tipo,
                                    BigDecimal cantidad, Integer idCompra, Integer idVenta, String motivo)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO movimientos_inventario (id_producto, id_usuario, fecha, tipo, cantidad, id_compra, id_venta, motivo) "
              + "VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idUsuario);
            ps.setTimestamp(3, Sql.ts(fecha));
            ps.setString(4, tipo);
            ps.setBigDecimal(5, cantidad);
            if (idCompra == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, idCompra);
            if (idVenta == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, idVenta);
            ps.setString(8, motivo);
            ps.executeUpdate();
        }
    }

    public Tabla movimientos(Connection c, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT m.fecha, p.nombre AS producto, m.tipo, m.cantidad, u.nombre AS usuario, "
                   + "COALESCE(m.motivo, '') AS motivo "
                   + "FROM movimientos_inventario m "
                   + "JOIN productos p ON p.id_producto = m.id_producto "
                   + "JOIN usuarios u ON u.id_usuario = m.id_usuario "
                   + "WHERE (? IS NULL OR m.fecha >= ?) AND (? IS NULL OR m.fecha < ?) "
                   + "ORDER BY m.fecha DESC, m.id_movimiento DESC";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Timestamp d = desde == null ? null : Sql.ts(desde);
            Timestamp h = hasta == null ? null : Sql.ts(hasta.plusDays(1));
            ps.setTimestamp(1, d); ps.setTimestamp(2, d);
            ps.setTimestamp(3, h); ps.setTimestamp(4, h);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getTimestamp("fecha").toLocalDateTime().format(
                            com.verduleria.util.Validador.FECHA_HORA),
                            rs.getString("producto"), rs.getString("tipo"), rs.getBigDecimal("cantidad"),
                            rs.getString("usuario"), rs.getString("motivo")});
                }
            }
        }
        return new Tabla(List.of("Fecha", "Producto", "Tipo", "Cantidad", "Usuario", "Motivo"), filas);
    }
}
