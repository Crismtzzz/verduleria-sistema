package com.verduleria;

import com.verduleria.model.*;
import com.verduleria.service.*;
import com.verduleria.util.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite completa de pruebas unitarias y de integración del Sistema de Gestión para Verdulería.
 * Verifica reglas de negocio, integridad de transacciones, validaciones y permisos.
 */
public class SistemaVerduleriaTest {

    private static Usuario admin;

    @BeforeAll
    public static void setup() {
        // Iniciar sesión con el usuario administrador (id=3 es el root registrado en la BD)
        admin = new Usuario(3, 1, "Administrador", "root", "root", true);
        Sesion.iniciar(admin);
        assertTrue(Sesion.activa());
    }

    @AfterAll
    public static void tearDown() {
        new AuthService().cerrarSesion();
        assertFalse(Sesion.activa());
    }

    @Test
    public void testPasswordUtilHashAndVerify() {
        String pass = "MiClaveSecreta#2026";
        String hash = PasswordUtil.hash(pass);
        assertNotNull(hash);
        assertTrue(hash.startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.verificar(pass, hash));
        assertFalse(PasswordUtil.verificar("otraClave", hash));
    }

    @Test
    public void testValidadorDecimalsAndDates() {
        BigDecimal d = Validador.decimal("12.50", "Precio", 2);
        assertEquals(new BigDecimal("12.50"), d);

        BigDecimal coma = Validador.decimal("15,75", "Precio", 2);
        assertEquals(new BigDecimal("15.75"), coma);

        assertThrows(NegocioException.class, () -> Validador.decimal("abc", "Precio", 2));
        assertThrows(NegocioException.class, () -> Validador.decimal("12.555", "Precio", 2));
        assertThrows(NegocioException.class, () -> Validador.positivo(BigDecimal.ZERO, "Cantidad"));
        assertThrows(NegocioException.class, () -> Validador.noNegativo(new BigDecimal("-1"), "Precio"));

        LocalDateTime fh = Validador.fechaHora("2026-11-04 10:30", "Fecha");
        assertEquals(2026, fh.getYear());
        assertEquals(11, fh.getMonthValue());
        assertEquals(4, fh.getDayOfMonth());
        assertEquals(10, fh.getHour());
        assertEquals(30, fh.getMinute());
    }

    @Test
    public void testPermisosRol() {
        Usuario adm = new Usuario(1, 1, "Administrador", "Admin", "admin", true);
        Usuario caj = new Usuario(2, 2, "Cajero", "Cajero", "cajero", true);

        // Administrador tiene todos los permisos
        for (Permiso p : Permiso.values()) {
            assertTrue(adm.tiene(p), "Admin debe tener " + p);
        }

        // Cajero tiene permisos limitados (no debe tener usuarios ni editar productos)
        assertTrue(caj.tiene(Permiso.VENTAS));
        assertTrue(caj.tiene(Permiso.COMPRAS));
        assertTrue(caj.tiene(Permiso.CLIENTES));
        assertTrue(caj.tiene(Permiso.PRODUCTOS_VER));
        assertTrue(caj.tiene(Permiso.INVENTARIO_VER));
        assertFalse(caj.tiene(Permiso.USUARIOS));
        assertFalse(caj.tiene(Permiso.PRODUCTOS_EDITAR));
    }

    @Test
    public void testLineaSubtotal() {
        Linea l = new Linea(1, "Naranja", "kg", new BigDecimal("2.500"), new BigDecimal("18.50"));
        // 2.500 * 18.50 = 46.25
        assertEquals(new BigDecimal("46.25"), l.subtotal());
    }

