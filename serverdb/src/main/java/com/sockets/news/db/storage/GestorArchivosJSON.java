package com.sockets.news.db.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rmi.news.shared.dto.NoticiaDTO;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GestorArchivosJSON {

    private static final String RUTA_ARCHIVO = "datos/noticias.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // Control de concurrencia
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        // Inicializar archivo si no existe
        File archivo = new File(RUTA_ARCHIVO);
        archivo.getParentFile().mkdirs();
        if (!archivo.exists()) {
            guardarTodo(new ArrayList<>());
        }
    }

    public static List<NoticiaDTO> leerTodas() {
        lock.readLock().lock();
        try (Reader reader = new FileReader(RUTA_ARCHIVO)) {
            Type listType = new TypeToken<List<NoticiaDTO>>(){}.getType();
            List<NoticiaDTO> noticias = gson.fromJson(reader, listType);
            return noticias != null ? noticias : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error de lectura: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void guardarTodo(List<NoticiaDTO> noticias) {
        lock.writeLock().lock();
        try (Writer writer = new FileWriter(RUTA_ARCHIVO)) {
            gson.toJson(noticias, writer);
        } catch (IOException e) {
            System.err.println("Error de escritura: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean insertar(NoticiaDTO nuevaNoticia) {
        List<NoticiaDTO> noticias = leerTodas();
        
        // Validación de Nombre Único a nivel de base de datos
        boolean existe = noticias.stream()
                .anyMatch(n -> n.getNombreUnico().equalsIgnoreCase(nuevaNoticia.getNombreUnico()));
        
        if (existe) return false;

        noticias.add(nuevaNoticia);
        guardarTodo(noticias);
        return true;
    }
}