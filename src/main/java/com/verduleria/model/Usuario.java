package com.verduleria.model;

import java.util.EnumSet;
import java.util.Set;

public record Usuario(int id, int idRol, String rol, String nombre, String usuario, boolean activo) {
    public static final String ADMINISTRADOR = "Administrador";

    private static final Set<Permiso> PERMISOS_CAJERO = EnumSet.of(
            Permiso.PRODUCTOS_VER, Permiso.INVENTARIO_VER,
            Permiso.COMPRAS, Permiso.VENTAS, Permiso.CLIENTES);

    /** El administrador tiene todos los permisos; el personal operativo (Cajero), un subconjunto. */
    public boolean tiene(Permiso p) {
        if (!activo) return false;
        if (ADMINISTRADOR.equalsIgnoreCase(rol)) return true;
        return PERMISOS_CAJERO.contains(p);
    }
}
