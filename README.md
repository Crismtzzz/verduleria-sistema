# Sistema de Gestión para Verdulería Jiménez

Sistema de escritorio moderno para la administración comercial de verdulerías, fruterías y abarrotes.  
Desarrollado en **Java Swing**, **Maven**, **JDBC**, **MySQL 8.0** y **FlatLaf**.

> **Documentación del Sistema y Requerimientos:**  
> Consulte [docs/DOCUMENTACION_SISTEMA.md](docs/DOCUMENTACION_SISTEMA.md) para la especificación formal de requerimientos funcionales y no funcionales, casos de uso, diagrama relacional y diccionario de datos.

---

## 🎨 Rediseño Visual y Experiencia de Usuario (Dashboard Moderno)

La interfaz gráfica fue rediseñada para ofrecer una experiencia estética, ergonómica y profesional inspirada en dashboards modernos de gestión comercial:

- **Paleta de colores oficial:**
  - 🌲 **Verde oscuro (`#184D36`):** Menú lateral, encabezados, botones principales de confirmación y detalles de marca.
  - 🌿 **Verde salvia (`#83A96B`):** Módulo activo en el menú lateral, etiquetas de éxito y selección de elementos.
  - 🌾 **Crema (`#F7F5EF`):** Fondo general suave en todas las pantallas (elimina grandes superficies blancas deslumbrantes).
  - 🏺 **Terracota (`#C47D51`):** Acentos cálidos, alertas de stock bajo y avisos de atención.
  - 🌫️ **Gris suave (`#E6E9E1` / `#D8DCD3`):** Bordes sutiles, tarjetas, fondos secundarios y divisores.
- **Menú lateral oscuro con CardLayout:** Navegación fluida entre módulos (Inicio, Ventas, Compras, Productos, Inventario, Clientes, Reportes, Usuarios) con resaltado visual y botón accesible para cerrar sesión.
- **Dashboard de Inicio con KPIs Reales:** Métricas dinámicas calculadas directamente de la base de datos (Ventas de hoy, Productos activos, Stock bajo, Saldo por cobrar) y tabla de movimientos recientes con insignias visuales.
- **Navegación por Píldoras (Pill Tabs):** Vistas secundarias ("Nueva venta" | "Historial") integradas limpiamente sin pestañas anticuadas.
- **Componentes vectoriales HiDPI (`Iconos.java`):** Íconos procedurales nítidos para cada acción y módulo sin dependencias de imágenes externas rasterizadas.
- **Insignias de estado (Badges):** Indicadores visuales redondeados de texto y color ("Activo", "Inactivo", "Suficiente", "Stock bajo", "Sin stock").
- **Ventanas modales rediseñadas:** Formularios de alta y modificación estilo tarjeta flotante con ícono distintivo de hoja y botones redondeados.

---

## 💻 Requisitos del Entorno

1. **Java Development Kit (JDK):** Versión 17 o superior (verificado con JDK 23).
2. **Apache Maven:** 3.8+ (incluido con NetBeans).
3. **MySQL Server:** 8.0 o superior en ejecución local (`localhost:3306`).
4. **IDE (Opcional):** NetBeans 21+, IntelliJ IDEA o Eclipse.

---

## 🚀 Instrucciones para Abrir y Ejecutar el Proyecto

### Opción A: Desde NetBeans IDE

1. Abra **NetBeans**.
2. Vaya al menú **File** → **Open Project...** (o presione `Ctrl + Shift + O`).
3. Navegue hasta la carpeta `verduleria-sistema` y haga clic en **Open Project** (NetBeans reconocerá automáticamente el icono del proyecto Maven con la taza de café).
4. Espere a que NetBeans descargue las dependencias (FlatLaf y MySQL Connector).
5. En la ventana **Projects**, haga clic derecho sobre el proyecto **Sistema de Gestión para Verdulería (sistema-verduleria)** y seleccione **Clean and Build**.
6. Haga clic derecho sobre el proyecto y seleccione **Run** (o presione `F6`).  
   *La clase principal predeterminada es `com.verduleria.Main`.*

### Opción B: Desde la Consola / Terminal (Maven)

1. Abra una terminal (PowerShell o CMD) en la raíz del proyecto:
   ```powershell
   cd c:\Users\Cristian\Desktop\verduleria-sistema
   ```
