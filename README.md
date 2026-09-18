# Sistema de Gestión de Pacientes

API REST desarrollada con Spring Boot para el módulo de Pacientes de un sistema hospitalario. Permite registrar y consultar pacientes, administrar seguros y contactos de emergencia, y mantener una bitácora de modificaciones.

Proyecto del laboratorio **“Implementación de Persistencia con Spring Boot e Hibernate” – Tecsup**.

## Tecnologías

- Java 17
- Spring Boot 4.1.1
- Spring Data JPA
- Hibernate
- MariaDB
- Thymeleaf

## Cómo ejecutarlo

1. Crear la base de datos en MariaDB:

   ```sql
   CREATE DATABASE bd_pacientes;
   ```

2. Revisar la conexión, el usuario y la contraseña en `src/main/resources/application.properties`.

3. Ejecutar la clase `PacienteApplication` desde IntelliJ IDEA.

4. Abrir en el navegador:

   ```text
   http://localhost:8080
   ```

Las tablas se crean automáticamente al iniciar la aplicación, según la configuración de Hibernate.

## Requerimientos implementados

| Código | Descripción |
| --- | --- |
| RF-PAC-01 | Registrar pacientes. |
| RF-PAC-02 | Validar que el documento no esté duplicado. |
| RF-PAC-03 | Generar un código único automáticamente. |
| RF-PAC-04 | Registrar datos personales y de contacto. |
| RF-PAC-05 | Buscar por documento, código, nombres y apellidos. |
| RF-PAC-06 | Consultar la información completa del paciente. |
| RF-PAC-07 | Consultar el resumen de atenciones. |
| RF-PAC-08 | Modificar los datos del paciente. |
| RF-PAC-09 | Actualizar el seguro del paciente. |
| RF-PAC-10 | Administrar contactos de emergencia. |
| RF-PAC-11 | Controlar el acceso según el rol. |
| RF-PAC-12 | Registrar las modificaciones en una bitácora. |
| RF-PAC-13 | Impedir la eliminación de pacientes con atenciones. |

## Endpoints

URL base: `http://localhost:8080`

### Pacientes

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/pacientes` | Registrar un paciente. |
| GET | `/api/pacientes` | Listar pacientes. |
| GET | `/api/pacientes/{id}` | Consultar un paciente por su ID. |
| GET | `/api/pacientes/buscar?nombres=` | Buscar pacientes por nombres. |
| PUT | `/api/pacientes/{id}?idUsuario=` | Actualizar un paciente indicando el usuario responsable. |
| DELETE | `/api/pacientes/{id}` | Eliminar un paciente si no tiene atenciones registradas. |
| GET | `/api/pacientes/{id}/bitacora` | Consultar la bitácora de modificaciones del paciente. |

### Otros recursos

| Métodos | Ruta base | Recurso |
| --- | --- | --- |
| POST, GET | `/api/atenciones` | Atenciones. |
| POST, GET, PUT, DELETE | `/api/seguros` | Seguros. |
| POST, GET, PUT, DELETE | `/api/contactos-emergencia` | Contactos de emergencia. |
| POST, GET | `/api/usuarios` | Usuarios. |
| GET, POST | `/api/catalogos/tipo-documento` | Tipos de documento. |
