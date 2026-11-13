-- =============================================================================
-- Migración: Cobro en efectivo y cambio para Verdulería Jiménez
-- =============================================================================
-- Este script agrega las columnas para registrar el método de pago, el efectivo
-- recibido del cliente y el cambio a entregar en cada venta realizada.
-- Conserva el 100% de los registros existentes sin pérdida de información.
-- =============================================================================

USE verduleria;

-- 1. Agregar columnas en la tabla ventas
ALTER TABLE ventas
  ADD COLUMN metodo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO' AFTER estado,
  ADD COLUMN efectivo_recibido DECIMAL(12,2) NULL DEFAULT NULL AFTER metodo_pago,
  ADD COLUMN cambio DECIMAL(12,2) NULL DEFAULT NULL AFTER efectivo_recibido;

-- 2. Clasificar las ventas históricas que correspondan a créditos (tienen adeudo asociado)
UPDATE ventas v
JOIN adeudos a ON a.id_venta = v.id_venta
SET v.metodo_pago = 'CREDITO'
WHERE v.metodo_pago = 'EFECTIVO';

-- 3. Para las ventas históricas de contado (efectivo), asignar efectivo recibido igual al total y cambio 0
UPDATE ventas
SET efectivo_recibido = total,
    cambio = 0.00
WHERE metodo_pago = 'EFECTIVO' AND efectivo_recibido IS NULL;