    @Test
    public void testCicloCompletoComprasVentasAdeudosYReportes() {
        ProductoService prodService = new ProductoService();
        CompraService compService = new CompraService();
        VentaService ventService = new VentaService();
        ClienteService cliService = new ClienteService();
        InventarioService invService = new InventarioService();
        ReporteService repService = new ReporteService();

        // 1. Registrar producto de prueba con nombre único
        String nombreProd = "Platano Test " + System.currentTimeMillis();
        List<Categoria> cats = prodService.categorias();
        Categoria cat = cats.isEmpty() ? null : cats.get(0);
        int idProd = prodService.registrar(nombreProd, cat, "kg", "25.00", "5.000");
        assertTrue(idProd > 0);

        // Verificar existencia inicial en 0
        List<Producto> encontrados = prodService.listar(nombreProd, true);
        assertFalse(encontrados.isEmpty());
        Producto p = encontrados.get(0);
        assertEquals(new BigDecimal("0.000"), p.existencia());
        assertEquals(new BigDecimal("25.00"), p.precioVenta());

        // 2. Registrar compra (Central de Abastos): entra mercancía
        Linea lineaCompra = CompraService.crearLinea(p, "10.000", "15.00");
        int idCompra = compService.confirmar(LocalDateTime.now(), List.of(lineaCompra));
        assertTrue(idCompra > 0);

        // Verificar que la existencia subió a 10.000 (RF-INV-02)
        p = prodService.listar(nombreProd, true).get(0);
        assertEquals(0, new BigDecimal("10.000").compareTo(p.existencia()));

        // 3. Intentar vender más de lo disponible: debe ser rechazado (RF-INV-05)
        Linea lineaExceso = VentaService.crearLinea(p, "15.000");
        assertThrows(NegocioException.class, () -> ventService.confirmar(null, false, List.of(lineaExceso)));

        // 4. Registrar venta normal de mostrador (3.000 kg a $25 = $75)
        Linea lineaVenta = VentaService.crearLinea(p, "3.000");
        int idVenta = ventService.confirmar(null, false, List.of(lineaVenta));
        assertTrue(idVenta > 0);

        // Verificar existencia descontada a 7.000 (RF-INV-03)
        p = prodService.listar(nombreProd, true).get(0);
        assertEquals(0, new BigDecimal("7.000").compareTo(p.existencia()));

        // 5. Registrar merma (1.000 kg dañados) (RF-INV-06 / CU-INV-03)
        invService.registrarMerma(p, "1.000", "Plátano maduro dañado");
        p = prodService.listar(nombreProd, true).get(0);
        assertEquals(0, new BigDecimal("6.000").compareTo(p.existencia()));

        // 6. Registrar cliente y venta a crédito (RF-CLI-01, RF-CLI-03, RF-CLI-04)
        String nombreCli = "Cliente Test " + System.currentTimeMillis();
        int idCli = cliService.registrar(nombreCli, "2281234567");
        assertTrue(idCli > 0);
        Cliente cliente = cliService.listar(nombreCli, true).get(0);
        assertEquals(0, BigDecimal.ZERO.compareTo(cliente.saldo()));

        // Venta a crédito de 2.000 kg ($50.00)
        Linea lineaCredito = VentaService.crearLinea(p, "2.000");
        int idVentaCredito = ventService.confirmar(cliente, true, List.of(lineaCredito));
        assertTrue(idVentaCredito > 0);

        // Verificar saldo del cliente actualizado a $50.00
        cliente = cliService.listar(nombreCli, true).get(0);
        assertEquals(0, new BigDecimal("50.00").compareTo(cliente.saldo()));

        // 7. Registrar abono/pago de adeudo ($30.00) (CU-CLI-04)
        final Cliente clienteConDeuda = cliente;
        BigDecimal nuevoSaldo = cliService.registrarPago(clienteConDeuda, "30.00");
        assertEquals(new BigDecimal("20.00"), nuevoSaldo);

        // Intentar pagar más del saldo restante ($25 > $20): debe rechazar
        assertThrows(NegocioException.class, () -> cliService.registrarPago(clienteConDeuda, "25.00"));

        // Liquidar adeudo con $20.00
        nuevoSaldo = cliService.registrarPago(clienteConDeuda, "20.00");
        assertEquals(new BigDecimal("0.00"), nuevoSaldo);

        // 8. Consultar todos los reportes (RF-REP-01 a RF-REP-05)
        Tabla repVentas = repService.ventas(null, null);
        assertNotNull(repVentas);
        assertFalse(repVentas.filas().isEmpty());

        Tabla repInv = repService.inventario();
        assertNotNull(repInv);

        Tabla repCompras = repService.compras(null, null);
        assertNotNull(repCompras);

        Tabla repMovs = repService.movimientos(null, null);
        assertNotNull(repMovs);

        Tabla repBajoStock = repService.bajoStock();
        assertNotNull(repBajoStock);

        Tabla repAdeudos = repService.adeudos();
        assertNotNull(repAdeudos);

        // 9. Desactivar producto (RF-PRO-05)
        prodService.cambiarActivo(idProd, false);
        p = prodService.listar(nombreProd, false).get(0);
        assertFalse(p.activo());

        // Intentar vender producto desactivado: debe rechazar
        final Producto prodDesactivado = p;
        assertThrows(NegocioException.class, () -> ventService.confirmar(null, false, List.of(lineaVenta)));
    }

