-- =====================================================================
-- PF-02 - Órdenes de reparación de bicicletas
-- Script de creación de la estructura (PostgreSQL 14)
--
-- Antes de ejecutarlo, crear la base de datos y conectarse a ella:
--     CREATE DATABASE taller_bicicletas;
-- =====================================================================

-- Servicios que ofrece el taller
CREATE TABLE servicio_taller (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100)  NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    costo_base  NUMERIC(10,2) NOT NULL CHECK (costo_base > 0),
    activo      BOOLEAN       NOT NULL DEFAULT TRUE
);

-- Órdenes de reparación: cada orden selecciona un servicio
CREATE TABLE orden_reparacion (
    id             BIGSERIAL PRIMARY KEY,
    cliente        VARCHAR(100)  NOT NULL,
    bicicleta      VARCHAR(100)  NOT NULL,
    fecha          DATE          NOT NULL,
    estado         VARCHAR(20)   NOT NULL
                   CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'TERMINADA')),
    observaciones  VARCHAR(255),
    recargo        NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (recargo >= 0),
    costo_estimado NUMERIC(10,2) NOT NULL CHECK (costo_estimado >= 0),
    servicio_id    BIGINT        NOT NULL,
    CONSTRAINT orden_reparacion_servicio_id_fkey
        FOREIGN KEY (servicio_id) REFERENCES servicio_taller (id)
);
