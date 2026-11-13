-- ============================================================
-- Sistema de Gestión para Verdulería - Esquema de base de datos
-- Compatible con MySQL 8.0+ (y MariaDB 10.5+)
-- ============================================================
CREATE DATABASE IF NOT EXISTS verduleria
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE verduleria;

CREATE TABLE IF NOT EXISTS roles (
  id_rol INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(40) NOT NULL,
  CONSTRAINT uq_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  id_rol INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  usuario VARCHAR(60) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_usuarios_usuario UNIQUE (usuario),
  CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categorias (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS productos (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  id_categoria INT NULL,
  nombre VARCHAR(120) NOT NULL,
  unidad_medida ENUM('kg','pieza','manojo','otro') NOT NULL,
  precio_venta DECIMAL(12,2) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT ck_productos_precio CHECK (precio_venta >= 0),
  CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventario (
  id_producto INT PRIMARY KEY,
  existencia DECIMAL(12,3) NOT NULL DEFAULT 0,
  stock_minimo DECIMAL(12,3) NOT NULL DEFAULT 0,
  CONSTRAINT ck_inventario_existencia CHECK (existencia >= 0),
  CONSTRAINT ck_inventario_minimo CHECK (stock_minimo >= 0),
  CONSTRAINT fk_inventario_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clientes (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  telefono VARCHAR(25) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS compras (
  id_compra INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  fecha DATETIME NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  estado ENUM('borrador','confirmada','cancelada') NOT NULL DEFAULT 'borrador',
  CONSTRAINT ck_compras_total CHECK (total >= 0),
  CONSTRAINT fk_compras_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_compra (
  id_detalle_compra INT AUTO_INCREMENT PRIMARY KEY,
  id_compra INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad DECIMAL(12,3) NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  subtotal DECIMAL(12,2) AS (ROUND(cantidad * precio_unitario, 2)) STORED,
  CONSTRAINT ck_dcompra_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_dcompra_precio CHECK (precio_unitario >= 0),
  CONSTRAINT fk_dcompra_compra FOREIGN KEY (id_compra) REFERENCES compras(id_compra),
  CONSTRAINT fk_dcompra_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ventas (
  id_venta INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  id_cliente INT NULL,
  fecha DATETIME NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  estado ENUM('borrador','confirmada','cancelada') NOT NULL DEFAULT 'borrador',
  metodo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO',
  efectivo_recibido DECIMAL(12,2) NULL DEFAULT NULL,
  cambio DECIMAL(12,2) NULL DEFAULT NULL,
  CONSTRAINT ck_ventas_total CHECK (total >= 0),
  CONSTRAINT fk_ventas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
  CONSTRAINT fk_ventas_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_venta (
  id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
  id_venta INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad DECIMAL(12,3) NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  subtotal DECIMAL(12,2) AS (ROUND(cantidad * precio_unitario, 2)) STORED,
  CONSTRAINT ck_dventa_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_dventa_precio CHECK (precio_unitario >= 0),
  CONSTRAINT fk_dventa_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
  CONSTRAINT fk_dventa_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movimientos_inventario (
  id_movimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_producto INT NOT NULL,
  id_usuario INT NOT NULL,
  fecha DATETIME NOT NULL,
  tipo ENUM('entrada_compra','salida_venta','merma','ajuste_entrada','ajuste_salida') NOT NULL,
  cantidad DECIMAL(12,3) NOT NULL,
  id_compra INT NULL,
  id_venta INT NULL,
  motivo VARCHAR(255) NULL,
  CONSTRAINT ck_mov_cantidad CHECK (cantidad > 0),
  CONSTRAINT fk_mov_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
  CONSTRAINT fk_mov_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
  CONSTRAINT fk_mov_compra FOREIGN KEY (id_compra) REFERENCES compras(id_compra),
  CONSTRAINT fk_mov_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS adeudos (
  id_adeudo INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NOT NULL,
  id_venta INT NULL,
  fecha DATETIME NOT NULL,
  monto_original DECIMAL(12,2) NOT NULL,
  descripcion VARCHAR(255) NULL,
  CONSTRAINT ck_adeudos_monto CHECK (monto_original > 0),
  CONSTRAINT fk_adeudos_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
  CONSTRAINT fk_adeudos_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pagos_adeudo (
  id_pago INT AUTO_INCREMENT PRIMARY KEY,
  id_adeudo INT NOT NULL,
  id_usuario INT NOT NULL,
  fecha DATETIME NOT NULL,
  monto DECIMAL(12,2) NOT NULL,
  CONSTRAINT ck_pagos_monto CHECK (monto > 0),
  CONSTRAINT fk_pagos_adeudo FOREIGN KEY (id_adeudo) REFERENCES adeudos(id_adeudo),
  CONSTRAINT fk_pagos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ---------- Vistas para reportes ----------
CREATE OR REPLACE VIEW vw_saldos_adeudos AS
SELECT a.id_adeudo, a.id_cliente, a.id_venta, a.fecha, a.descripcion,
       a.monto_original,
       COALESCE(SUM(p.monto), 0) AS total_pagado,
       a.monto_original - COALESCE(SUM(p.monto), 0) AS saldo_pendiente
FROM adeudos a
LEFT JOIN pagos_adeudo p ON p.id_adeudo = a.id_adeudo
GROUP BY a.id_adeudo, a.id_cliente, a.id_venta, a.fecha, a.descripcion, a.monto_original;

CREATE OR REPLACE VIEW vw_productos_bajo_stock AS
SELECT p.id_producto, p.nombre, p.unidad_medida, i.existencia, i.stock_minimo
FROM productos p
JOIN inventario i ON i.id_producto = p.id_producto
WHERE p.activo = TRUE AND i.existencia <= i.stock_minimo;

-- ---------- Datos iniciales ----------
INSERT IGNORE INTO roles (nombre) VALUES ('Administrador'), ('Cajero');
INSERT IGNORE INTO categorias (nombre) VALUES ('Frutas'), ('Verduras'), ('Legumbres'), ('Hierbas'), ('Otros');

-- ---------- Cuenta de BD con privilegios mínimos (cambiar la contraseña) ----------
-- CREATE USER IF NOT EXISTS 'verduleria_app'@'localhost' IDENTIFIED BY 'CambiarEsta#2026';
-- GRANT SELECT, INSERT, UPDATE ON verduleria.* TO 'verduleria_app'@'localhost';
-- FLUSH PRIVILEGES;
