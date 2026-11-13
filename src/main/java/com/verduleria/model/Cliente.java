package com.verduleria.model;

import java.math.BigDecimal;

public record Cliente(int id, String nombre, String telefono, boolean activo, BigDecimal saldo) {
    @Override public String toString() { return nombre; }
}
