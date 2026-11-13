package com.verduleria.service;

import com.verduleria.model.*;
import com.verduleria.repository.*;
import com.verduleria.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** RF-VEN-01 a RF-VEN-06, RF-INV-03, RF-INV-05, RF-CLI-03. Cobro en efectivo y cambio. */
public class VentaService {
    private final VentaRepository repo = new VentaRepository();
    private final InventarioRepository inventario = new InventarioRepository();
    private final ProductoRepository productos = new ProductoRepository();
    private final ClienteRepository clientes = new ClienteRepository();
    private final AdeudoRepository adeudos = new AdeudoRepository();

    public static Linea crearLinea(Producto p, String cantidadTxt) {
        if (p == null || p.id() == null) throw new NegocioException("Seleccione un producto.");
        BigDecimal cant = Validador.positivo(Validador.decimal(cantidadTxt, "Cantidad", 3), "Cantidad");
        return new Linea(p.id(), p.nombre(), p.unidad(), cant, p.precioVenta());
    }

    public static BigDecimal total(List<Linea> lineas) {
        return lineas.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Confirma la venta de forma atómica registrando el método de pago, el efectivo recibido y el cambio.
     * Los precios se toman de la base de datos (no de la pantalla), se verifican y bloquean las existencias,
     * y se descuenta el inventario.
     *
     * @param aCredito si es true, la venta genera un adeudo del cliente por el total (requiere cliente).
     * @param efectivoRecibido monto entregado por el cliente en efectivo (debe cubrir el total en ventas de contado).
     * @param cambio cambio a entregar al cliente (calculado como efectivo - total).
     * @return folio de la venta generada
     */
    public int confirmar(Cliente cliente, boolean aCredito, List<Linea> lineas, BigDecimal efectivoRecibido, BigDecimal cambio) {
        Usuario u = Sesion.exigir(Permiso.VENTAS);
        if (lineas == null || lineas.isEmpty()) throw new NegocioException("Agregue al menos un producto a la venta.");
        if (aCredito && cliente == null) throw new NegocioException("Una venta a crédito requiere seleccionar un cliente.");

        // Combina el mismo producto en un solo renglón y ordena por id (evita interbloqueos).
        Map<Integer, BigDecimal> cantidades = new TreeMap<>();
        for (Linea l : lineas) cantidades.merge(l.idProducto(), l.cantidad(), BigDecimal::add);
        LocalDateTime fecha = LocalDateTime.now();

        return Tx.ejecutar(c -> {
            if (cliente != null) {
                Cliente cl = clientes.buscar(c, cliente.id())
                        .orElseThrow(() -> new NegocioException("El cliente ya no existe."));
                if (!cl.activo()) throw new NegocioException("El cliente «" + cl.nombre() + "» está deshabilitado.");
            }
            List<Linea> finales = new ArrayList<>();
            for (Map.Entry<Integer, BigDecimal> e : cantidades.entrySet()) {
                Producto p = productos.buscar(c, e.getKey())
                        .orElseThrow(() -> new NegocioException("Un producto de la venta ya no existe."));
                if (!p.activo())
                    throw new NegocioException("El producto «" + p.nombre() + "» está desactivado y no puede venderse.");
                BigDecimal existencia = inventario.existenciaBloqueada(c, p.id()).orElse(BigDecimal.ZERO);
                if (e.getValue().compareTo(existencia) > 0)                                  // RF-INV-05
                    throw new NegocioException("Existencia insuficiente de «" + p.nombre() + "»: disponible "
                            + existencia.stripTrailingZeros().toPlainString() + " " + p.unidad() + ", solicitado "
                            + e.getValue().stripTrailingZeros().toPlainString() + ".");
                finales.add(new Linea(p.id(), p.nombre(), p.unidad(), e.getValue(), p.precioVenta()));
            }
            BigDecimal total = total(finales);
            if (aCredito && total.signum() <= 0)
                throw new NegocioException("El total de una venta a crédito debe ser mayor que cero.");

            // Validaciones de cobro en efectivo / crédito
            BigDecimal efectivoFinal = null;
            BigDecimal cambioFinal = null;
            String metodoPago;
            if (aCredito) {
                metodoPago = "CREDITO";
            } else {
                metodoPago = "EFECTIVO";
                if (efectivoRecibido == null || efectivoRecibido.compareTo(total) < 0) {
                    throw new NegocioException("El efectivo recibido es insuficiente para cubrir el total de la venta.");
                }
                efectivoFinal = efectivoRecibido;
                cambioFinal = cambio != null ? cambio : efectivoRecibido.subtract(total);
            }

            int idVenta = repo.insertar(c, u.id(), cliente == null ? null : cliente.id(), fecha, total,
                    "confirmada", metodoPago, efectivoFinal, cambioFinal);
            for (Linea l : finales) {
                repo.insertarDetalle(c, idVenta, l);
                inventario.ajustar(c, l.idProducto(), l.cantidad().negate());                // RF-INV-03 / RF-VEN-05
                inventario.registrarMovimiento(c, l.idProducto(), u.id(), fecha, "salida_venta",
                        l.cantidad(), null, idVenta, null);
            }
            if (aCredito)
                adeudos.insertar(c, cliente.id(), idVenta, fecha, total, "Venta #" + idVenta + " a crédito");
            return idVenta;
        });
    }

    /** Sobrecarga para compatibilidad previa (asume pago exacto en efectivo si es de contado). */
    public int confirmar(Cliente cliente, boolean aCredito, List<Linea> lineas) {
        BigDecimal total = total(lineas);
        return confirmar(cliente, aCredito, lineas, aCredito ? null : total, aCredito ? null : BigDecimal.ZERO);
    }

    public List<Venta> historial(LocalDateTime desde, LocalDateTime hasta) {
        Sesion.exigir(Permiso.VENTAS);
        return Tx.ejecutar(c -> repo.listar(c, desde, hasta));
    }

    public List<Linea> detalle(int idVenta) {
        Sesion.exigir(Permiso.VENTAS);
        return Tx.ejecutar(c -> repo.detalle(c, idVenta));
    }
}
