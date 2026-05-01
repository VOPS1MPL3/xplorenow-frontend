package com.xplorenow.data.dto;

public class CalificacionDTO {
    private Long id;
    private Integer ratingActividad;
    private Integer ratingGuia;
    private String comentario;
    private String creadaEn;

    public Long getId() { return id; }
    public Integer getRatingActividad() { return ratingActividad; }
    public Integer getRatingGuia() { return ratingGuia; }
    public String getComentario() { return comentario; }
    public String getCreadaEn() { return creadaEn; }
}