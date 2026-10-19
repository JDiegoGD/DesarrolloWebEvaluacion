package com.tecsup.model;

import com.tecsup.enums.OperacionAuditoria;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

// Auditoría global. Sin FK: debe sobrevivir aunque se elimine el usuario o el registro auditado.
// Tabla e índices creados por docs/migracion_tarea4.sql
@Entity
@Table(name = "auditoria", indexes = {
        @Index(name = "idx_auditoria_fecha_hora", columnList = "fecha_hora"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario"),
        @Index(name = "idx_auditoria_entidad", columnList = "entidad"),
        @Index(name = "idx_auditoria_operacion", columnList = "operacion")
})
public class Auditoria {

    public static final int MAX_DETALLE = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditoria;

    @Column(nullable = false, length = 100)
    private String usuario;

    // Sin FK a propósito
    private Integer idUsuario;

    @Column(length = 50)
    private String rolUsuario;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    // VARCHAR y no ENUM nativo de MariaDB: agregar operaciones no exige alterar la columna
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private OperacionAuditoria operacion;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(length = 100)
    private String idRegistro;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    public Long getIdAuditoria() {
        return idAuditoria; }

    public String getUsuario() {
        return usuario; }
    public void setUsuario(String usuario) {
        this.usuario = usuario; }

    public Integer getIdUsuario() {
        return idUsuario; }
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario; }

    public String getRolUsuario() {
        return rolUsuario; }
    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario; }

    public LocalDateTime getFechaHora() {
        return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora; }

    public OperacionAuditoria getOperacion() {
        return operacion; }
    public void setOperacion(OperacionAuditoria operacion) {
        this.operacion = operacion; }

    public String getEntidad() {
        return entidad; }
    public void setEntidad(String entidad) {
        this.entidad = entidad; }

    public String getIdRegistro() {
        return idRegistro; }
    public void setIdRegistro(String idRegistro) {
        this.idRegistro = idRegistro; }

    public String getDetalle() {
        return detalle; }
    // Máximo 1000 caracteres
    public void setDetalle(String detalle) {
        this.detalle = (detalle != null && detalle.length() > MAX_DETALLE)
                ? detalle.substring(0, MAX_DETALLE - 3) + "..." : detalle; }
}
