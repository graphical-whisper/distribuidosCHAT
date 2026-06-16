package com.sockets.news.db.main;

import com.sockets.news.db.handler.ProcesadorPeticiones;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorDatosApp {

    private static final int PUERTO = 5000;
    private static final String IP_PERMITIDA_VM1 = "10.10.1.129";
    // Pool de hilos para atender múltiples peticiones concurrentes de la VM 1
    private static final ExecutorService poolHilos = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("Iniciando Servidor de Datos en la IP 10.10.1.130...");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Escuchando en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = serverSocket.accept();
                String ipOrigen = socketCliente.getInetAddress().getHostAddress();

                // Validación estricta de origen (Seguridad de red a nivel de aplicación)
                if (!ipOrigen.equals(IP_PERMITIDA_VM1)) {
                    System.err.println("Conexión rechazada desde IP no autorizada: " + ipOrigen);
                    socketCliente.close();
                    continue;
                }

                // Delegar la petición a un hilo de trabajo
                poolHilos.execute(new ProcesadorPeticiones(socketCliente));
            }
        } catch (IOException e) {
            System.err.println("Error crítico en el Servidor de Datos: " + e.getMessage());
        }
    }
}