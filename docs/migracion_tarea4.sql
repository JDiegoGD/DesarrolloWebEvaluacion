-- =====================================================================
-- Tarea 4 - Auditoría global: migración para MariaDB 10.4 (XAMPP)
-- ---------------------------------------------------------------------
-- Ejecutar UNA SOLA VEZ y ANTES de arrancar la aplicación con la Tarea 4.
-- La tabla NO tiene claves foráneas a propósito: una fila de auditoría debe
-- sobrevivir aunque se elimine el usuario o el registro auditado.
-- Índices con nombres explícitos (los mismos que declara la entidad Auditoria,
-- así Hibernate los reconoce y no intenta crearlos).
-- =====================================================================
USE bd_pacientes;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria BIGINT       NOT NULL AUTO_INCREMENT,
    usuario      VARCHAR(100) NOT NULL,           -- nombre de usuario o 'SISTEMA'
    id_usuario   INT          NULL,               -- sin FK
    rol_usuario  VARCHAR(50)  NULL,               -- rol en el momento de la operación
    fecha_hora   DATETIME(6)  NOT NULL,
    operacion    VARCHAR(20)  NOT NULL,           -- REGISTRO, MODIFICACION, ELIMINACION, ACTIVACION, DESACTIVACION
    entidad      VARCHAR(50)  NOT NULL,
    id_registro  VARCHAR(100) NULL,
    detalle      TEXT         NULL,               -- máximo 1000 caracteres (truncado por la aplicación)
    PRIMARY KEY (id_auditoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_auditoria_fecha_hora ON auditoria (fecha_hora);
CREATE INDEX idx_auditoria_usuario    ON auditoria (usuario);
CREATE INDEX idx_auditoria_entidad    ON auditoria (entidad);
CREATE INDEX idx_auditoria_operacion  ON auditoria (operacion);

-- Verificación
SHOW CREATE TABLE auditoria;
SHOW INDEX FROM auditoria;
