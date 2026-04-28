package com.xplorenow.data.dto;
import java.math.BigDecimal;

public class ActividadDTO {

    private Long id;
    private String nombre;
    private String imagenPrincipal;
    private String destino;        // String, no DestinoDTO
    private String categoria;      // String, no CategoriaDTO
    private Integer duracionMinutos;
    private BigDecimal precio;
    private Integer cuposDisponibles;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getImagenPrincipal() { return imagenPrincipal; }
    public String getDestino() { return destino; }
    public String getCategoria() { return categoria; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getCuposDisponibles() { return cuposDisponibles; }
}