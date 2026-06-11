package com;

import java.util.List;

public interface ClienteListener {
    void onMensajeRecibido(String emisor, String contenido);
    void onMensajePrivadoRecibido(String emisor, String contenido);
    void onListaUsuariosActualizada(List<String> usuarios);
    void onErrorRecibido(String mensajeError);
    void onDesconexion();
}