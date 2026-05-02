package com.xplorenow.data.dto;

public class FiltrosActividad {

    private Long destinoId;
    private Long categoriaId;
    private String fechaDesde;
    private String fechaHasta;
    private Double precioMin;
    private Double precioMax;

    public FiltrosActividad() {}

    // Getters / Setters
    public Long getDestinoId() { return destinoId; }
    public void setDestinoId(Long destinoId) { this.destinoId = destinoId; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    public Double getPrecioMin() { return precioMin; }
    public void setPrecioMin(Double precioMin) { this.precioMin = precioMin; }

    public Double getPrecioMax() { return precioMax; }
    public void setPrecioMax(Double precioMax) { this.precioMax = precioMax; }

    public boolean estaVacio() {
        return destinoId == null && categoriaId == null
                && fechaDesde == null && fechaHasta == null
                && precioMin == null && precioMax == null;
    }
}