-- =====================================================================
-- Tarea 2 - Usuarios y roles: migración para MariaDB 10.4 (XAMPP)
-- ---------------------------------------------------------------------
-- Hibernate 7 genera "ALTER TABLE IF EXISTS", que MariaDB 10.4 no admite,
-- así que columnas nuevas, FK y UNIQUE se crean aquí, con nombres explícitos.
--
-- EJECUTAR UNA SOLA VEZ y ANTES de arrancar la aplicación con el código de
-- la Tarea 2 (DataInitializer necesita usuario.id_rol).
-- En SQLyog: activar "Abortar en error" (o ejecutar bloque por bloque).
-- Requisito previo: docs/migracion_tarea1_fk.sql ya ejecutado.
-- =====================================================================
USE bd_pacientes;
-- El cliente mysql de XAMPP usa cp850 por defecto: sin esto las tildes se guardan corruptas
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- PASO 1: tabla rol y roles base
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rol (
    id_rol      INT          NOT NULL AUTO_INCREMENT,
    activo      BIT(1)       NOT NULL DEFAULT b'1',
    descripcion VARCHAR(255) NULL,
    nombre      VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO rol (nombre, descripcion, activo)
SELECT 'ADMINISTRADOR', 'Administra usuarios, roles y catálogos del sistema', b'1' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMINISTRADOR');
INSERT INTO rol (nombre, descripcion, activo)
SELECT 'MEDICO', 'Registra y consulta atenciones de pacientes', b'1' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'MEDICO');
INSERT INTO rol (nombre, descripcion, activo)
SELECT 'RECEPCIONISTA', 'Registra pacientes, seguros y contactos', b'1' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'RECEPCIONISTA');

-- ---------------------------------------------------------------------
-- PASO 2: columnas nuevas en usuario (id_rol PRIMERO como NULL)
-- ---------------------------------------------------------------------
ALTER TABLE usuario ADD COLUMN id_rol   INT          NULL;
ALTER TABLE usuario ADD COLUMN password VARCHAR(100) NULL;
ALTER TABLE usuario ADD COLUMN nombres  VARCHAR(150) NULL;
ALTER TABLE usuario ADD COLUMN correo   VARCHAR(150) NULL;
ALTER TABLE usuario ADD COLUMN activo   BIT(1)       NOT NULL DEFAULT b'1';

-- 2a) Texto antiguo usuario.rol -> id_rol. Se aceptan los valores que usaba el
--     frontend (ADMIN, MEDICO, RECEPCION) y sus variantes; la intercalación
--     utf8mb4_general_ci ignora mayúsculas y tildes ('Médico' = 'MEDICO').
UPDATE usuario u
JOIN rol r ON r.nombre = CASE
        WHEN TRIM(u.rol) IN ('ADMIN', 'ADMINISTRADOR')               THEN 'ADMINISTRADOR'
        WHEN TRIM(u.rol) IN ('MEDICO', 'DOCTOR')                     THEN 'MEDICO'
        WHEN TRIM(u.rol) IN ('RECEPCION', 'RECEPCIONISTA')           THEN 'RECEPCIONISTA'
     END
SET u.id_rol = r.id_rol
WHERE u.id_rol IS NULL;

-- 2b) Sin coincidencia: MEDICO si ya figura como médico en alguna atención
UPDATE usuario u
JOIN rol r ON r.nombre = 'MEDICO'
SET u.id_rol = r.id_rol
WHERE u.id_rol IS NULL
  AND EXISTS (SELECT 1 FROM atencion a WHERE a.id_usuario_medico = u.id_usuario);

-- 2c) Sin forma de saberlo: ADMINISTRADOR (revisar manualmente después)
UPDATE usuario u
JOIN rol r ON r.nombre = 'ADMINISTRADOR'
SET u.id_rol = r.id_rol
WHERE u.id_rol IS NULL;

-- 2d) Contraseña temporal "Cambiar123*" (hash BCrypt generado con
--     spring-security-crypto 7.1.1) para usuarios sin password
UPDATE usuario
SET password = '$2a$10$979kcxJFKd6zIjCCgCmiF.HrY447bvYQb4OY/E.ZnLZbZpddkTFMS'
WHERE password IS NULL;

UPDATE usuario SET activo = b'1';

-- ---------------------------------------------------------------------
-- GUARDA: detiene el script si algún usuario quedó sin rol o sin password.
-- Necesaria porque el sql_mode de XAMPP no es estricto: un MODIFY a NOT NULL
-- convertiría los NULL en 0 sin dar error.
-- ---------------------------------------------------------------------
DELIMITER //
BEGIN NOT ATOMIC
    IF (SELECT COUNT(*) FROM usuario WHERE id_rol IS NULL OR password IS NULL) > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Migración detenida: hay usuarios sin id_rol o sin password. No se aplican los pasos 3 y 4.';
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- PASO 3: NOT NULL, FK usuario -> rol y UNIQUE
-- ---------------------------------------------------------------------
ALTER TABLE usuario MODIFY id_rol   INT          NOT NULL;
ALTER TABLE usuario MODIFY password VARCHAR(100) NOT NULL;
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol);
ALTER TABLE rol     ADD CONSTRAINT uk_rol_nombre UNIQUE (nombre);
-- Un índice UNIQUE admite varios NULL: el correo es único solo cuando viene informado
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_correo UNIQUE (correo);

-- ---------------------------------------------------------------------
-- PASO 4: eliminar la columna de texto antigua (solo se llega aquí si la
-- guarda y el paso 3 terminaron sin error)
-- ---------------------------------------------------------------------
ALTER TABLE usuario DROP COLUMN rol;

-- Verificación
SHOW CREATE TABLE rol;
SHOW CREATE TABLE usuario;
SELECT u.id_usuario, u.nombre_usuario, r.nombre AS rol, LEFT(u.password, 7) AS hash, u.activo
FROM usuario u JOIN rol r ON r.id_rol = u.id_rol;
