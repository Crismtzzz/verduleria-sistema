package com.verduleria.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Capa de persistencia: fábrica de conexiones JDBC hacia MySQL local. */
public final class Db {
    private static final Properties PROPS = cargar();

    private Db() { }

    private static Properties cargar() {
        Properties p = new Properties();
        Path externo = Path.of("db.properties");
        try {
            if (Files.exists(externo)) {
                try (InputStream in = Files.newInputStream(externo)) { p.load(in); }
            } else {
                try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
                    if (in == null) throw new IllegalStateException("No se encontró db.properties");
                    p.load(in);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }
        return p;
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password"));
    }
}
