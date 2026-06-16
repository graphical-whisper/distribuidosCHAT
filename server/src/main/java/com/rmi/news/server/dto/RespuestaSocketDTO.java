package com.rmi.news.server.dto;

import com.google.gson.JsonElement;

public class RespuestaSocketDTO {
    private String estado; // EXITO, ERROR
    private String mensaje;
    private JsonElement datos;

    public RespuestaSocketDTO() {}

    // Getters y Setters
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public JsonElement getDatos() { return datos; }
    public void setDatos(JsonElement datos) { this.datos = datos; }
}