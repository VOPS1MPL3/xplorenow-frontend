package com.xplorenow.data.dto;

public class CrearReservaRequest {
    private Long horarioId;
    private Integer cantidadParticipantes;

    public CrearReservaRequest(Long horarioId, Integer cantidadParticipantes) {
        this.horarioId = horarioId;
        this.cantidadParticipantes = cantidadParticipantes;
    }

    public Long getHorarioId() { return horarioId; }
    public Integer getCantidadParticipantes() { return cantidadParticipantes; }
}
