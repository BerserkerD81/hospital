package com.hospital.Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
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

    public static void getHistorial(String usuario1, String usuario2) {
        String nombreUsuario1 = usuario1.compareTo(usuario2) < 0 ? usuario1 : usuario2;
        String nombreUsuario2 = usuario1.compareTo(usuario2) < 0 ? usuario2 : usuario1;

        String nombreArchivo = nombreUsuario1 + "_" + nombreUsuario2 + "_chat.txt";
        String rutaDirectorio = "src/main/java/com/hospital/Server/";

        File archivo = new File(rutaDirectorio + nombreArchivo);

        if (archivo.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                StringBuilder historial = new StringBuilder();
                while ((linea = br.readLine()) != null) {
                    historial.append(linea).append(",-");
                }

                // Enviar el historial al socket del usuario correspondiente si está en la lista
                Socket socketUsuario = usuariosConSockets.get(usuario1);
                Socket socketUsuario1 = usuariosConSockets.get(usuario2);

                if (socketUsuario != null) {
                    PrintWriter escritor = new PrintWriter(socketUsuario.getOutputStream(), true);
                    escritor.println("Historial entre*" + usuario1 + "*" + usuario2 +"*"+ historial.toString());
                    PrintWriter escritor1 = new PrintWriter(socketUsuario1.getOutputStream(), true);
                    escritor1.println("Historial entre*" + usuario1 + "*" + usuario2 +"*"+ historial.toString());  }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if (archivo.createNewFile()) {
                    System.out.println("El archivo " + nombreArchivo + " ha sido creado.");
                    getHistorial(usuario1,usuario2);
                } else {
                    System.out.println("No se pudo crear el archivo " + nombreArchivo + ".");
                }
            } catch (IOException e) {
                System.out.println("Ocurrió un error al intentar crear el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void updateHistorial(String usuario1, String usuario2, String mensaje) {
        String nombreUsuario1 = usuario1.compareTo(usuario2) < 0 ? usuario1 : usuario2;
        String nombreUsuario2 = usuario1.compareTo(usuario2) < 0 ? usuario2 : usuario1;

        String nombreArchivo = nombreUsuario1 + "_" + nombreUsuario2 + "_chat.txt";
        String rutaDirectorio = "src/main/java/com/hospital/Server/";

        File archivo = new File(rutaDirectorio + nombreArchivo);

        try (FileWriter fw = new FileWriter(archivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            // Agregar el mensaje al final del archivo
            bw.write((mensaje)+"\n");
            System.out.println("Mensaje agregado al historial correctamente.");
            getHistorial(usuario1,usuario2);
        } catch (IOException e) {
            System.out.println("Ocurrió un error al intentar actualizar el historial: " + e.getMessage());
            e.printStackTrace();
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

    static class ManejadorCliente implements Runnable {
        private Socket socket;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String nombreUsuario = lector.readLine();

                Server.usuariosConSockets.put(nombreUsuario, socket);
                Server.enviarListaUsuariosAClientes();

                while (true) {
                    String mensaje = lector.readLine();

                    if (mensaje == null) {
                        break;
                    }

                    if (mensaje.startsWith("/getHistorial")) {
                        String[] parts = mensaje.split(" ");
                        if (parts.length == 3) {
                            Server.getHistorial(parts[1], parts[2]);
                        } else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }
                    } else if (mensaje.startsWith("/updateHistorial")) {
                        String[] parts = mensaje.split(" ", 4);
                        if (parts.length == 4) {
                            Server.updateHistorial(parts[1], parts[2], parts[3]);
                        } else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }
                    } else {
                        enviarMensajeASockets(socket, "Comando desconocido");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    listaSockets.remove(socket);
                    Iterator<Map.Entry<String, Socket>> iterator = usuariosConSockets.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Socket> entry = iterator.next();
                        if (entry.getValue().equals(socket)) {
                            iterator.remove();
                            enviarListaUsuariosAClientes();
                            enviarMensajeDesconexion("Desconectado: " + entry.getKey());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void enviarMensajeASockets(Socket remitenteSocket, String mensaje) {
            for (Socket destinoSocket : Server.listaSockets) {
                try {
                    PrintWriter escritor = new PrintWriter(destinoSocket.getOutputStream(), true);
                    escritor.println("De " + remitenteSocket + ": " + mensaje);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void enviarMensajeDesconexion(String mensaje) {
            for (Socket destinoSocket : Server.listaSockets) {
                try {
                    PrintWriter escritor = new PrintWriter(destinoSocket.getOutputStream(), true);
                    escritor.println(mensaje);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
