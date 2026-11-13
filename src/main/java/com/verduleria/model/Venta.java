package com.verduleria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Venta(int id, String usuario, String cliente, LocalDateTime fecha, BigDecimal total, String estado,
                    String metodoPago, BigDecimal efectivoRecibido, BigDecimal cambio) {

    public Venta(int id, String usuario, String cliente, LocalDateTime fecha, BigDecimal total, String estado) {
        this(id, usuario, cliente, fecha, total, estado, "EFECTIVO", total, BigDecimal.ZERO);
    }
}
