package com;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorMensajeria implements MediadorServidor {
    private static final int PUERTO = 5000;
    
    // El servidor guarda referencias a los objetos ClientHandler en lugar de PrintWriters crudos
    private final ConcurrentHashMap<String, ClientHandler> clientesConectados = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new ServidorMensajeria().iniciarServidor();
    }

    public void iniciarServidor() {
        System.out.println("Servidor de mensajería (Mediador) iniciado en el puerto " + PUERTO + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("[!] Conexión TCP establecida: " + socketCliente.getInetAddress().getHostAddress());
                
                // Inyecta el mediador (this) en el manejador
                ClientHandler manejador = new ClientHandler(socketCliente, this);
                new Thread(manejador).start();
            }
        } catch (Exception e) {
            System.err.println("Error crítico en el listener del servidor: " + e.getMessage());
        }
    }

    @Override
    public synchronized void registrarCliente(String nombre, ClientHandler handler) {
        clientesConectados.put(nombre, handler);
        System.out.println("[+] Sistema: " + nombre + " se ha conectado.");
        difundirListaUsuarios();
    }

    @Override
    public synchronized void eliminarCliente(String nombreUsuario) {
        if (nombreUsuario != null && clientesConectados.containsKey(nombreUsuario)) {
            clientesConectados.remove(nombreUsuario);
            System.out.println("[-] Sistema: " + nombreUsuario + " se ha desconectado.");
            difundirListaUsuarios();
        }
    }

    @Override
    public void difundirMensaje(String mensajeFormateado) {
        for (ClientHandler handler : clientesConectados.values()) {
            handler.enviarDatos(mensajeFormateado);
        }
    }

    @Override
    public void enviarMensajePrivado(String emisor, String receptor, String contenido) {
        ClientHandler handlerReceptor = clientesConectados.get(receptor);
        ClientHandler handlerEmisor = clientesConectados.get(emisor);
        
        if (handlerReceptor != null) {
            String msjSalida = "PRIVMSG;" + emisor + ";" + contenido;
            handlerReceptor.enviarDatos(msjSalida);
            
            if (handlerEmisor != null) {
                handlerEmisor.enviarDatos(msjSalida); // Copia para la GUI local del emisor
            }
        } else if (handlerEmisor != null) {
            handlerEmisor.enviarDatos("ERROR;El usuario [" + receptor + "] no se encuentra en línea.");
        }
    }

    private void difundirListaUsuarios() {
        StringBuilder constructLista = new StringBuilder("USERS");
        for (String usuario : clientesConectados.keySet()) {
            constructLista.append(";").append(usuario);
        }
        difundirMensaje(constructLista.toString());
    }
}