2. Compile y ejecute las pruebas unitarias:
   ```powershell
   mvn clean test
   ```
3. Genere el paquete ejecutable con dependencias:
   ```powershell
   mvn package
   ```
4. Ejecute el sistema:
   ```powershell
   java -jar target/sistema-verduleria-1.0-jar-with-dependencies.jar
   ```

---

## ⚙️ Base de Datos y Primer Uso

1. Ejecute el script `sql/schema.sql` en MySQL Workbench o vía consola MySQL:
   ```sql
   source c:/Users/Cristian/Desktop/verduleria-sistema/sql/schema.sql;
   ```
2. Asegúrese de que las credenciales en `src/main/resources/db.properties` coincidan con su servidor MySQL local (por defecto: usuario `verduleria_app` o `root`).
3. **Primer inicio:** Si la base de datos no tiene usuarios, el sistema abrirá automáticamente el diálogo para crear la cuenta del **Administrador inicial**.

---

## ⌨️ Guía Completa de Atajos de Teclado

El sistema conserva y optimiza todos los atajos de teclado para agilizar la operación en mostrador:

| Tecla / Atajo | Módulo / Contexto | Acción |
| :--- | :--- | :--- |
| **`F1`** | Global | Abre la **Guía interactiva de atajos de teclado** |
| **`Ctrl + 1` al `8`** | Global | Navega directamente al módulo (1: Inicio, 2: Ventas, 3: Compras, 4: Productos, 5: Inventario, 6: Clientes, 7: Reportes, 8: Usuarios) |
| **`Alt + V`** | Global | Ir al módulo de **Ventas** |
| **`Alt + C`** | Global | Ir al módulo de **Compras** |
| **`Alt + P`** | Global | Ir al módulo de **Productos** |
| **`Alt + I`** | Global | Ir al módulo de **Inventario** |
| **`Alt + L`** | Global | Ir al módulo de **Clientes y Adeudos** |
| **`Alt + R`** | Global | Ir al módulo de **Reportes** |
| **`Alt + U`** | Global | Ir al módulo de **Usuarios y Permisos** |
| **`Ctrl + Tab`** / **`Shift+Tab`** | Global | Módulo siguiente / anterior |
| **`F12`** | Global | **Cerrar sesión** rápidamente |
| **`F2`** | Ventas / Compras | Enfocar selector de producto |
| **`F3`** | Ventas / Compras | Enfocar campo de cantidad |
| **`Enter`** (en cantidad/precio)| Ventas / Compras | Agregar renglón al carrito |
| **`F4`** | Ventas | Alternar casilla de **Venta a crédito (fiado)** |
| **`F6`** | Ventas | Enfocar selector de cliente |
| **`Supr` / `Delete`** | Ventas / Compras | Quitar renglón seleccionado |
| **`F5`** | Ventas / Compras | Cancelar / limpiar venta o compra actual |
| **`F9`** o **`Ctrl + Enter`** | Ventas / Compras | **Confirmar operación** y actualizar existencias |
| **`F2`** o **`Ctrl + N`** | Productos | **Nuevo producto** |
| **`F3`** | Productos / Inventario | Enfocar buscador |
| **`F4`** | Productos | Modificar producto seleccionado |
| **`Alt + D`** | Productos | Activar / desactivar producto |
| **`F2`** o **`Alt + M`** | Inventario | Registrar merma del producto |
| **`F2`** / **`F3`** | Clientes | Nuevo cliente / Modificar cliente |
| **`F4`** | Clientes | Registrar adeudo |
| **`F6`** | Clientes | Registrar pago o abono |
| **`F7`** | Clientes | Consultar estado de cuenta detallado |
| **`F5`** | Reportes | Ejecutar consulta de reporte |
| **`Ctrl + S`** | Reportes | **Exportar a archivo CSV** |

---

## 🔒 Roles y Permisos (RF-AUT-03)

- **Administrador:** Acceso completo a todos los 8 módulos, configuración de precios, alta de productos, registro de mermas, reportes ejecutivos y gestión de usuarios.
- **Cajero:** Acceso restringido únicamente a Inicio, Ventas, Compras y Clientes (solo consulta de productos e inventario). Los módulos de Reportes y Usuarios se ocultan del menú lateral automáticamente.
