package com.xplorenow.data.dto;

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
    public void setId(Long id) { this.id = id; }
    public String getVoucherCodigo() { return voucherCodigo; }
    public void setVoucherCodigo(String voucherCodigo) { this.voucherCodigo = voucherCodigo; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public Integer getCantidadParticipantes() { return cantidadParticipantes; }
    public void setCantidadParticipantes(Integer cantidadParticipantes) { this.cantidadParticipantes = cantidadParticipantes; }
    public Long getActividadId() { return actividadId; }
    public void setActividadId(Long actividadId) { this.actividadId = actividadId; }
    public String getActividadNombre() { return actividadNombre; }
    public void setActividadNombre(String actividadNombre) { this.actividadNombre = actividadNombre; }
    public String getActividadImagen() { return actividadImagen; }
    public void setActividadImagen(String actividadImagen) { this.actividadImagen = actividadImagen; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public String getGuiaAsignado() { return guiaAsignado; }
    public void setGuiaAsignado(String guiaAsignado) { this.guiaAsignado = guiaAsignado; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public CalificacionDTO getCalificacion() { return calificacion; }
    public void setCalificacion(CalificacionDTO calificacion) { this.calificacion = calificacion; }
}
