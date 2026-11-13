package com.verduleria.util;

import com.verduleria.config.Db;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta un bloque como transacción atómica: COMMIT si todo termina bien,
 * ROLLBACK ante cualquier error (RNF-06).
 */
public final class Tx {
    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar(Connection c) throws SQLException;
    }

    private Tx() { }

    public static <T> T ejecutar(Trabajo<T> trabajo) {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException e) {
                c.rollback();
                throw new DatosException("Error de base de datos: " + e.getMessage(), e);
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo conectar con la base de datos: " + e.getMessage(), e);
        }
    }
}
