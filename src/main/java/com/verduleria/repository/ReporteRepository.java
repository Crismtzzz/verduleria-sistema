package com.verduleria.repository;

import com.verduleria.model.Tabla;
import com.verduleria.util.Sql;
import com.verduleria.util.Validador;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReporteRepository {

    private Tabla ejecutar(Connection c, String sql, List<String> columnas, Object... params) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                int n = columnas.size();
                while (rs.next()) {
                    Object[] fila = new Object[n];
                    for (int i = 0; i < n; i++) {
                        Object v = rs.getObject(i + 1);
                        if (v instanceof Timestamp t) v = t.toLocalDateTime().format(Validador.FECHA_HORA);
                        else if (v instanceof java.sql.Date d) v = d.toLocalDate().format(Validador.FECHA);
                        else if (v instanceof LocalDateTime ldt) v = ldt.format(Validador.FECHA_HORA);
                        else if (v instanceof Boolean b) v = b ? "Sí" : "No";
                        fila[i] = v;
                    }
                    filas.add(fila);
                }
            }
        }
        return new Tabla(columnas, filas);
    }

    private static Timestamp desde(LocalDateTime d) { return d == null ? null : Sql.ts(d); }
    private static Timestamp hasta(LocalDateTime h) { return h == null ? null : Sql.ts(h.plusDays(1)); }

    /** RF-REP-01: ventas confirmadas, con filtro opcional por fechas. */
    public Tabla ventas(Connection c, LocalDateTime d, LocalDateTime h) throws SQLException {
        return ejecutar(c,
                "SELECT v.id_venta, v.fecha, COALESCE(cl.nombre,'(Mostrador)'), u.nombre, "
              + "(SELECT COUNT(*) FROM detalle_venta dv WHERE dv.id_venta = v.id_venta), v.total, "
              + "v.metodo_pago, v.efectivo_recibido, v.cambio "
              + "FROM ventas v JOIN usuarios u ON u.id_usuario = v.id_usuario "
              + "LEFT JOIN clientes cl ON cl.id_cliente = v.id_cliente "
              + "WHERE v.estado = 'confirmada' AND (? IS NULL OR v.fecha >= ?) AND (? IS NULL OR v.fecha < ?) "
              + "ORDER BY v.fecha DESC, v.id_venta DESC",
                List.of("Folio", "Fecha", "Cliente", "Atendió", "Conceptos", "Total", "Método", "Efectivo", "Cambio"),
                desde(d), desde(d), hasta(h), hasta(h));
    }

    /** RF-REP-02: existencias actuales. */
    public Tabla inventario(Connection c) throws SQLException {
        return ejecutar(c,
                "SELECT p.id_producto, p.nombre, COALESCE(ca.nombre,''), p.unidad_medida, i.existencia, i.stock_minimo, "
              + "CASE WHEN i.existencia <= i.stock_minimo THEN 'BAJO' ELSE 'OK' END, p.activo "
              + "FROM productos p JOIN inventario i ON i.id_producto = p.id_producto "
              + "LEFT JOIN categorias ca ON ca.id_categoria = p.id_categoria ORDER BY p.nombre",
                List.of("ID", "Producto", "Categoría", "Unidad", "Existencia", "Stock mínimo", "Estado", "Activo"));
    }

    /** RF-REP-03: historial de compras confirmadas. */
    public Tabla compras(Connection c, LocalDateTime d, LocalDateTime h) throws SQLException {
        return ejecutar(c,
                "SELECT c.id_compra, c.fecha, u.nombre, p.nombre, dc.cantidad, dc.precio_unitario, dc.subtotal, c.total "
              + "FROM compras c JOIN usuarios u ON u.id_usuario = c.id_usuario "
              + "JOIN detalle_compra dc ON dc.id_compra = c.id_compra JOIN productos p ON p.id_producto = dc.id_producto "
              + "WHERE c.estado = 'confirmada' AND (? IS NULL OR c.fecha >= ?) AND (? IS NULL OR c.fecha < ?) "
              + "ORDER BY c.fecha DESC, c.id_compra DESC, dc.id_detalle_compra",
                List.of("Folio", "Fecha", "Registró", "Producto", "Cantidad", "Precio compra", "Importe", "Total compra"),
                desde(d), desde(d), hasta(h), hasta(h));
    }

    /** RF-REP-04: productos en o bajo el stock mínimo (vista vw_productos_bajo_stock). */
    public Tabla bajoStock(Connection c) throws SQLException {
        return ejecutar(c,
                "SELECT id_producto, nombre, unidad_medida, existencia, stock_minimo FROM vw_productos_bajo_stock ORDER BY nombre",
                List.of("ID", "Producto", "Unidad", "Existencia", "Stock mínimo"));
    }

    /** Saldos pendientes por cliente (vista vw_saldos_adeudos). */
    public Tabla adeudos(Connection c) throws SQLException {
        return ejecutar(c,
                "SELECT cl.nombre, COALESCE(cl.telefono,''), SUM(s.monto_original), SUM(s.total_pagado), SUM(s.saldo_pendiente) "
              + "FROM vw_saldos_adeudos s JOIN clientes cl ON cl.id_cliente = s.id_cliente "
              + "GROUP BY cl.id_cliente, cl.nombre, cl.telefono HAVING SUM(s.saldo_pendiente) > 0 ORDER BY SUM(s.saldo_pendiente) DESC",
                List.of("Cliente", "Teléfono", "Adeudado", "Pagado", "Saldo pendiente"));
    }
}