    @Test
    public void testValidacionesUsuario() {
        assertThrows(NegocioException.class, () -> UsuarioService.validarNombreUsuario("ab")); // < 3
        assertThrows(NegocioException.class, () -> UsuarioService.validarNombreUsuario("usuario con espacios"));
        assertThrows(NegocioException.class, () -> UsuarioService.validarPassword("corta")); // < 8
        assertEquals("usuario_valido.1", UsuarioService.validarNombreUsuario("usuario_valido.1"));
    }

    @Test
    public void testValidacionesProducto() {
        ProductoService ps = new ProductoService();
        assertThrows(NegocioException.class, () -> ps.registrar("", null, "kg", "20.00", "0"));
        assertThrows(NegocioException.class, () -> ps.registrar("Test Prod", null, "tonelada_invalida", "20.00", "0"));
        assertThrows(NegocioException.class, () -> ps.registrar("Test Prod", null, "kg", "-5.00", "0"));
    }

    @Test
    public void testFechasFlexiblesYRangoInvalido() {
        LocalDateTime d1 = Validador.fechaHora("2026-5-4", "Fecha");
        assertEquals(2026, d1.getYear());
        assertEquals(5, d1.getMonthValue());
        assertEquals(4, d1.getDayOfMonth());

        assertThrows(NegocioException.class, () -> Validador.fechaHora("", "Fecha"));
        assertThrows(NegocioException.class, () -> Validador.fechaHora("formato-invalido", "Fecha"));

        ReporteService rs = new ReporteService();
        LocalDateTime desde = LocalDateTime.of(2026, 11, 10, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 11, 5, 0, 0);
        assertThrows(NegocioException.class, () -> rs.ventas(desde, hasta));
    }

    @Test
    public void testInicializacionPanelesVisualesYRediseño() {
        com.verduleria.ui.Tema.inicializar();
        assertDoesNotThrow(() -> {
            com.verduleria.ui.InicioPanel inicio = new com.verduleria.ui.InicioPanel();
            inicio.recargar();

            com.verduleria.ui.ProductosPanel productos = new com.verduleria.ui.ProductosPanel();
            productos.recargar();

            com.verduleria.ui.VentasPanel ventas = new com.verduleria.ui.VentasPanel();
            ventas.recargar();

            com.verduleria.ui.ComprasPanel compras = new com.verduleria.ui.ComprasPanel();
            compras.recargar();

            com.verduleria.ui.InventarioPanel inventario = new com.verduleria.ui.InventarioPanel();
            inventario.recargar();

            com.verduleria.ui.ClientesPanel clientes = new com.verduleria.ui.ClientesPanel();
            clientes.recargar();

            com.verduleria.ui.ReportesPanel reportes = new com.verduleria.ui.ReportesPanel();
            reportes.recargar();

            com.verduleria.ui.UsuariosPanel usuarios = new com.verduleria.ui.UsuariosPanel();
            usuarios.recargar();

            com.verduleria.ui.MainFrame main = new com.verduleria.ui.MainFrame(admin);
            main.mostrarModulo("ventas");
            main.mostrarModulo("productos");
            main.mostrarModulo("inventario");
            main.mostrarModulo("inicio");
            main.dispose();
        });
    }
}
