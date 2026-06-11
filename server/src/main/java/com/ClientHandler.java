package com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final MediadorServidor mediador;
    private PrintWriter out;
    private BufferedReader in;
    private String nombreUsuario;

    public ClientHandler(Socket socket, MediadorServidor mediador) {
        this.socket = socket;
        this.mediador = mediador;
    }

    public void enviarDatos(String datos) {
        if (out != null) {
            out.println(datos);
        }
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String mensajeEntrada;
            while ((mensajeEntrada = in.readLine()) != null) {
                delegarProcesamiento(mensajeEntrada);
            }
        } catch (Exception e) {
            System.err.println("Conexión interrumpida en el hilo del cliente.");
        } finally {
            mediador.eliminarCliente(nombreUsuario);
            cerrarRecursos();
        }
    }

    private void delegarProcesamiento(String mensaje) {
        String[] partes = mensaje.split(";", 4);
        if (partes.length == 0) return;

        String comando = partes[0];

        switch (comando) {
            case "CONNECT":
                if (partes.length >= 2) {
                    nombreUsuario = partes[1];
                    mediador.registrarCliente(nombreUsuario, this);
                }
                break;
            case "BROADCAST":
                if (partes.length >= 3) {
                    mediador.difundirMensaje("MSG;" + partes[1] + ";" + partes[2]);
                }
                break;
            case "PRIVATE":
                if (partes.length >= 4) {
                    mediador.enviarMensajePrivado(partes[1], partes[2], partes[3]);
                }
                break;
            default:
                System.err.println("Comando no reconocido: " + comando);
        }
    }

    private void cerrarRecursos() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            System.err.println("Error cerrando el socket del cliente.");
        }
    }
}