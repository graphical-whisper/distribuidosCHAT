package com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServicioRedCliente {
    private final String ip;
    private final int puerto;
    private final String nombreUsuario;
    private final ClienteListener listener;
    private PrintWriter out;
    private Socket socket;

    public ServicioRedCliente(String ip, int puerto, String nombreUsuario, ClienteListener listener) {
        this.ip = ip;
        this.puerto = puerto;
        this.nombreUsuario = nombreUsuario;
        this.listener = listener;
    }

    public void conectar() {
        new Thread(() -> {
            try {
                socket = new Socket(ip, puerto);
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviar comando de registro inicial
                out.println("CONNECT;" + nombreUsuario);

                String linea;
                while ((linea = in.readLine()) != null) {
                    procesarLineaServidor(linea);
                }
            } catch (Exception e) {
                listener.onDesconexion();
            } finally {
                cerrarConexion();
            }
        }).start();
    }

    public void enviarMensajeGlobal(String contenido) {
        if (out != null) {
            out.println("BROADCAST;" + nombreUsuario + ";" + contenido);
        }
    }

    public void enviarMensajePrivado(String destinatario, String contenido) {
        if (out != null) {
            out.println("PRIVATE;" + nombreUsuario + ";" + destinatario + ";" + contenido);
        }
    }

    private void procesarLineaServidor(String linea) {
        String[] partes = linea.split(";", 3);
        if (partes.length == 0) return;

        String comando = partes[0];

        switch (comando) {
            case "USERS":
                String[] usuariosArray = linea.split(";");
                List<String> usuarios = new ArrayList<>(Arrays.asList(usuariosArray).subList(1, usuariosArray.length));
                usuarios.remove(nombreUsuario); 
                listener.onListaUsuariosActualizada(usuarios);
                break;
            case "MSG":
                if (partes.length >= 3) {
                    listener.onMensajeRecibido(partes[1], partes[2]);
                }
                break;
            case "PRIVMSG":
                if (partes.length >= 3) {
                    listener.onMensajePrivadoRecibido(partes[1], partes[2]);
                }
                break;
            case "ERROR":
                if (partes.length >= 2) {
                    listener.onErrorRecibido(partes[1]);
                }
                break;
        }
    }

    public void cerrarConexion() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar el socket del cliente.");
        }
    }
}