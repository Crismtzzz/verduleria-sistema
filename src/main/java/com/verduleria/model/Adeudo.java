package com.verduleria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Adeudo(int id, int idCliente, Integer idVenta, LocalDateTime fecha, BigDecimal montoOriginal,
                     BigDecimal totalPagado, BigDecimal saldo, String descripcion) { }
