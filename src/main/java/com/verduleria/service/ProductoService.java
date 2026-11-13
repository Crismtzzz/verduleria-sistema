package com.verduleria.service;

import com.verduleria.model.*;
import com.verduleria.repository.*;
import com.verduleria.util.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** RF-PRO-01 a RF-PRO-06. */
public class ProductoService {
    private final ProductoRepository repo = new ProductoRepository();
    private final CategoriaRepository categorias = new CategoriaRepository();

    public List<Categoria> categorias() {
        Sesion.exigir(Permiso.PRODUCTOS_VER);
        return Tx.ejecutar(categorias::listar);
    }

    public List<Producto> listar(String texto, boolean soloActivos) {
        Sesion.exigir(Permiso.PRODUCTOS_VER);
        return Tx.ejecutar(c -> repo.listar(c, texto, soloActivos));
    }

    /** Productos disponibles para compras y ventas (solo activos). Requiere el permiso de la operación. */
    public List<Producto> activosPara(Permiso permisoOperacion) {
        Sesion.exigir(permisoOperacion);
        return Tx.ejecutar(c -> repo.listar(c, "", true));
    }

    public int registrar(String nombre, Categoria categoria, String unidad, String precio, String stockMinimo) {
        Sesion.exigir(Permiso.PRODUCTOS_EDITAR);
        Producto p = armar(null, nombre, categoria, unidad, precio, stockMinimo, true);
        return Tx.ejecutar(c -> {
            if (repo.existeNombre(c, p.nombre(), null))
                throw new NegocioException("Ya existe un producto llamado «" + p.nombre() + "».");
            return repo.insertar(c, p);
        });
    }

    public void modificar(int id, String nombre, Categoria categoria, String unidad, String precio,
                          String stockMinimo, boolean activo) {
        Sesion.exigir(Permiso.PRODUCTOS_EDITAR);
        Producto p = armar(id, nombre, categoria, unidad, precio, stockMinimo, activo);
        Tx.ejecutar(c -> {
            if (repo.buscar(c, id).isEmpty()) throw new NegocioException("El producto ya no existe.");
            if (repo.existeNombre(c, p.nombre(), id))
                throw new NegocioException("Ya existe otro producto llamado «" + p.nombre() + "».");
            repo.actualizar(c, p);
            return null;
        });
    }

    /** RF-PRO-05: un producto desactivado no puede agregarse a nuevas ventas. */
    public void cambiarActivo(int id, boolean activo) {
        Sesion.exigir(Permiso.PRODUCTOS_EDITAR);
        Tx.ejecutar(c -> { repo.cambiarActivo(c, id, activo); return null; });
    }

    /**
     * RF-PRO-06: elimina definitivamente un producto creado por error o duplicado.
     * Solo disponible para Administradores (PRODUCTOS_EDITAR + rol sin restricción de cajero).
     * <p>Reglas:
     * <ul>
     *   <li>El producto debe existir.</li>
     *   <li>No debe tener referencias en detalle_venta, detalle_compra ni movimientos_inventario.</li>
     *   <li>Su existencia en inventario debe ser cero.</li>
     * </ul>
     * Si alguna regla falla lanza {@link NegocioException} con un mensaje descriptivo.
     */
    public void eliminar(int id) {
        Sesion.exigir(Permiso.PRODUCTOS_EDITAR);
        // Solo administradores pueden eliminar (cajero solo tiene PRODUCTOS_VER)
        Usuario u = Sesion.usuario();
        if (!"Administrador".equalsIgnoreCase(u.rol())) {
            throw new NegocioException("Solo el Administrador puede eliminar productos definitivamente.");
        }
        Tx.ejecutar(c -> {
            Producto p = repo.buscar(c, id)
                    .orElseThrow(() -> new NegocioException("El producto ya no existe en la base de datos."));

            if (p.existencia() != null && p.existencia().signum() > 0) {
                throw new NegocioException(
                        "No se puede eliminar «" + p.nombre() + "» porque tiene "
                        + p.existencia().stripTrailingZeros().toPlainString() + " " + p.unidad()
                        + " en existencia. Registre una merma para reducir el stock a cero primero.");
            }

            if (repo.tieneHistorial(c, id)) {
                throw new NegocioException(
                        "No se puede eliminar «" + p.nombre() + "» porque tiene registros históricos "
                        + "(ventas, compras o movimientos de inventario). "
                        + "Se recomienda desactivarlo para que no aparezca en nuevas ventas.");
            }

            repo.eliminar(c, id);
            return null;
        });
    }

    private Producto armar(Integer id, String nombre, Categoria cat, String unidad, String precio,
                           String stockMinimo, boolean activo) {
        String n = Validador.texto(nombre, "Nombre", 120);
        if (unidad == null || !Arrays.asList(Producto.UNIDADES).contains(unidad))
            throw new NegocioException("Seleccione una unidad de medida válida.");
        BigDecimal pv = Validador.noNegativo(Validador.decimal(precio, "Precio de venta", 2), "Precio de venta");
        BigDecimal sm = Validador.noNegativo(Validador.decimal(
                stockMinimo == null || stockMinimo.isBlank() ? "0" : stockMinimo, "Stock mínimo", 3), "Stock mínimo");
        return new Producto(id, cat == null ? null : cat.id(), cat == null ? null : cat.nombre(), n, unidad, pv, activo,
                BigDecimal.ZERO, sm);
    }
}
