package com.verduleria.model;

import java.math.BigDecimal;

/** Producto del catálogo junto con su existencia y stock mínimo. id == null: aún no registrado. */
public record Producto(Integer id, Integer idCategoria, String categoria, String nombre, String unidad,
                       BigDecimal precioVenta, boolean activo, BigDecimal existencia, BigDecimal stockMinimo) {
    public static final String[] UNIDADES = {"kg", "pieza", "manojo", "otro"};

    public boolean bajoStock() {
        return existencia != null && stockMinimo != null && existencia.compareTo(stockMinimo) <= 0;
    }

    @Override public String toString() { return nombre + " (" + unidad + ")"; }
}
