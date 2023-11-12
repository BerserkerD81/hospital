package com.hospital.Server;

import java.sql.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PUERTO = 9090;
    public static Map<User, Socket> usuariosConSockets = new HashMap<>();
    public static List<Socket> listaSockets = new CopyOnWriteArrayList<>();


    public static void main(String[] args) {
        //conecta con la base de datos
        String url = "jdbc:sqlite:src/main/java/resources/db/login.db";
        Connection connect;
        try {
            connect = DriverManager.getConnection(url);
            if (connect != null) {
                DatabaseMetaData meta = connect.getMetaData();
                System.out.println("El driver es " + meta.getDriverName());
                System.out.println("Se ha establecido una conexión con la base de datos");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //inicia el servidor
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

    public static Socket buascarSocket(String usuario)
    {
        Socket aux= null;
        Iterator<Map.Entry<User,Socket>> iterator= usuariosConSockets.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<User,Socket> entry =iterator.next();
            User user = entry.getKey();
            if(user.getUser().equals(usuario)){
                aux = entry.getValue();
                return aux;
            }
        }
        return null;
    }
    public static void getActiveUsers(String reciver) throws IOException {
        Socket Reciver =buascarSocket(reciver);
        PrintWriter escritor = new PrintWriter(Reciver.getOutputStream(), true);

        System.out.println((obtenerListaUsuarios()));
        escritor.println("/activos:"+ obtenerListaUsuarios().replaceAll("Usuarios:",""));



    }

    public static void getHistorial(String usuario1, String usuario2) {
        String nombreArchivo1 = usuario1 + "_" + usuario2 + "_chat.txt";
        String nombreArchivo2 = usuario2 + "_" + usuario1 + "_chat.txt"; // Fix the file naming for user2's perspective
        String rutaDirectorio = "src/main/java/com/hospital/Server/";

        File archivo1 = new File(rutaDirectorio + nombreArchivo1);
        File archivo2 = new File(rutaDirectorio + nombreArchivo2);

        try {
            if (!archivo1.exists() && archivo1.createNewFile()) {
                System.out.println("El archivo " + nombreArchivo1 + " ha sido creado.");
                // Carry on with operations if file creation is successful
            } else if (!archivo2.exists() && archivo2.createNewFile()) {
                System.out.println("El archivo " + nombreArchivo2 + " ha sido creado.");
                // Carry on with operations if file creation is successful
            } else {
                // Handle cases where files already exist or file creation fails
            }
        } catch (IOException e) {
            System.out.println("Ocurrió un error al intentar crear el archivo: " + e.getMessage());
            e.printStackTrace();
            return; // Return if file creation fails
        }

        if (archivo1.exists()) {
            try (BufferedReader br1 = new BufferedReader(new FileReader(archivo1))) {
                StringBuilder historial1 = new StringBuilder();
                String linea;
                while ((linea = br1.readLine()) != null) {
                    historial1.append(linea).append(",-");
                }

                // Send the first history to the corresponding user if they are in the list
                Socket socketUsuario1 = buascarSocket(usuario1);
                if (socketUsuario1 != null) {
                    PrintWriter escritor1 = new PrintWriter(socketUsuario1.getOutputStream(), true);
                    escritor1.println("Historial entre*" + usuario1 + "*" + usuario2 + "*" + historial1.toString());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (archivo2.exists()) {
            try (BufferedReader br2 = new BufferedReader(new FileReader(archivo2))) {
                StringBuilder historial2 = new StringBuilder();
                String linea2;
                while ((linea2 = br2.readLine()) != null) {
                    historial2.append(linea2).append(",-");
                }

                // Send the second history to the corresponding user if they are in the list
                Socket socketUsuario2 = buascarSocket(usuario2);
                if (socketUsuario2 != null) {
                    PrintWriter escritor2 = new PrintWriter(socketUsuario2.getOutputStream(), true);
                    escritor2.println("Historial entre*" + usuario2 + "*" + usuario1 + "*" + historial2.toString());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void gruposDisponibles(String user) throws IOException {
        Socket socketuser =buascarSocket(user);
        PrintWriter escritor = new PrintWriter(socketuser.getOutputStream(), true);
        String aux= "";
        StringBuilder medicos= new StringBuilder();
        StringBuilder todos = new StringBuilder();
        Iterator<Map.Entry<User,Socket>> iterator= usuariosConSockets.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<User,Socket> entry =iterator.next();
            User usuario = entry.getKey();
            if(usuario.getUser().equals(user)){
                aux=usuario.getType();
                break;
            }
        }
        Iterator<Map.Entry<User,Socket>> iterator1= usuariosConSockets.entrySet().iterator();
        while (iterator1.hasNext())
        {
            Map.Entry<User,Socket> entry =iterator1.next();
            User usuario = entry.getKey();

            if(!usuario.getUser().equals(user))
            {todos.append(usuario.getUser()).append(" ");}
            if(usuario.getType().equals("medico")){
                medicos.append(usuario.getUser()).append(" ");
            }
        }


        System.out.println("el usuario:"+user+"es de tipo "+aux);
        if (!aux.equals("medico")) {
            if(aux.equals("aseo")){
                escritor.println("Grupo:" + aux+":"+todos);
            } else if (aux.equals("admin")) {
                escritor.println("Grupo:" +"URGENTE");

            } else {
                escritor.println("Grupo:" + aux+" "+"aseo"+":"+medicos);

            }

        }
        else {
            escritor.println("Grupo:" + aux+" "+"admision"+" "+"examenes"+" "+"pabellon"+" "+"aseo");
        }
    }

    public static void updateHistorial(String usuario1, String usuario2, String mensaje) {

        String nombreArchivo1 = usuario1 + "_" + usuario2 + "_chat.txt";
        String nombreArchivo2 = usuario2 + "_" + usuario1 + "_chat.txt"; // Cambiado el nombre del segundo archivo

        String rutaDirectorio = "src/main/java/com/hospital/Server/";

        File archivo1 = new File(rutaDirectorio + nombreArchivo1);
        File archivo2 = new File(rutaDirectorio + nombreArchivo2);

        try (FileWriter fw1 = new FileWriter(archivo1, true);
             BufferedWriter bw1 = new BufferedWriter(fw1)) {

            // Agregar el mensaje al final del archivo 1
            bw1.write(mensaje + "\n");
            System.out.println("Mensaje agregado al historial de " + usuario1 + " y " + usuario2 + " correctamente.");

             getHistorial(usuario1, usuario2);
             getHistorial(usuario2, usuario1);

        } catch (IOException e) {
            System.out.println("Ocurrió un error al intentar actualizar el historial de " + usuario1 + " y " + usuario2 + ": " + e.getMessage());
            e.printStackTrace();
        }

        try (FileWriter fw2 = new FileWriter(archivo2, true);
             BufferedWriter bw2 = new BufferedWriter(fw2)) {

            // Agregar el mensaje al final del archivo 2
            bw2.write(mensaje + "\n");
            System.out.println("Mensaje agregado al historial de " + usuario2 + " y " + usuario1 + " correctamente.");
             getHistorial(usuario1, usuario2);
             getHistorial(usuario2, usuario1);

        } catch (IOException e) {
            System.out.println("Ocurrió un error al intentar actualizar el historial de " + usuario2 + " y " + usuario1 + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void getHistorialGrupal(String grupo)
    {
        {

            ArrayList<String> GroupUsers =new ArrayList<String>();


            Iterator<Map.Entry<User,Socket>> iterator= usuariosConSockets.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<User,Socket> entry =iterator.next();
                User user = entry.getKey();
                    if(grupo.equals("aseo")){
                    GroupUsers.add(user.getUser());}
                    else if (grupo.equals("admision")) {
                        if (user.getType().equals("admision")|| user.getType().equals("medico"))
                        {
                            GroupUsers.add(user.getUser());
                        }
                    }
                    else if (grupo.equals("pabellon")) {
                        if (user.getType().equals("pabellon")|| user.getType().equals("medico"))
                        {
                            GroupUsers.add(user.getUser());
                        }

                    }
                    else if (grupo.equals("examenes")) {
                        if (user.getType().equals("examenes")|| user.getType().equals("medico"))
                        {
                            GroupUsers.add(user.getUser());
                        }
                    }
                    else if (grupo.equals("medico")) {
                        if ( user.getType().equals("medico"))
                        {
                            GroupUsers.add(user.getUser());
                        }
                    }

                    else if (grupo.equals("URGENTE")) {
                            if ( user.getType().equals("admin")){
                                GroupUsers.add(user.getUser());
                            }


                    }

            }
            System.out.println("los usuarios del grupo"+grupo+" son: "+GroupUsers.toString());

            for (String user : GroupUsers) {
                String fileName ="src/main/java/com/hospital/Server/"+ user + "_" + grupo + "_chat.txt"; // Creating the file name
                File file = new File(fileName);

                if (file.exists()) {
                    try (BufferedReader br1 = new BufferedReader(new FileReader(file))) {
                    StringBuilder historial1 = new StringBuilder();
                    String linea;
                    while ((linea = br1.readLine()) != null) {
                        historial1.append(linea).append(",-");
                    }

                    // Send the first history to the corresponding user if they are in the list
                    Socket socketUsuario1 = buascarSocket(user);
                    if (socketUsuario1 != null) {
                        PrintWriter escritor1 = new PrintWriter(socketUsuario1.getOutputStream(), true);
                        escritor1.println("Historial entre*" + user + "*" + grupo + "*" + historial1.toString());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                } else {
                    try {
                        if (file.createNewFile()) {
                            System.out.println("File created: " + fileName);
                            // Perform operations with the newly created file
                        } else {
                            System.out.println("Failed to create file: " + fileName);
                            // Handle the failure to create the file
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }
    }
    public static void updateHistorialGrupo( String grupo, String mensaje) {

        ArrayList<String> GroupUsers =new ArrayList<String>();


        Iterator<Map.Entry<User,Socket>> iterator= usuariosConSockets.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<User,Socket> entry =iterator.next();
            User user = entry.getKey();
            if(grupo.equals("aseo")){
                GroupUsers.add(user.getUser());}
            else if (grupo.equals("admision")) {
                if (user.getType().equals("admision")|| user.getType().equals("medico"))
                {
                    GroupUsers.add(user.getUser());
                }
            }
            else if (grupo.equals("pabellon")) {
                if (user.getType().equals("pabellon")|| user.getType().equals("medico"))
                {
                    GroupUsers.add(user.getUser());
                }

            }
            else if (grupo.equals("examenes")) {
                if (user.getType().equals("examenes")|| user.getType().equals("medico"))
                {
                    GroupUsers.add(user.getUser());
                }
            }
            else if (grupo.equals("medico")) {
                if ( user.getType().equals("medico"))
                {
                    GroupUsers.add(user.getUser());
                }
            }

            else if (grupo.equals("URGENTE")) {
                if ( user.getType().equals("admin")){
                    GroupUsers.add(user.getUser());
                }
            }
        }
        System.out.println("los usuarios del grupo"+grupo+" son: "+GroupUsers.toString());

        for (String user : GroupUsers) {
            String fileName = "src/main/java/com/hospital/Server/"+user + "_" + grupo + "_chat.txt"; // Creating the file name
            File file = new File(fileName);

            if (file.exists()) {
                try {
                    FileWriter fileWriter = new FileWriter(file, true); // Set true for append mode
                    BufferedWriter writer = new BufferedWriter(fileWriter);
                    writer.write(mensaje+"\n"); // New line of text
                    writer.close();
                    System.out.println("New line appended to file: " + fileName);
                    // Perform operations with the file (if needed)
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (file.createNewFile()) {
                        System.out.println("File created: " + fileName);
                        updateHistorialGrupo(grupo,mensaje);
                        // Perform operations with the newly created file
                    } else {
                        System.out.println("Failed to create file: " + fileName);
                        // Handle the failure to create the file
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String obtenerListaUsuarios() {
        StringBuilder lista = new StringBuilder();
        for (User usuario : usuariosConSockets.keySet()) {
            lista.append(usuario.getUser()).append("-").append(usuario.getType()).append(", ");
        }
        if (lista.length() > 0) {
            lista.setLength(lista.length() - 2);
        }
        return "Usuarios:"+lista.toString();
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
                String message = lector.readLine();
                if(message.startsWith("/sendUser")){

                    String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        User userAux = new User(parts[1], parts[2]);
                        Server.usuariosConSockets.put(userAux, socket);
                        Server.enviarListaUsuariosAClientes();

                    } else {
                        enviarMensajeASockets(socket, "Comando incorrecto");
                    }



                }





                while (true) {
                    String mensaje = lector.readLine();
                    System.out.println(mensaje);

                    if (mensaje == null) {
                        break;
                    } else if (mensaje.startsWith("/getUser")) {
                        String[] parts = mensaje.split(" ");
                        if (parts.length == 2) {
                            getActiveUsers(parts[1]);
                        } else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }
                    } else if (mensaje.startsWith("/UpdateHistorialEspecifico")) {
                        System.out.println("llegue");
                        String[] parts = mensaje.split(" ",5);
                        System.out.println(parts.length);

                        if (parts.length==5){
                            updateHistorialEspecifico(parts[3],parts[1],parts[2],parts[4]);
                            getHistorialGrupal(parts[3]);
                        }



                    }
                    else if (mensaje.startsWith("/updateHistorialGrupal")) {
                        String[] parts = mensaje.split(" ",3);
                        System.out.println(parts.length);
                        if (parts.length ==3) {
                            if(parts[1].equals("URGENTE"))
                            {
                                updateHistorialGrupo("URGENTE",parts[2]);
                                updateHistorialGrupo("medico",parts[2]);
                                updateHistorialGrupo("aseo",parts[2]);
                                updateHistorialGrupo("admision",parts[2]);
                                updateHistorialGrupo("pabellon",parts[2]);
                                updateHistorialGrupo("examenes",parts[2]);
                            }
                            updateHistorialGrupo(parts[1],parts[2]);
                        } else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }

                    }
                    else if (mensaje.startsWith("/getHistorialGrupal")) {
                        String[] parts = mensaje.split(" ");
                        if (parts.length == 2) {
                            if (parts[1].equals("URGENTE"))
                            {
                                getHistorialGrupal("medico");
                                getHistorialGrupal("aseo");
                                getHistorialGrupal("admision");
                                getHistorialGrupal("pabellon");
                                getHistorialGrupal("examenes");
                                getHistorialGrupal("URGENTE");


                            }
                            else {
                            Server.getHistorialGrupal(parts[1]);
                            }
                        } else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }
                    }
                    else if (mensaje.startsWith("/getHistorial")) {
                        String[] parts = mensaje.split(" ");
                        if (parts.length == 3) {
                                System.out.println(parts[2]);
                                Server.getHistorial(parts[1], parts[2]);
                        }
                        else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }
                    } else if (mensaje.startsWith("/updateHistorial")) {
                        String[] parts = mensaje.split(" ", 4);
                        if (parts.length == 4) {
                             Server.updateHistorial(parts[1], parts[2], parts[3]);
                        }
                        else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }
                    }
                    else if (mensaje.startsWith("/getGroup")) {
                        String[] parts = mensaje.split(" ");
                        if (parts.length == 2) {
                            gruposDisponibles(parts[1]);
                        } else {
                            enviarMensajeASockets(socket, "Comando incorrecto");
                        }

                    } else if (mensaje.startsWith("/BorrarHistorial")) {
                        String parts[] = mensaje.split(" ");
                        if (parts.length == 4)
                        {
                            borrarHistorial(parts[1],parts[2]);
                            if(parts[3].equals("true"))
                            {
                                System.out.println("legue");
                                getHistorial(parts[1],parts[2]);
                            }
                            else if(parts[3].equals("false")){
                                getHistorialGrupal(parts[2]);
                            }
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
                    Iterator<Map.Entry<User, Socket>> iterator = usuariosConSockets.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<User, Socket> entry = iterator.next();
                        if (entry.getValue().equals(socket)) {
                            iterator.remove();
                            enviarListaUsuariosAClientes();
                            enviarMensajeDesconexion("Desconectado: " + entry.getKey().getUser()+"-"+entry.getKey().getType());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateHistorialEspecifico(String grupo, String user1, String user2,String mensaje) {

            String nombreArchivo1 = user1 + "_" + grupo + "_chat.txt";
            String nombreArchivo2 = user2 + "_" + grupo + "_chat.txt"; // Cambiado el nombre del segundo archivo

            String rutaDirectorio = "src/main/java/com/hospital/Server/";

            File archivo1 = new File(rutaDirectorio + nombreArchivo1);
            File archivo2 = new File(rutaDirectorio + nombreArchivo2);

            try (FileWriter fw1 = new FileWriter(archivo1, true);
                 BufferedWriter bw1 = new BufferedWriter(fw1)) {

                // Agregar el mensaje al final del archivo 1
                bw1.write(mensaje + "\n");
                System.out.println("Mensaje agregado al historial de " + user1 + " y " + grupo + " correctamente.");

            } catch (IOException e) {
                System.out.println("Ocurrió un error al intentar actualizar el historial de " + user1 + " y " + grupo + ": " + e.getMessage());
                e.printStackTrace();
            }

            try (FileWriter fw2 = new FileWriter(archivo2, true);
                 BufferedWriter bw2 = new BufferedWriter(fw2)) {

                // Agregar el mensaje al final del archivo 2
                bw2.write(mensaje + "\n");
                System.out.println("Mensaje agregado al historial de " + user2 + " y " + grupo + " correctamente.");

            } catch (IOException e) {
                System.out.println("Ocurrió un error al intentar actualizar el historial de " + user2 + " y " + grupo + ": " + e.getMessage());
                e.printStackTrace();
            }


        }
        private void borrarHistorial(String parte1, String parte2)
        {
            String nombreArchivo1 = parte1 + "_" + parte2 + "_chat.txt";
            String rutaDirectorio = "src/main/java/com/hospital/Server/";
            File archivo1 = new File(rutaDirectorio + nombreArchivo1);
            if(archivo1.exists())
            {
                archivo1.delete();
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
