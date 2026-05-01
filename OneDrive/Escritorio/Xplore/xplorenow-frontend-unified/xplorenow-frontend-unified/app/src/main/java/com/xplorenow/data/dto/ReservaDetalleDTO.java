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
    public String getVoucherCodigo() { return voucherCodigo; }
    public EstadoReserva getEstado() { return estado; }
    public Integer getCantidadParticipantes() { return cantidadParticipantes; }
    public String getCreadaEn() { return creadaEn; }
    public Long getActividadId() { return actividadId; }
    public String getActividadNombre() { return actividadNombre; }
    public String getActividadImagen() { return actividadImagen; }
    public String getDestino() { return destino; }
    public String getCategoria() { return categoria; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public String getGuiaAsignado() { return guiaAsignado; }
    public String getIdioma() { return idioma; }
    public String getPuntoEncuentro() { return puntoEncuentro; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getPoliticaCancelacion() { return politicaCancelacion; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public CalificacionDTO getCalificacion() { return calificacion; }
}