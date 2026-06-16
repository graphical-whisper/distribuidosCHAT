package com.rmi.news.server.dto;

import com.google.gson.JsonElement;

public class PeticionSocketDTO {
    private String operacion; // CREAR, LEER_TODAS, LEER_UNO, ACTUALIZAR, ELIMINAR
    private String entidad;   // NOTICIA, USUARIO
    private JsonElement payload; // Datos adjuntos genéricos

    public PeticionSocketDTO(String operacion, String entidad, JsonElement payload) {
        this.operacion = operacion;
        this.entidad = entidad;
        this.payload = payload;
    }

    // Getters y Setters
    public String getOperacion() { return operacion; }
    public String getEntidad() { return entidad; }
    public JsonElement getPayload() { return payload; }
}