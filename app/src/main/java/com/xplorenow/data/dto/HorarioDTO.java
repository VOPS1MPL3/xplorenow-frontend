package com.xplorenow.data.dto;

public class HorarioDTO {
    private Long id;
    private String fecha;
    private String hora;
    private Integer cuposRestantes;

    public Long getId() { return id; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public Integer getCuposRestantes() { return cuposRestantes; }
}