package com.verduleria.service;

import com.verduleria.model.*;
import com.verduleria.repository.*;
import com.verduleria.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** RF-CLI-01 a RF-CLI-04: clientes, adeudos y pagos. */
public class ClienteService {
    private final ClienteRepository repo = new ClienteRepository();
    private final AdeudoRepository adeudos = new AdeudoRepository();

    public List<Cliente> listar(String texto, boolean soloActivos) {
        Sesion.exigir(Permiso.CLIENTES);
        return Tx.ejecutar(c -> repo.listar(c, texto, soloActivos));
    }

    /** Clientes activos para elegir en una venta. */
    public List<Cliente> activosParaVenta() {
        Sesion.exigir(Permiso.VENTAS);
        return Tx.ejecutar(c -> repo.listar(c, "", true));
    }

    private static String validarTelefono(String tel) {
        String t = Validador.textoOpcional(tel, "Teléfono", 25);
        if (t != null && !t.matches("[0-9 +()\\-]{7,25}"))
            throw new NegocioException("El teléfono solo puede contener dígitos, espacios, +, - y paréntesis (mínimo 7).");
        return t;
    }

    public int registrar(String nombre, String telefono) {
        Sesion.exigir(Permiso.CLIENTES);
        String n = Validador.texto(nombre, "Nombre", 120);
        String t = validarTelefono(telefono);
        return Tx.ejecutar(c -> repo.insertar(c, n, t));
    }

    public void modificar(int id, String nombre, String telefono, boolean activo) {
        Sesion.exigir(Permiso.CLIENTES);
        String n = Validador.texto(nombre, "Nombre", 120);
        String t = validarTelefono(telefono);
        Tx.ejecutar(c -> {
            if (repo.buscar(c, id).isEmpty()) throw new NegocioException("El cliente no existe.");
            repo.actualizar(c, id, n, t, activo);
            return null;
        });
    }

    /** CU-CLI-03: registra un adeudo manual (se conserva el historial de adeudos anteriores). */
    public int registrarAdeudo(Cliente cliente, String montoTxt, String descripcion) {
        Sesion.exigir(Permiso.CLIENTES);
        if (cliente == null) throw new NegocioException("Seleccione un cliente.");
        BigDecimal monto = Validador.positivo(Validador.decimal(montoTxt, "Monto", 2), "Monto");
        String d = Validador.texto(descripcion, "Descripción", 255);
        return Tx.ejecutar(c -> {
            Cliente cl = repo.buscar(c, cliente.id())
                    .orElseThrow(() -> new NegocioException("El cliente no existe."));
            if (!cl.activo()) throw new NegocioException("El cliente «" + cl.nombre() + "» está deshabilitado.");
            return adeudos.insertar(c, cliente.id(), null, LocalDateTime.now(), monto, d);
        });
    }

    /**
     * CU-CLI-04: aplica el pago a los adeudos más antiguos primero, en una sola transacción.
     * El pago no puede superar el saldo pendiente total del cliente.
     */
    public BigDecimal registrarPago(Cliente cliente, String montoTxt) {
        Usuario u = Sesion.exigir(Permiso.CLIENTES);
        if (cliente == null) throw new NegocioException("Seleccione un cliente.");
        BigDecimal monto = Validador.positivo(Validador.decimal(montoTxt, "Monto del pago", 2), "Monto del pago");
        return Tx.ejecutar(c -> {
            List<Adeudo> pendientes = adeudos.pendientesBloqueados(c, cliente.id());
            BigDecimal saldo = pendientes.stream().map(Adeudo::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (saldo.signum() <= 0) throw new NegocioException("El cliente no tiene adeudos pendientes.");
            if (monto.compareTo(saldo) > 0)
                throw new NegocioException("El pago (" + monto + ") es mayor que el saldo pendiente (" + saldo + "). Revise la cantidad.");
            BigDecimal restante = monto;
            LocalDateTime ahora = LocalDateTime.now();
            for (Adeudo a : pendientes) {
                if (restante.signum() <= 0) break;
                BigDecimal aplicar = restante.min(a.saldo());
                adeudos.insertarPago(c, a.id(), u.id(), ahora, aplicar);
                restante = restante.subtract(aplicar);
            }
            return saldo.subtract(monto);   // nuevo saldo
        });
    }

    public record Estado(Cliente cliente, List<Adeudo> adeudos, List<PagoAdeudo> pagos) { }

    /** CU-CLI-05: saldo, adeudos y pagos del cliente. */
    public Estado estadoDeCuenta(Cliente cliente) {
        Sesion.exigir(Permiso.CLIENTES);
        return Tx.ejecutar(c -> {
            Cliente actualizado = repo.buscar(c, cliente.id()).orElseThrow(() -> new NegocioException("El cliente no existe."));
            return new Estado(actualizado, adeudos.listarPorCliente(c, cliente.id()), adeudos.pagosPorCliente(c, cliente.id()));
        });
    }
}
