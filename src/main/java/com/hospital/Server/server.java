package com.hospital.Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class server {
    private static final int PUERTO = 9090;
    public static Map<String, Socket> usuariosConSockets = new HashMap<>();
    public static List<Socket> listaSockets = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado. Esperando conexiones en el puerto " + PUERTO);

            while (true) {
                Socket cliente = serverSocket.accept();
                listaSockets.add(cliente);
                System.out.println("Nuevo cliente conectado: " + cliente);

                // Enviar la lista actualizada a todos los clientes
                enviarListaUsuariosAClientes();

                Thread hiloCliente = new Thread(new ManejadorCliente(cliente));
                hiloCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enviarListaUsuariosAClientes() {
        String listaUsuarios = obtenerListaUsuarios();
        for (Socket socket : listaSockets) {
            try {
                PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
                escritor.println(listaUsuarios);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String obtenerListaUsuarios() {
        StringBuilder lista = new StringBuilder();
        for (String usuario : usuariosConSockets.keySet()) {
            lista.append(usuario).append(", ");
        }
        if (lista.length() > 0) {
            lista.setLength(lista.length() - 2);
        }
        return lista.toString();
    }
}

class ManejadorCliente implements Runnable {
    private Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String nombreUsuario = lector.readLine();

            server.usuariosConSockets.put(nombreUsuario, socket);
            server.enviarListaUsuariosAClientes();

            while (true) {
                String mensaje = lector.readLine();
                if (mensaje == null) {
                    break;
                }
                System.out.println("Mensaje recibido de " + socket + ": " + mensaje);
                enviarMensajeASockets(socket, mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.listaSockets.remove(socket);
            List<String> usuariosDesconectados = new ArrayList<>();

            for (Map.Entry<String, Socket> entry : server.usuariosConSockets.entrySet()) {
                if (entry.getValue().equals(socket)) {
                    usuariosDesconectados.add(entry.getKey());
                    server.usuariosConSockets.remove(entry.getKey());
                    break;
                }
            }
            server.enviarListaUsuariosAClientes();

            if (!usuariosDesconectados.isEmpty()) {
                StringBuilder mensajeDesconexion = new StringBuilder("Desconectados: ");
                for (String usuario : usuariosDesconectados) {
                    mensajeDesconexion.append(usuario).append(", ");
                }
                mensajeDesconexion.setLength(mensajeDesconexion.length() - 2);

                enviarMensajeDesconexion(mensajeDesconexion.toString());
            }
        }
    }

    private void enviarMensajeASockets(Socket remitenteSocket, String mensaje) {
        for (Socket destinoSocket : server.listaSockets) {
            try {
                PrintWriter escritor = new PrintWriter(destinoSocket.getOutputStream(), true);
                escritor.println("De " + remitenteSocket + ": " + mensaje);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enviarMensajeDesconexion(String mensaje) {
        for (Socket destinoSocket : server.listaSockets) {
            try {
                PrintWriter escritor = new PrintWriter(destinoSocket.getOutputStream(), true);
                escritor.println(mensaje);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
