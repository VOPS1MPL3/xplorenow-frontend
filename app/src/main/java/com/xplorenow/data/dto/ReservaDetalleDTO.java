package com.xplorenow.data.dto;

public class ReservaDetalleDTO {
    private Long id;
    private String voucherCodigo;
    private EstadoReserva estado;
    private Integer cantidadParticipantes;
    private String creadaEn;

    private Long actividadId;
    private String actividadNombre;
    private String actividadImagen;
    private String destino;
    private String categoria;
    private Integer duracionMinutos;
    private String guiaAsignado;
    private String idioma;
    private String puntoEncuentro;
    private Double latitud;
    private Double longitud;
    private String politicaCancelacion;

    private String fecha;
    private String hora;

    private CalificacionDTO calificacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVoucherCodigo() { return voucherCodigo; }
    public void setVoucherCodigo(String voucherCodigo) { this.voucherCodigo = voucherCodigo; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public Integer getCantidadParticipantes() { return cantidadParticipantes; }
    public void setCantidadParticipantes(Integer cantidadParticipantes) { this.cantidadParticipantes = cantidadParticipantes; }
    public String getCreadaEn() { return creadaEn; }
    public void setCreadaEn(String creadaEn) { this.creadaEn = creadaEn; }
    public Long getActividadId() { return actividadId; }
    public void setActividadId(Long actividadId) { this.actividadId = actividadId; }
    public String getActividadNombre() { return actividadNombre; }
    public void setActividadNombre(String actividadNombre) { this.actividadNombre = actividadNombre; }
    public String getActividadImagen() { return actividadImagen; }
    public void setActividadImagen(String actividadImagen) { this.actividadImagen = actividadImagen; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public String getGuiaAsignado() { return guiaAsignado; }
    public void setGuiaAsignado(String guiaAsignado) { this.guiaAsignado = guiaAsignado; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getPuntoEncuentro() { return puntoEncuentro; }
    public void setPuntoEncuentro(String puntoEncuentro) { this.puntoEncuentro = puntoEncuentro; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public String getPoliticaCancelacion() { return politicaCancelacion; }
    public void setPoliticaCancelacion(String politicaCancelacion) { this.politicaCancelacion = politicaCancelacion; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public CalificacionDTO getCalificacion() { return calificacion; }
    public void setCalificacion(CalificacionDTO calificacion) { this.calificacion = calificacion; }
}
