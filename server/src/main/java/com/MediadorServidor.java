package com;

public interface MediadorServidor {
    void registrarCliente(String nombre, ClientHandler handler);
    void eliminarCliente(String nombreUsuario);
    void difundirMensaje(String mensajeFormateado);
    void enviarMensajePrivado(String emisor, String receptor, String contenido);
}