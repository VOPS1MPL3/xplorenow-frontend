package com.xplorenow.data.dto;

public class NoticiaDTO {
    private Long id;
    private String titulo;
    private String descripcionBreve;
    private String imagenUrl;
    private Long actividadRelacionadaId;
    private String publicadaEn;

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcionBreve() { return descripcionBreve; }
    public String getImagenUrl() { return imagenUrl; }
    public Long getActividadRelacionadaId() { return actividadRelacionadaId; }
    public String getPublicadaEn() { return publicadaEn; }
}