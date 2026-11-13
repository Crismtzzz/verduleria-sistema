package com.verduleria.repository;

import com.verduleria.model.Compra;
import com.verduleria.model.Linea;
import com.verduleria.util.Sql;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompraRepository {

    public int insertar(Connection c, int idUsuario, LocalDateTime fecha, BigDecimal total, String estado) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO compras (id_usuario, fecha, total, estado) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.setTimestamp(2, Sql.ts(fecha));
            ps.setBigDecimal(3, total);
            ps.setString(4, estado);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        }
    }

    public void insertarDetalle(Connection c, int idCompra, Linea l) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO detalle_compra (id_compra, id_producto, cantidad, precio_unitario) VALUES (?,?,?,?)")) {
            ps.setInt(1, idCompra);
            ps.setInt(2, l.idProducto());
            ps.setBigDecimal(3, l.cantidad());
            ps.setBigDecimal(4, l.precio());
            ps.executeUpdate();
        }
    }

    public List<Compra> listar(Connection c, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT c.id_compra, u.nombre AS usuario, c.fecha, c.total, c.estado FROM compras c "
                   + "JOIN usuarios u ON u.id_usuario = c.id_usuario "
                   + "WHERE (? IS NULL OR c.fecha >= ?) AND (? IS NULL OR c.fecha < ?) "
                   + "ORDER BY c.fecha DESC, c.id_compra DESC";
        List<Compra> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Timestamp d = desde == null ? null : Sql.ts(desde);
            Timestamp h = hasta == null ? null : Sql.ts(hasta.plusDays(1));
            ps.setTimestamp(1, d); ps.setTimestamp(2, d);
            ps.setTimestamp(3, h); ps.setTimestamp(4, h);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new Compra(rs.getInt("id_compra"), rs.getString("usuario"), Sql.fecha(rs, "fecha"),
                            rs.getBigDecimal("total"), rs.getString("estado")));
            }
        }
        return lista;
    }

    public List<Linea> detalle(Connection c, int idCompra) throws SQLException {
        List<Linea> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT d.id_producto, p.nombre, p.unidad_medida, d.cantidad, d.precio_unitario "
              + "FROM detalle_compra d JOIN productos p ON p.id_producto = d.id_producto "
              + "WHERE d.id_compra = ? ORDER BY d.id_detalle_compra")) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new Linea(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4), rs.getBigDecimal(5)));
            }
        }
        return lista;
    }
}
