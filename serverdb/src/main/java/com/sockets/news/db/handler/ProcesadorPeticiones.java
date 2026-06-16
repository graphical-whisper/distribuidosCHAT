package com.sockets.news.db.handler;

import com.google.gson.Gson;
import com.rmi.news.server.dto.PeticionSocketDTO;
import com.rmi.news.server.dto.RespuestaSocketDTO;
import com.rmi.news.shared.dto.NoticiaDTO;
import com.sockets.news.db.storage.GestorArchivosJSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ProcesadorPeticiones implements Runnable {

    private final Socket socket;
    private final Gson gson;

    public ProcesadorPeticiones(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String jsonEntrada = in.readLine();
            if (jsonEntrada != null) {
                PeticionSocketDTO peticion = gson.fromJson(jsonEntrada, PeticionSocketDTO.class);
                RespuestaSocketDTO respuesta = procesarOperacion(peticion);
                out.println(gson.toJson(respuesta));
            }

        } catch (Exception e) {
            System.err.println("Error procesando flujo de socket: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private RespuestaSocketDTO procesarOperacion(PeticionSocketDTO peticion) {
        RespuestaSocketDTO respuesta = new RespuestaSocketDTO();

        if ("NOTICIA".equals(peticion.getEntidad())) {
            switch (peticion.getOperacion()) {
                case "CREAR":
                    NoticiaDTO nuevaNoticia = gson.fromJson(peticion.getPayload(), NoticiaDTO.class);
                    if (GestorArchivosJSON.insertar(nuevaNoticia)) {
                        respuesta.setEstado("EXITO");
                        respuesta.setDatos(gson.toJsonTree(nuevaNoticia));
                    } else {
                        respuesta.setEstado("ERROR");
                        respuesta.setMensaje("El nombre único de la noticia ya existe.");
                    }
                    break;
                case "LEER_TODAS":
                    List<NoticiaDTO> noticias = GestorArchivosJSON.leerTodas();
                    respuesta.setEstado("EXITO");
                    respuesta.setDatos(gson.toJsonTree(noticias));
                    break;
                // La implementación de ELIMINAR, ACTUALIZAR y LEER_UNO sigue el mismo patrón
                default:
                    respuesta.setEstado("ERROR");
                    respuesta.setMensaje("Operación no soportada.");
            }
        } else {
            respuesta.setEstado("ERROR");
            respuesta.setMensaje("Entidad desconocida.");
        }
        return respuesta;
    }
}