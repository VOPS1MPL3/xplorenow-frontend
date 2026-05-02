package com.xplorenow.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reservas")
public class ReservaEntity {
    @PrimaryKey
    private long id;
    private String voucherCodigo;
    private String estado;
    private int cantidadParticipantes;
    private String actividadNombre;
    private String actividadImagen;
    private String destino;
    private String categoria;
    private String puntoEncuentro;
    private double latitud;
    private double longitud;
    private String politicaCancelacion;
    private String fecha;
    private String hora;
    private String guiaAsignado;
    private String idioma;

    public ReservaEntity(long id, String voucherCodigo, String estado, int cantidadParticipantes,
                         String actividadNombre, String actividadImagen, String destino, String categoria,
                         String puntoEncuentro, double latitud, double longitud,
                         String politicaCancelacion, String fecha, String hora,
                         String guiaAsignado, String idioma) {
        this.id = id;
        this.voucherCodigo = voucherCodigo;
        this.estado = estado;
        this.cantidadParticipantes = cantidadParticipantes;
        this.actividadNombre = actividadNombre;
        this.actividadImagen = actividadImagen;
        this.destino = destino;
        this.categoria = categoria;
        this.puntoEncuentro = puntoEncuentro;
        this.latitud = latitud;
        this.longitud = longitud;
        this.politicaCancelacion = politicaCancelacion;
        this.fecha = fecha;
        this.hora = hora;
        this.guiaAsignado = guiaAsignado;
        this.idioma = idioma;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getVoucherCodigo() { return voucherCodigo; }
    public void setVoucherCodigo(String voucherCodigo) { this.voucherCodigo = voucherCodigo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getCantidadParticipantes() { return cantidadParticipantes; }
    public void setCantidadParticipantes(int cantidadParticipantes) { this.cantidadParticipantes = cantidadParticipantes; }
    public String getActividadNombre() { return actividadNombre; }
    public void setActividadNombre(String actividadNombre) { this.actividadNombre = actividadNombre; }
    public String getActividadImagen() { return actividadImagen; }
    public void setActividadImagen(String actividadImagen) { this.actividadImagen = actividadImagen; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getPuntoEncuentro() { return puntoEncuentro; }
    public void setPuntoEncuentro(String puntoEncuentro) { this.puntoEncuentro = puntoEncuentro; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public String getPoliticaCancelacion() { return politicaCancelacion; }
    public void setPoliticaCancelacion(String politicaCancelacion) { this.politicaCancelacion = politicaCancelacion; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getGuiaAsignado() { return guiaAsignado; }
    public void setGuiaAsignado(String guiaAsignado) { this.guiaAsignado = guiaAsignado; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
}