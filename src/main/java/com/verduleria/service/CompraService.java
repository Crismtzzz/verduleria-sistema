package com.verduleria.service;

import com.verduleria.model.*;
import com.verduleria.repository.*;
import com.verduleria.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** RF-COM-01 a RF-COM-07. Las compras se hacen en la Central de Abastos: no hay proveedor. */
public class CompraService {
    private final CompraRepository repo = new CompraRepository();
    private final InventarioRepository inventario = new InventarioRepository();
    private final ProductoRepository productos = new ProductoRepository();

    /** Valida un renglón y lo devuelve normalizado. */
    public static Linea crearLinea(Producto p, String cantidadTxt, String precioTxt) {
        if (p == null || p.id() == null) throw new NegocioException("Seleccione un producto.");
        BigDecimal cant = Validador.positivo(Validador.decimal(cantidadTxt, "Cantidad", 3), "Cantidad");
        BigDecimal precio = Validador.noNegativo(Validador.decimal(precioTxt, "Precio de compra", 2), "Precio de compra");
        return new Linea(p.id(), p.nombre(), p.unidad(), cant, precio);
    }

    /** RF-COM-04: total = suma de los importes de los renglones. */
    public static BigDecimal total(List<Linea> lineas) {
        return lineas.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Confirma la compra en una sola transacción: compra + detalle + entrada de inventario + movimientos.
     * Si algo falla se hace ROLLBACK y no cambia nada.
     */
    public int confirmar(LocalDateTime fecha, List<Linea> lineas) {
        Usuario u = Sesion.exigir(Permiso.COMPRAS);
        if (fecha == null) throw new NegocioException("La fecha de la compra es obligatoria.");
        if (lineas == null || lineas.isEmpty()) throw new NegocioException("Agregue al menos un producto a la compra.");
        // Ordena por producto para bloquear siempre en el mismo orden (evita interbloqueos).
        List<Linea> ordenadas = new ArrayList<>(lineas);
        ordenadas.sort(Comparator.comparingInt(Linea::idProducto));
        BigDecimal total = total(ordenadas);

        return Tx.ejecutar(c -> {
            for (Linea l : ordenadas) {
                Producto p = productos.buscar(c, l.idProducto())
                        .orElseThrow(() -> new NegocioException("El producto «" + l.producto() + "» ya no existe."));
                if (!p.activo()) throw new NegocioException("El producto «" + p.nombre() + "» está desactivado.");
                inventario.existenciaBloqueada(c, l.idProducto())
                        .orElseThrow(() -> new NegocioException("No se encontró el registro de inventario del producto «" + p.nombre() + "»."));
            }
            int idCompra = repo.insertar(c, u.id(), fecha, total, "confirmada");
            for (Linea l : ordenadas) {
                repo.insertarDetalle(c, idCompra, l);
                inventario.ajustar(c, l.idProducto(), l.cantidad());                       // RF-INV-02 / RF-COM-05
                inventario.registrarMovimiento(c, l.idProducto(), u.id(), fecha, "entrada_compra",
                        l.cantidad(), idCompra, null, null);
            }
            return idCompra;
        });
    }

    public List<Compra> historial(LocalDateTime desde, LocalDateTime hasta) {
        Sesion.exigir(Permiso.COMPRAS);
        return Tx.ejecutar(c -> repo.listar(c, desde, hasta));
    }

    public List<Linea> detalle(int idCompra) {
        Sesion.exigir(Permiso.COMPRAS);
        return Tx.ejecutar(c -> repo.detalle(c, idCompra));
    }
}
