package com.verduleria.model;

public record Rol(int id, String nombre) {
    @Override public String toString() { return nombre; }
}
