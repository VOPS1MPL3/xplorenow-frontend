package com.xplorenow.data.dto;

import java.math.BigDecimal;

public class FavoritoDTO {

    private Long actividadId;
    private String actividadNombre;
    private String actividadImagen;
    private String destino;
    private String categoria;
    private Integer duracionMinutos;

    private BigDecimal precioActual;
    private Integer cuposActuales;

    private BigDecimal precioAlMarcar;
    private Integer cuposAlMarcar;

    /** true si bajo de precio o liberaron cupos desde que se marco */
    private boolean tieneNovedad;
    /** "BAJO_PRECIO" / "MAS_CUPOS" / null */
    private String motivoNovedad;

    public Long getActividadId()        { return actividadId; }
    public String getActividadNombre()  { return actividadNombre; }
    public String getActividadImagen()  { return actividadImagen; }
    public String getDestino()          { return destino; }
    public String getCategoria()        { return categoria; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public BigDecimal getPrecioActual()    { return precioActual; }
    public Integer getCuposActuales()      { return cuposActuales; }
    public BigDecimal getPrecioAlMarcar()  { return precioAlMarcar; }
    public Integer getCuposAlMarcar()      { return cuposAlMarcar; }
    public boolean isTieneNovedad()        { return tieneNovedad; }
    public String getMotivoNovedad()       { return motivoNovedad; }
}
