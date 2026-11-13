package com.verduleria.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Renglón de una compra o venta (producto, cantidad y precio unitario). */
public record Linea(int idProducto, String producto, String unidad, BigDecimal cantidad, BigDecimal precio) {
    /** Cantidad × precio, redondeado a dos decimales (igual que la columna generada subtotal). */
    public BigDecimal subtotal() {
        return cantidad.multiply(precio).setScale(2, RoundingMode.HALF_UP);
    }
}
