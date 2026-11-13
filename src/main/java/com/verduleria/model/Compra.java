package com.verduleria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Compra(int id, String usuario, LocalDateTime fecha, BigDecimal total, String estado) { }
