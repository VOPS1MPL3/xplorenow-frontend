package com.xplorenow.data.dto;
import java.math.BigDecimal;
import java.util.List;

public class ActividadDetalleDTO {

    private Long id;
    private String nombre;
    private String imagenPrincipal;
    private String destino;
    private String categoria;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private Integer cuposDisponibles;

    private String descripcion;
    private String queIncluye;
    private String puntoEncuentro;
    private Double latitud;
    private Double longitud;
    private String guiaAsignado;
    private String idioma;
    private String politicaCancelacion;

    private List<String> galeriaUrls;

    // Getters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getImagenPrincipal() { return imagenPrincipal; }
    public String getDestino() { return destino; }
    public String getCategoria() { return categoria; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getCuposDisponibles() { return cuposDisponibles; }
    public String getDescripcion() { return descripcion; }
    public String getQueIncluye() { return queIncluye; }
    public String getPuntoEncuentro() { return puntoEncuentro; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getGuiaAsignado() { return guiaAsignado; }
    public String getIdioma() { return idioma; }
    public String getPoliticaCancelacion() { return politicaCancelacion; }
    public List<String> getGaleriaUrls() { return galeriaUrls; }}