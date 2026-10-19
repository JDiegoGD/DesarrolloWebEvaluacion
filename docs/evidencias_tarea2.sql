-- =====================================================================
-- Tarea 2 - Usuarios y roles: consultas de evidencia para SQLyog
-- Base de datos: bd_pacientes (MariaDB 10.4 de XAMPP, puerto 3307)
-- Requisito previo: docs/migracion_tarea2.sql ejecutado
-- =====================================================================
USE bd_pacientes;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1) Estructura: rol y usuario (debe verse fk_usuario_rol, uk_rol_nombre,
--    uk_usuario_correo, id_rol y password NOT NULL)
-- ---------------------------------------------------------------------
SHOW CREATE TABLE rol;
SHOW CREATE TABLE usuario;

-- ---------------------------------------------------------------------
-- 2) Todas las claves foráneas del esquema (incluida usuario -> rol)
-- ---------------------------------------------------------------------
SELECT  k.TABLE_NAME              AS tabla,
        k.COLUMN_NAME             AS columna,
        k.REFERENCED_TABLE_NAME   AS tabla_referenciada,
        k.REFERENCED_COLUMN_NAME  AS columna_referenciada,
        k.CONSTRAINT_NAME         AS constraint_fk
FROM    information_schema.KEY_COLUMN_USAGE k
WHERE   k.TABLE_SCHEMA = 'bd_pacientes'
  AND   k.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY (k.CONSTRAINT_NAME = 'fk_usuario_rol') DESC, k.TABLE_NAME, k.COLUMN_NAME;

-- ---------------------------------------------------------------------
-- 3) Usuarios con su rol. Solo se muestra el prefijo del hash BCrypt
--    ($2a$10$ = algoritmo 2a, costo 10), nunca el hash completo
-- ---------------------------------------------------------------------
SELECT  u.id_usuario,
        u.nombre_usuario,
        u.nombres,
        u.correo,
        IF(u.activo = b'1', 'Activo', 'Inactivo')   AS estado_usuario,
        r.id_rol,
        r.nombre                                     AS rol,
        IF(r.activo = b'1', 'Activo', 'Inactivo')   AS estado_rol,
        LEFT(u.password, 7)                          AS prefijo_hash,
        LENGTH(u.password)                           AS largo_hash
FROM    usuario u
JOIN    rol r ON r.id_rol = u.id_rol
ORDER BY r.nombre, u.nombre_usuario;

-- ---------------------------------------------------------------------
-- 4) Conteo de usuarios por rol (incluye roles sin usuarios)
-- ---------------------------------------------------------------------
SELECT  r.nombre                                         AS rol,
        IF(r.activo = b'1', 'Activo', 'Inactivo')       AS estado_rol,
        COUNT(u.id_usuario)                              AS total_usuarios,
        COALESCE(SUM(u.activo = b'1'), 0)                AS usuarios_activos
FROM    rol r
LEFT JOIN usuario u ON u.id_rol = r.id_rol
GROUP BY r.id_rol, r.nombre, r.activo
ORDER BY r.nombre;

-- ---------------------------------------------------------------------
-- 5) (Opcional) Ninguna contraseña en texto plano: todas deben ser BCrypt
-- ---------------------------------------------------------------------
SELECT  COUNT(*) AS total_usuarios,
        SUM(password REGEXP '^\\$2[aby]\\$[0-9]{2}\\$.{53}$') AS con_hash_bcrypt
FROM    usuario;
