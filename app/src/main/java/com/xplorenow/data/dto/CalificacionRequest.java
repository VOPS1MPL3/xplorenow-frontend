package com.xplorenow.data.dto;

public class CalificacionRequest {
    private Integer ratingActividad;
    private Integer ratingGuia;
    private String comentario;

    public CalificacionRequest(Integer ratingActividad, Integer ratingGuia, String comentario) {
        this.ratingActividad = ratingActividad;
        this.ratingGuia      = ratingGuia;
        this.comentario      = comentario;
    }

    public Integer getRatingActividad() { return ratingActividad; }
    public Integer getRatingGuia()      { return ratingGuia; }
    public String getComentario()       { return comentario; }
}
