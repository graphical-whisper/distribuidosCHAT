package com.rmi.news.server.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rmi.news.server.dto.PeticionSocketDTO;
import com.rmi.news.server.dto.RespuestaSocketDTO;
import com.rmi.news.shared.dto.NoticiaDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.lang.reflect.Type;

public class NoticiaSocketDAO {

    // Configuración de red hacia la VM 2 (Servidor de Datos)
    private static final String IP_VM2 = "10.10.1.130";
    private static final int PUERTO_VM2 = 5000;
    private final Gson gson;

    public NoticiaSocketDAO() {
        // Importante registrar adaptadores para LocalDateTime si se usa Gson
        this.gson = new GsonBuilder().create(); 
    }

    public NoticiaDTO guardar(NoticiaDTO noticia) throws RuntimeException {
        PeticionSocketDTO peticion = new PeticionSocketDTO("CREAR", "NOTICIA", gson.toJsonTree(noticia));
        RespuestaSocketDTO respuesta = enviarPeticion(peticion);

        if ("EXITO".equals(respuesta.getEstado())) {
            return gson.fromJson(respuesta.getDatos(), NoticiaDTO.class);
        }
        throw new RuntimeException("Error en VM2: " + respuesta.getMensaje());
    }

    public boolean eliminar(String nombreUnico) {
        PeticionSocketDTO peticion = new PeticionSocketDTO("ELIMINAR", "NOTICIA", gson.toJsonTree(nombreUnico));
        RespuestaSocketDTO respuesta = enviarPeticion(peticion);
        return "EXITO".equals(respuesta.getEstado());
    }

    public NoticiaDTO obtenerPorNombreUnico(String nombreUnico) {
        PeticionSocketDTO peticion = new PeticionSocketDTO("LEER_UNO", "NOTICIA", gson.toJsonTree(nombreUnico));
        RespuestaSocketDTO respuesta = enviarPeticion(peticion);
        
        if ("EXITO".equals(respuesta.getEstado()) && respuesta.getDatos() != null) {
            return gson.fromJson(respuesta.getDatos(), NoticiaDTO.class);
        }
        return null;
    }

    public List<NoticiaDTO> obtenerTodas(String criterio) {
        PeticionSocketDTO peticion = new PeticionSocketDTO("LEER_TODAS", "NOTICIA", gson.toJsonTree(criterio));
        RespuestaSocketDTO respuesta = enviarPeticion(peticion);

        if ("EXITO".equals(respuesta.getEstado())) {
            Type listType = new TypeToken<List<NoticiaDTO>>(){}.getType();
            return gson.fromJson(respuesta.getDatos(), listType);
        }
        throw new RuntimeException("Error al consultar datos en VM2.");
    }

    // Lógica interna de comunicación TCP por Socket efímero
    private RespuestaSocketDTO enviarPeticion(PeticionSocketDTO peticion) {
        try (Socket socket = new Socket(IP_VM2, PUERTO_VM2);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonPeticion = gson.toJson(peticion);
            out.println(jsonPeticion);

            String jsonRespuesta = in.readLine();
            return gson.fromJson(jsonRespuesta, RespuestaSocketDTO.class);

        } catch (Exception e) {
            throw new RuntimeException("Falla de comunicación por Sockets con el Nodo de Datos (VM2)", e);
        }
    }

    public NoticiaDTO actualizar(NoticiaDTO noticiaActualizada) {
        PeticionSocketDTO peticion = new PeticionSocketDTO("ACTUALIZAR", "NOTICIA", gson.toJsonTree(noticiaActualizada));
        RespuestaSocketDTO respuesta = enviarPeticion(peticion);

        if ("EXITO".equals(respuesta.getEstado())) {
            return gson.fromJson(respuesta.getDatos(), NoticiaDTO.class);
        }
        throw new RuntimeException("Error al actualizar en VM2: " + respuesta.getMensaje());
    }
}