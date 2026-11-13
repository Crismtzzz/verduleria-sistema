package com.verduleria.service;

import com.verduleria.model.*;
import com.verduleria.repository.*;
import com.verduleria.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** RF-INV-01, 04, 05, 06, 07. */
public class InventarioService {
    private final InventarioRepository repo = new InventarioRepository();
    private final ProductoRepository productos = new ProductoRepository();

    /** Existencias de todos los productos; Producto.bajoStock() identifica los que alcanzaron el mínimo. */
    public List<Producto> existencias(String texto) {
        Sesion.exigir(Permiso.INVENTARIO_VER);
        return Tx.ejecutar(c -> productos.listar(c, texto, false));
    }

    public Tabla movimientos(LocalDateTime desde, LocalDateTime hasta) {
        Sesion.exigir(Permiso.INVENTARIO_VER);
        return Tx.ejecutar(c -> repo.movimientos(c, desde, hasta));
    }

    /** CU-INV-03: la merma descuenta existencias y registra el movimiento, o no hace nada. */
    public void registrarMerma(Producto producto, String cantidadTxt, String motivo) {
        Usuario u = Sesion.exigir(Permiso.INVENTARIO_MERMA);
        if (producto == null || producto.id() == null) throw new NegocioException("Seleccione un producto.");
        BigDecimal cantidad = Validador.positivo(Validador.decimal(cantidadTxt, "Cantidad", 3), "Cantidad");
        String m = Validador.textoOpcional(motivo, "Motivo", 255);
        Tx.ejecutar(c -> {
            BigDecimal existencia = repo.existenciaBloqueada(c, producto.id())
                    .orElseThrow(() -> new NegocioException("El producto no existe."));
            if (cantidad.compareTo(existencia) > 0)
                throw new NegocioException("La merma (" + cantidad + ") supera la existencia disponible (" + existencia + ").");
            repo.ajustar(c, producto.id(), cantidad.negate());
            repo.registrarMovimiento(c, producto.id(), u.id(), LocalDateTime.now(), "merma", cantidad, null, null, m);
            return null;
        });
    }
}
