-- =====================================================================
-- Tarea 4 - Auditoría: consultas de evidencia para SQLyog
-- Base de datos: bd_pacientes (MariaDB 10.4 de XAMPP, puerto 3307)
-- Requisito previo: docs/migracion_tarea4.sql ejecutado
-- =====================================================================
USE bd_pacientes;
SET NAMES utf8mb4;

-- 1) Estructura: sin claves foráneas, con los 4 índices idx_auditoria_*
SHOW CREATE TABLE auditoria;

-- 2) Últimas 30 auditorías
SELECT  usuario,
        rol_usuario,
        fecha_hora,
        operacion,
        entidad,
        id_registro,
        detalle
FROM    auditoria
ORDER BY fecha_hora DESC, id_auditoria DESC
LIMIT 30;

-- 3) Operaciones por entidad
SELECT  entidad,
        operacion,
        COUNT(*) AS total
FROM    auditoria
GROUP BY entidad, operacion
ORDER BY entidad, operacion;

-- 4) Operaciones por usuario
SELECT  usuario,
        COUNT(*)                         AS total,
        SUM(operacion = 'REGISTRO')      AS registros,
        SUM(operacion = 'MODIFICACION')  AS modificaciones,
        SUM(operacion = 'ELIMINACION')   AS eliminaciones,
        SUM(operacion = 'ACTIVACION')    AS activaciones,
        SUM(operacion = 'DESACTIVACION') AS desactivaciones,
        MAX(fecha_hora)                  AS ultima_operacion
FROM    auditoria
GROUP BY usuario
ORDER BY total DESC;

-- 5) (Opcional) Ningún detalle contiene hashes BCrypt: debe devolver 0
SELECT  COUNT(*) AS detalles_con_hash
FROM    auditoria
WHERE   detalle LIKE '%$2a$%' OR detalle LIKE '%$2b$%' OR detalle LIKE '%$2y$%';
