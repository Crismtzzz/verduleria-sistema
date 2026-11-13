package com.verduleria.service;

import com.verduleria.model.Permiso;
import com.verduleria.model.Tabla;
import com.verduleria.repository.ReporteRepository;
import com.verduleria.util.*;
import java.time.LocalDateTime;

/** RF-REP-01 a RF-REP-05. */
public class ReporteService {
    private final ReporteRepository repo = new ReporteRepository();

    private static void validarRango(LocalDateTime d, LocalDateTime h) {
        if (d != null && h != null && d.isAfter(h))
            throw new NegocioException("La fecha inicial no puede ser posterior a la fecha final.");
    }

    public Tabla ventas(LocalDateTime d, LocalDateTime h) {
        Sesion.exigir(Permiso.REPORTES); validarRango(d, h);
        return Tx.ejecutar(c -> repo.ventas(c, d, h));
    }

    public Tabla inventario() {
        Sesion.exigir(Permiso.REPORTES);
        return Tx.ejecutar(repo::inventario);
    }

    public Tabla compras(LocalDateTime d, LocalDateTime h) {
        Sesion.exigir(Permiso.REPORTES); validarRango(d, h);
        return Tx.ejecutar(c -> repo.compras(c, d, h));
    }

    public Tabla movimientos(LocalDateTime d, LocalDateTime h) {
        Sesion.exigir(Permiso.REPORTES); validarRango(d, h);
        return Tx.ejecutar(c -> new com.verduleria.repository.InventarioRepository().movimientos(c, d, h));
    }

    public Tabla bajoStock() {
        Sesion.exigir(Permiso.REPORTES);
        return Tx.ejecutar(repo::bajoStock);
    }

    public Tabla adeudos() {
        Sesion.exigir(Permiso.REPORTES);
        return Tx.ejecutar(repo::adeudos);
    }
}
