package com;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClienteSwing extends JFrame implements ClienteListener {
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private DefaultListModel<String> modeloUsuarios;
    private JList<String> listaUsuarios;
    private ServicioRedCliente servicioRed;
    private final String nombreUsuario;

    public ClienteSwing(String ipServidor, String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        configurarInterfaz();
        
        // Inyección de dependencias: se pasa la instancia de la vista como listener
        this.servicioRed = new ServicioRedCliente(ipServidor, 5000, nombreUsuario, this);
        this.servicioRed.conectar();
    }

    private void configurarInterfaz() {
        setTitle("Chat - " + nombreUsuario);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        modeloUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setPreferredSize(new Dimension(150, 0));
        scrollUsuarios.setBorder(BorderFactory.createTitledBorder("Usuarios"));
        add(scrollUsuarios, BorderLayout.EAST);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        JButton botonEnviar = new JButton("Send");

        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        botonEnviar.addActionListener(e -> gestionarEnvio());
        campoMensaje.addActionListener(e -> gestionarEnvio());
    }

    private void gestionarEnvio() {
        String texto = campoMensaje.getText().trim();
        if (texto.isEmpty()) return;

        String destinatario = listaUsuarios.getSelectedValue();
        if (destinatario != null) {
            servicioRed.enviarMensajePrivado(destinatario, texto);
            listaUsuarios.clearSelection();
        } else {
            servicioRed.enviarMensajeGlobal(texto);
        }
        campoMensaje.setText("");
    }

    // Métodos del contrato ClienteListener (Thread-Safe)
    @Override
    public void onMensajeRecibido(String emisor, String contenido) {
        SwingUtilities.invokeLater(() -> areaChat.append(emisor + ": " + contenido + "\n"));
    }

    @Override
    public void onMensajePrivadoRecibido(String emisor, String contenido) {
        SwingUtilities.invokeLater(() -> areaChat.append("[Privado de " + emisor + "]: " + contenido + "\n"));
    }

    @Override
    public void onListaUsuariosActualizada(List<String> usuarios) {
        SwingUtilities.invokeLater(() -> {
            modeloUsuarios.clear();
            for (String usuario : usuarios) {
                modeloUsuarios.addElement(usuario);
            }
        });
    }

    @Override
    public void onErrorRecibido(String mensajeError) {
        SwingUtilities.invokeLater(() -> areaChat.append("SISTEMA: " + mensajeError + "\n"));
    }

    @Override
    public void onDesconexion() {
        SwingUtilities.invokeLater(() -> areaChat.append("[-] Desconectado del servidor.\n"));
    }

    public static void main(String[] args) {
        String ipServidor = JOptionPane.showInputDialog("Ingrese la IP del servidor:", "10.10.1.129");
        if (ipServidor == null || ipServidor.trim().isEmpty()) return;

        String usuario = JOptionPane.showInputDialog("Ingrese su nombre de usuario:");
        if (usuario == null || usuario.trim().isEmpty()) return;

        SwingUtilities.invokeLater(() -> {
            ClienteSwing cliente = new ClienteSwing(ipServidor, usuario);
            cliente.setVisible(true);
        });
    }
}