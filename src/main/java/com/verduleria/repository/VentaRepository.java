package com.verduleria.repository;

import com.verduleria.model.Linea;
import com.verduleria.model.Venta;
import com.verduleria.util.Sql;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaRepository {

    public int insertar(Connection c, int idUsuario, Integer idCliente, LocalDateTime fecha, BigDecimal total,
                        String estado, String metodoPago, BigDecimal efectivoRecibido, BigDecimal cambio) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO ventas (id_usuario, id_cliente, fecha, total, estado, metodo_pago, efectivo_recibido, cambio) "
              + "VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            if (idCliente == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, idCliente);
            ps.setTimestamp(3, Sql.ts(fecha));
            ps.setBigDecimal(4, total);
            ps.setString(5, estado);
            ps.setString(6, metodoPago == null ? "EFECTIVO" : metodoPago);
            if (efectivoRecibido == null) ps.setNull(7, Types.DECIMAL); else ps.setBigDecimal(7, efectivoRecibido);
            if (cambio == null) ps.setNull(8, Types.DECIMAL); else ps.setBigDecimal(8, cambio);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        }
    }

    public int insertar(Connection c, int idUsuario, Integer idCliente, LocalDateTime fecha, BigDecimal total,
                        String estado) throws SQLException {
        return insertar(c, idUsuario, idCliente, fecha, total, estado, "EFECTIVO", total, BigDecimal.ZERO);
    }

    public void insertarDetalle(Connection c, int idVenta, Linea l) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario) VALUES (?,?,?,?)")) {
            ps.setInt(1, idVenta);
            ps.setInt(2, l.idProducto());
            ps.setBigDecimal(3, l.cantidad());
            ps.setBigDecimal(4, l.precio());
            ps.executeUpdate();
        }
    }

    public List<Venta> listar(Connection c, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT v.id_venta, u.nombre AS usuario, COALESCE(cl.nombre, '(Mostrador)') AS cliente, "
                   + "v.fecha, v.total, v.estado, v.metodo_pago, v.efectivo_recibido, v.cambio FROM ventas v "
                   + "JOIN usuarios u ON u.id_usuario = v.id_usuario "
                   + "LEFT JOIN clientes cl ON cl.id_cliente = v.id_cliente "
                   + "WHERE (? IS NULL OR v.fecha >= ?) AND (? IS NULL OR v.fecha < ?) "
                   + "ORDER BY v.fecha DESC, v.id_venta DESC";
        List<Venta> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Timestamp d = desde == null ? null : Sql.ts(desde);
            Timestamp h = hasta == null ? null : Sql.ts(hasta.plusDays(1));
            ps.setTimestamp(1, d); ps.setTimestamp(2, d);
            ps.setTimestamp(3, h); ps.setTimestamp(4, h);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new Venta(rs.getInt("id_venta"), rs.getString("usuario"), rs.getString("cliente"),
                            Sql.fecha(rs, "fecha"), rs.getBigDecimal("total"), rs.getString("estado"),
                            rs.getString("metodo_pago"), rs.getBigDecimal("efectivo_recibido"), rs.getBigDecimal("cambio")));
            }
        }
        return lista;
    }

    public List<Linea> detalle(Connection c, int idVenta) throws SQLException {
        List<Linea> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT d.id_producto, p.nombre, p.unidad_medida, d.cantidad, d.precio_unitario "
              + "FROM detalle_venta d JOIN productos p ON p.id_producto = d.id_producto "
              + "WHERE d.id_venta = ? ORDER BY d.id_detalle_venta")) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new Linea(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4), rs.getBigDecimal(5)));
            }
        }
        return lista;
    }
}
