-- Script de creación de tablas para el Sistema de Gestión de Pedidos ED
-- Uso de IF NOT EXISTS para evitar errores si las tablas ya existen en PostgreSQL

-- Tabla de Clientes
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono BIGINT,
    zona VARCHAR(50),
    direccion VARCHAR(255),
    vip BOOLEAN DEFAULT FALSE,
    penalizacion NUMERIC(15, 2) DEFAULT 0.0
);

-- Tabla de Comercios
CREATE TABLE IF NOT EXISTS comercios (
    id_comercio SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_negocio VARCHAR(255),
    direccion VARCHAR(255),
    zona VARCHAR(50)
);

-- Tabla de Productos
CREATE TABLE IF NOT EXISTS productos (
    id_producto INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio NUMERIC(15, 2) NOT NULL,
    cantidad INT DEFAULT 0,
    id_comercio INT REFERENCES comercios(id_comercio)
);

-- Tabla de Repartidores
CREATE TABLE IF NOT EXISTS repartidores (
    id_repartidor BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono BIGINT,
    zona VARCHAR(50),
    calificacion NUMERIC(3, 2) DEFAULT 0.0,
    saldo NUMERIC(15, 2) DEFAULT 0.0,
    disponible BOOLEAN DEFAULT TRUE
);
