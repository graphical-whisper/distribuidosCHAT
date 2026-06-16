package com.rmi.news.shared.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class NoticiaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombreUnico;
    private String titular;
    private String contenido;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaFechaActualizacion;
    private String autorId;

    public NoticiaDTO() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreUnico() { return nombreUnico; }
    public void setNombreUnico(String nombreUnico) { this.nombreUnico = nombreUnico; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaFechaActualizacion() { return ultimaFechaActualizacion; }
    public void setUltimaFechaActualizacion(LocalDateTime ultimaFechaActualizacion) { this.ultimaFechaActualizacion = ultimaFechaActualizacion; }

    public String getAutorId() { return autorId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }
}