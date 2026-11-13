package com.verduleria.util;

/** Error técnico de acceso a datos (JDBC). */
public class DatosException extends RuntimeException {
    public DatosException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
