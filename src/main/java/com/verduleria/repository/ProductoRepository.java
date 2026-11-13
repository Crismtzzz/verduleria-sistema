package com.verduleria.repository;

import com.verduleria.model.Producto;
import com.verduleria.util.Sql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoRepository {
    private static final String BASE =
            "SELECT p.id_producto, p.id_categoria, c.nombre AS categoria, p.nombre, p.unidad_medida, "
          + "p.precio_venta, p.activo, i.existencia, i.stock_minimo "
          + "FROM productos p "
          + "LEFT JOIN categorias c ON c.id_categoria = p.id_categoria "
          + "JOIN inventario i ON i.id_producto = p.id_producto ";

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id_producto"), Sql.entero(rs, "id_categoria"), rs.getString("categoria"),
                rs.getString("nombre"), rs.getString("unidad_medida"), rs.getBigDecimal("precio_venta"),
                rs.getBoolean("activo"), rs.getBigDecimal("existencia"), rs.getBigDecimal("stock_minimo"));
    }

    public List<Producto> listar(Connection c, String texto, boolean soloActivos) throws SQLException {
        String sql = BASE + "WHERE (? = '' OR p.nombre LIKE ?) " + (soloActivos ? "AND p.activo = TRUE " : "")
                + "ORDER BY p.nombre";
        List<Producto> lista = new ArrayList<>();
        String t = texto == null ? "" : texto.trim();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t);
            ps.setString(2, "%" + t + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Producto> buscar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE p.id_producto = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public boolean existeNombre(Connection c, String nombre, Integer excluirId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM productos WHERE LOWER(nombre) = LOWER(?) AND id_producto <> ?")) {
            ps.setString(1, nombre);
            ps.setInt(2, excluirId == null ? -1 : excluirId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    /** Inserta el producto y su registro de inventario (existencia 0). Devuelve el id generado. */
    public int insertar(Connection c, Producto p) throws SQLException {
        int id;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO productos (id_categoria, nombre, unidad_medida, precio_venta, activo) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            if (p.idCategoria() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, p.idCategoria());
            ps.setString(2, p.nombre());
            ps.setString(3, p.unidad());
            ps.setBigDecimal(4, p.precioVenta());
            ps.setBoolean(5, p.activo());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); id = k.getInt(1); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO inventario (id_producto, existencia, stock_minimo) VALUES (?, 0, ?)")) {
            ps.setInt(1, id);
            ps.setBigDecimal(2, p.stockMinimo());
            ps.executeUpdate();
        }
        return id;
    }

    public void actualizar(Connection c, Producto p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE productos SET id_categoria = ?, nombre = ?, unidad_medida = ?, precio_venta = ?, activo = ? "
              + "WHERE id_producto = ?")) {
            if (p.idCategoria() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, p.idCategoria());
            ps.setString(2, p.nombre());
            ps.setString(3, p.unidad());
            ps.setBigDecimal(4, p.precioVenta());
            ps.setBoolean(5, p.activo());
            ps.setInt(6, p.id());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO inventario (id_producto, existencia, stock_minimo) VALUES (?, 0, ?) "
              + "ON DUPLICATE KEY UPDATE stock_minimo = VALUES(stock_minimo)")) {
            ps.setInt(1, p.id());
            ps.setBigDecimal(2, p.stockMinimo());
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(Connection c, int id, boolean activo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE productos SET activo = ? WHERE id_producto = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Verifica si el producto tiene referencias históricas en detalle_venta, detalle_compra
     * o movimientos_inventario. No modifica ningún dato.
     *
     * @return true si existe al menos un registro referenciando el producto.
     */
    public boolean tieneHistorial(Connection c, int id) throws SQLException {
        String sql = "SELECT 1 FROM detalle_venta WHERE id_producto = ? LIMIT 1 "
                   + "UNION ALL "
                   + "SELECT 1 FROM detalle_compra WHERE id_producto = ? LIMIT 1 "
                   + "UNION ALL "
                   + "SELECT 1 FROM movimientos_inventario WHERE id_producto = ? LIMIT 1 "
                   + "LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setInt(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Elimina el registro de inventario y luego el producto dentro de la misma transacción.
     * Prerequisito: llamar a {@link #tieneHistorial} antes para garantizar la integridad referencial.
     */
    public void eliminar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM inventario WHERE id_producto = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM productos WHERE id_producto = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
