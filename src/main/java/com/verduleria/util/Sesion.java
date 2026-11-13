package com.verduleria.util;

import com.verduleria.model.Permiso;
import com.verduleria.model.Usuario;

/** Usuario autenticado de la sesión actual (RF-AUT-02). */
public final class Sesion {
    private static Usuario actual;

    private Sesion() { }

    public static void iniciar(Usuario u) { actual = u; }
    public static void cerrar() { actual = null; }
    public static Usuario usuario() { return actual; }
    public static boolean activa() { return actual != null; }

    public static boolean tiene(Permiso p) { return actual != null && actual.tiene(p); }

    /** RF-AUT-03: impide ejecutar funciones sin autorización, también en la capa de negocio. */
    public static Usuario exigir(Permiso p) {
        if (actual == null) throw new NegocioException("Debe iniciar sesión para realizar esta operación.");
        if (!actual.tiene(p)) throw new NegocioException("No tiene autorización para realizar esta operación.");
        return actual;
    }
}
