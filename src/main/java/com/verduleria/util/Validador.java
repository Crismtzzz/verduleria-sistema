package com.verduleria.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Validaciones y conversiones comunes (RNF-04). */
public final class Validador {
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter PARSER_FECHA = DateTimeFormatter.ofPattern("yyyy-M-d");
    private static final DateTimeFormatter PARSER_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-M-d H:m");

    private Validador() { }

    public static String texto(String valor, String campo, int max) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) throw new NegocioException("El campo «" + campo + "» es obligatorio.");
        if (v.length() > max) throw new NegocioException("El campo «" + campo + "» admite máximo " + max + " caracteres.");
        return v;
    }

    public static String textoOpcional(String valor, String campo, int max) {
        String v = valor == null ? "" : valor.trim();
        if (v.length() > max) throw new NegocioException("El campo «" + campo + "» admite máximo " + max + " caracteres.");
        return v.isEmpty() ? null : v;
    }

    /** Convierte texto a BigDecimal (acepta coma o punto) y valida decimales. */
    public static BigDecimal decimal(String valor, String campo, int decimales) {
        String v = valor == null ? "" : valor.trim().replace(",", ".");
        if (v.isEmpty()) throw new NegocioException("El campo «" + campo + "» es obligatorio.");
        BigDecimal d;
        try {
            d = new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new NegocioException("El campo «" + campo + "» debe ser un número válido.");
        }
        return normalizar(d, campo, decimales);
    }

    public static BigDecimal normalizar(BigDecimal d, String campo, int decimales) {
        if (d == null) throw new NegocioException("El campo «" + campo + "» es obligatorio.");
        if (d.stripTrailingZeros().scale() > decimales)
            throw new NegocioException("El campo «" + campo + "» admite máximo " + decimales + " decimales.");
        if (d.abs().compareTo(new BigDecimal("999999999")) > 0)
            throw new NegocioException("El valor de «" + campo + "» es demasiado grande.");
        return d.setScale(decimales, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal noNegativo(BigDecimal d, String campo) {
        if (d.signum() < 0) throw new NegocioException("El campo «" + campo + "» no puede ser negativo.");
        return d;
    }

    public static BigDecimal positivo(BigDecimal d, String campo) {
        if (d.signum() <= 0) throw new NegocioException("El campo «" + campo + "» debe ser mayor que cero.");
        return d;
    }

    /** Acepta "yyyy-MM-dd HH:mm" o "yyyy-MM-dd" (con 1 o 2 dígitos en mes, día u hora). */
    public static LocalDateTime fechaHora(String valor, String campo) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) throw new NegocioException("El campo «" + campo + "» es obligatorio.");
        try {
            if (!v.contains(" ") && !v.contains("T")) {
                return java.time.LocalDate.parse(v, PARSER_FECHA).atStartOfDay();
            }
            return LocalDateTime.parse(v.replace("T", " "), PARSER_FECHA_HORA);
        } catch (DateTimeParseException e) {
            throw new NegocioException("«" + campo + "» debe tener el formato AAAA-MM-DD o AAAA-MM-DD HH:mm.");
        }
    }

    /** Fecha opcional: vacía = sin filtro. */
    public static LocalDateTime fechaOpcional(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) return null;
        return fechaHora(valor, campo);
    }
}
