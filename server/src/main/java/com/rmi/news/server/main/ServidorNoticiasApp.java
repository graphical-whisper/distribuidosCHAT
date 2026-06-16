package com.rmi.news.server.main;

import com.rmi.news.server.service.ServicioNoticiasImpl;
import com.rmi.news.shared.IServicioNoticias;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServidorNoticiasApp {
    
    // IP estática de la VM 1
    private static final String IP_SERVIDOR = "10.10.1.129"; 
    private static final int PUERTO_RMI = 1099;

    public static void main(String[] args) {
        try {
            // Asegurar que RMI envíe la IP correcta a los clientes
            System.setProperty("java.rmi.server.hostname", IP_SERVIDOR);

            // Iniciar el registro RMI localmente
            LocateRegistry.createRegistry(PUERTO_RMI);
            System.out.println("Registro RMI iniciado en el puerto " + PUERTO_RMI);

            // Instanciar y vincular los servicios
            IServicioNoticias servicioNoticias = new ServicioNoticiasImpl();
            String rmiUrl = String.format("rmi://%s:%d/ServicioNoticias", IP_SERVIDOR, PUERTO_RMI);
            
            Naming.rebind(rmiUrl, servicioNoticias);
            System.out.println("Servicio de Noticias expuesto correctamente en: " + rmiUrl);

        } catch (Exception e) {
            System.err.println("Error crítico al iniciar el servidor RMI:");
            e.printStackTrace();
        }
    }
}