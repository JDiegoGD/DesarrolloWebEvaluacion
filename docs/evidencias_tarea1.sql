-- =====================================================================
-- Tarea 1 - Relaciones JPA: consultas de evidencia para SQLyog
-- Base de datos: bd_pacientes (MariaDB de XAMPP, puerto 3307)
-- Requisito previo: haber ejecutado docs/migracion_tarea1_fk.sql
-- =====================================================================
USE bd_pacientes;

-- ---------------------------------------------------------------------
-- 1) Estructura de las tablas (capturar la pestaña de resultado de cada una)
-- ---------------------------------------------------------------------
SHOW CREATE TABLE paciente;
SHOW CREATE TABLE atencion;
SHOW CREATE TABLE contacto_emergencia;
SHOW CREATE TABLE seguro_paciente;
SHOW CREATE TABLE alergia;
SHOW CREATE TABLE antecedente_personal;
SHOW CREATE TABLE antecedente_familiar;
SHOW CREATE TABLE bitacora_paciente;

-- ---------------------------------------------------------------------
-- 2) Todas las claves foráneas del esquema
-- ---------------------------------------------------------------------
SELECT  k.TABLE_NAME              AS tabla,
        k.COLUMN_NAME             AS columna,
        k.REFERENCED_TABLE_NAME   AS tabla_referenciada,
        k.REFERENCED_COLUMN_NAME  AS columna_referenciada,
        k.CONSTRAINT_NAME         AS constraint_fk
FROM    information_schema.KEY_COLUMN_USAGE k
WHERE   k.TABLE_SCHEMA = 'bd_pacientes'
  AND   k.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY k.TABLE_NAME, k.COLUMN_NAME;

-- ---------------------------------------------------------------------
-- 3) Relación PACIENTE - ATENCION - USUARIO (médico) con datos de prueba
--    LEFT JOIN al médico porque id_usuario_medico es opcional
-- ---------------------------------------------------------------------
SELECT  p.codigo_paciente,
        p.numero_documento,
        CONCAT(p.nombres, ' ', p.apellido_paterno, ' ', p.apellido_materno) AS paciente,
        p.estado                AS estado_paciente,
        a.id_atencion,
        a.fecha_atencion,
        a.motivo,
        u.id_usuario            AS id_medico,
        u.nombre_usuario        AS medico,
        u.rol                   AS rol_medico
FROM    paciente p
JOIN    atencion a      ON a.id_paciente = p.id_paciente
LEFT JOIN usuario u     ON u.id_usuario  = a.id_usuario_medico
ORDER BY p.numero_documento, a.fecha_atencion DESC, a.id_atencion;

-- ---------------------------------------------------------------------
-- 4) (Opcional) Resumen de hijos por paciente: muestra que el paciente
--    eliminado sin atenciones no dejó registros huérfanos
-- ---------------------------------------------------------------------
SELECT  p.numero_documento,
        p.estado,
        (SELECT COUNT(*) FROM contacto_emergencia  c WHERE c.id_paciente = p.id_paciente) AS contactos,
        (SELECT COUNT(*) FROM seguro_paciente      s WHERE s.id_paciente = p.id_paciente) AS seguros,
        (SELECT COUNT(*) FROM antecedente_personal x WHERE x.id_paciente = p.id_paciente) AS ant_personales,
        (SELECT COUNT(*) FROM antecedente_familiar y WHERE y.id_paciente = p.id_paciente) AS ant_familiares,
        (SELECT COUNT(*) FROM alergia              z WHERE z.id_paciente = p.id_paciente) AS alergias,
        (SELECT COUNT(*) FROM atencion             a WHERE a.id_paciente = p.id_paciente) AS atenciones
FROM    paciente p
ORDER BY p.numero_documento;
