-- =====================================================================
-- Tarea 1 - Claves foráneas que Hibernate NO puede crear en XAMPP
-- ---------------------------------------------------------------------
-- XAMPP trae MariaDB 10.4.32. Hibernate 7 (MariaDBDialect, mínimo 10.6)
-- genera "ALTER TABLE IF EXISTS ...", sintaxis que MariaDB solo acepta
-- desde la 10.5.2. Con ddl-auto=update esas sentencias fallan en silencio
-- (solo un WARN en el log) y la BD queda SIN claves foráneas.
--
-- Estas son exactamente las sentencias que genera Hibernate, sin el
-- "IF EXISTS" y con los mismos nombres de constraint, para que Hibernate
-- las reconozca y no intente crearlas otra vez.
-- Ejecutar una sola vez en SQLyog sobre bd_pacientes.
-- =====================================================================
USE bd_pacientes;

-- Paso 3: ATENCION N-1 USUARIO (médico), nullable
ALTER TABLE atencion ADD COLUMN id_usuario_medico INT NULL;

-- PACIENTE N-1 TIPO_DOCUMENTO / UBIGEO
ALTER TABLE paciente ADD CONSTRAINT FK8iltg8ow5i2qg51cv376rosyj FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento (id_tipo_documento);
ALTER TABLE paciente ADD CONSTRAINT FKsnn7d9e0e7ker470aph3hxcek FOREIGN KEY (id_ubigeo) REFERENCES ubigeo (id_ubigeo);

-- PACIENTE 1-N hijos
ALTER TABLE contacto_emergencia  ADD CONSTRAINT FKm1565femlt43f7bkfx775danf FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente);
ALTER TABLE seguro_paciente      ADD CONSTRAINT FKlal4t6a0lud5lpbnoeo8l418s FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente);
ALTER TABLE antecedente_personal ADD CONSTRAINT FKlu3dqs9jy3icyml1ci4a1g3mt FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente);
ALTER TABLE antecedente_familiar ADD CONSTRAINT FKeys9pjbt92vn75eyo1ojdkyhq FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente);
ALTER TABLE alergia              ADD CONSTRAINT FKo1c8m6td4cjbo50099ua04ygg FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente);
ALTER TABLE atencion             ADD CONSTRAINT FK9715msnjtp44k3qnawnh1x6aj FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente);
ALTER TABLE bitacora_paciente    ADD CONSTRAINT FKlvr1iekgde66gwhbi0jkgtfie FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente);

-- USUARIO 1-N BITACORA_PACIENTE y USUARIO 1-N ATENCION (médico)
ALTER TABLE bitacora_paciente ADD CONSTRAINT FK28l0y9h8eucvav8gwpmr9r0ee FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);
ALTER TABLE atencion          ADD CONSTRAINT FK2jcttlfobemp95f39mte3nccr FOREIGN KEY (id_usuario_medico) REFERENCES usuario (id_usuario);

-- Mismo problema con los @Column(unique = true): la BD no tenía ningún índice UNIQUE
ALTER TABLE tipo_documento ADD CONSTRAINT uk_tipo_documento_codigo UNIQUE (codigo);
ALTER TABLE usuario        ADD CONSTRAINT uk_usuario_nombre_usuario UNIQUE (nombre_usuario);
ALTER TABLE paciente       ADD CONSTRAINT uk_paciente_codigo_paciente UNIQUE (codigo_paciente);
ALTER TABLE paciente       ADD CONSTRAINT uk_paciente_numero_documento UNIQUE (numero_documento);
ALTER TABLE paciente       ADD CONSTRAINT uk_paciente_telefono UNIQUE (telefono);
ALTER TABLE paciente       ADD CONSTRAINT uk_paciente_correo_electronico UNIQUE (correo_electronico);
