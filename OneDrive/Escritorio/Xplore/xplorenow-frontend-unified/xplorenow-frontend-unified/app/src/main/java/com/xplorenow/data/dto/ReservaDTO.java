package com.xplorenow.data.dto;

import java.math.BigDecimal;
public class ReservaDTO {

    private Long id;
    private String voucherCodigo;
    private EstadoReserva estado;
    private Integer cantidadParticipantes;
    private Long actividadId;
    private String actividadNombre;
    private String actividadImagen;
    private String destino;
    private Integer duracionMinutos;
    private String guiaAsignado;
    private String fecha;        // YYYY-MM-DD
    private String hora;         // HH:mm:ss

    private CalificacionDTO calificacion;

    public Long getId() { return id; }
    public String getVoucherCodigo() { return voucherCodigo; }
    public EstadoReserva getEstado() { return estado; }
    public Integer getCantidadParticipantes() { return cantidadParticipantes; }
    public Long getActividadId() { return actividadId; }
    public String getActividadNombre() { return actividadNombre; }
    public String getActividadImagen() { return actividadImagen; }
    public String getDestino() { return destino; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public String getGuiaAsignado() { return guiaAsignado; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public CalificacionDTO getCalificacion() { return calificacion; }
}