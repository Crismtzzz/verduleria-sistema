package com.verduleria.model;

import java.util.List;

/** Resultado tabular genérico (columnas + filas) usado por reportes y bitácoras. */
public record Tabla(List<String> columnas, List<Object[]> filas) { }
