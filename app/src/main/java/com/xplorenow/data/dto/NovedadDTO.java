package com.xplorenow.data.dto;

public class NovedadDTO {

    private Long id;
    private Long reservaId;
    private TipoNovedad tipo;
    private String mensaje;
    private String fecha; // LocalDateTime tal cual lo manda el backend (ISO), se reenvia sin parsear

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
    public TipoNovedad getTipo() { return tipo; }
    public void setTipo(TipoNovedad tipo) { this.tipo = tipo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
