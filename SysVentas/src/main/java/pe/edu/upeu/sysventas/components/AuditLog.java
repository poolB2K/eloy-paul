package pe.edu.upeu.sysventas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo para registrar todas las acciones importantes del sistema
 * Permite rastrear quién, qué, cuándo y desde dónde se realizaron cambios
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "upeu_audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_audit")
    private Long idAudit;

    @Column(name = "usuario", nullable = false, length = 50)
    private String usuario;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion; // INSERT, UPDATE, DELETE, LOGIN, LOGOUT

    @Column(name = "tabla", nullable = false, length = 50)
    private String tabla; // Nombre de la tabla afectada

    @Column(name = "id_registro")
    private String idRegistro; // ID del registro afectado

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores; // JSON con datos antes del cambio

    @Column(name = "datos_nuevos", columnDefinition = "TEXT")
    private String datosNuevos; // JSON con datos después del cambio

    @PrePersist
    protected void onCreate() {
        fechaHora = LocalDateTime.now();
    }
}