package com.xplorenow.model;

public class ReservaResponse {
    private Long id;
    private String estado;
    private String voucherCodigo;
    private int cantidadParticipantes;
    private String actividadNombre;
    private String destinoNombre;
    private String fecha;
    private String hora;

    public Long getId() { return id; }
    public String getEstado() { return estado; }
    public String getVoucherCodigo() { return voucherCodigo; }
    public int getCantidadParticipantes() { return cantidadParticipantes; }
    public String getActividadNombre() { return actividadNombre; }
    public String getDestinoNombre() { return destinoNombre; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
}