package com.xplorenow.data.dto;
import java.math.BigDecimal;

public class ActividadDTO {

    private Long id;
    private String nombre;
    private String imagenPrincipal;
    private DestinoDTO destino;
    private CategoriaDTO categoria;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private Integer cuposDisponibles;

    // Getters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getImagenPrincipal() { return imagenPrincipal; }
    public DestinoDTO getDestino() { return destino; }
    public CategoriaDTO getCategoria() { return categoria; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getCuposDisponibles() { return cuposDisponibles; }
}