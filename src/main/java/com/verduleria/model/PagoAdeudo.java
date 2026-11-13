package com.verduleria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoAdeudo(int id, int idAdeudo, LocalDateTime fecha, BigDecimal monto, String usuario) { }
