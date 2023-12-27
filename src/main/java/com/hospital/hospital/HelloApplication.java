    package com.hospital.hospital;
    import java.sql.*;
    import javafx.application.Application;
    import javafx.application.Platform;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXMLLoader;
    import javafx.geometry.Insets;
    import javafx.scene.Node;
    import javafx.scene.Scene;
    import javafx.scene.control.*;
    import javafx.scene.effect.Bloom;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.AnchorPane;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.Pane;
    import javafx.scene.layout.VBox;
    import javafx.scene.paint.Color;
    import javafx.scene.text.*;
    import javafx.stage.Stage;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import java.net.Socket;
    import java.util.*;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    import java.util.stream.Stream;
    import static java.lang.System.identityHashCode;
    import static java.lang.System.out;
    public class HelloApplication extends Application {
        private boolean dm=false;
        private VBox chatPlace;
        public ImageView buttonsend;
        public String userName;
        public TextField mesaggeinput;
        public TextFlow messagelog;
        private Socket socket;
        private PrintWriter out;
        public String talkingTo="*";
        private  ArrayList<String> usuariosActivos = new ArrayList<>();
        private  ArrayList<String> gruposActivos = new ArrayList<>();
        public PrintWriter writer;
        public BufferedReader reader;
        //Botones pal login
        private Button botonLogin;
        private TextField login_user;
        private TextField login_password;
        private Hyperlink login_register;
        //Variable chafa, esto es por mientras
        private boolean inicio = false;
        private Boolean change =false;
        private String userType;
        private ImageView searchButton;
        private TextField searchBar;
        private String filter;
        private VBox menuBar;
        private HBox messageButton;
        private HBox groupButton;
        private HBox accountButton;
        private HBox searchBox;
        private HBox chatBar;
        //variables para el registro
        private Button botonRegistro;
        private HBox adminButton;
        private Pane paneBox;
        private TextField registro_user;
        private TextField registro_password;
        private TextField registro_Checkpassword;
        private AnchorPane panelRegistro;
        private HBox deleteButton;
        private AnchorPane estadisticasPane;

        private boolean connected = true; // Variable para controlar la conexión

        private Scene scene;
        @Override
        public void start(Stage stage) throws IOException {
            // Pedir al usuario que ingrese su nombre por consola
            FXMLLoader escena_login = new FXMLLoader(HelloApplication.class.getResource("Inicio_sesion.fxml"));
            Scene login = new Scene(escena_login.load(), 419, 342);
            stage.setTitle("Iniciar Sesión");
            stage.setScene(login);
            stage.show();
            botonLogin = (Button) login.lookup("#login_accept");
            login_user = (TextField) login.lookup("#login_user");
            login_password = (TextField) login.lookup("#login_password");
            login_register =  (Hyperlink) login.lookup("#login_register");
            FXMLLoader registro = new FXMLLoader(HelloApplication.class.getResource("registrar_usuario.fxml"));
            Scene registro_view = new Scene(registro.load(), 419, 342);
            //cambiar a solo administrador
            login_register.setOnAction(e ->{
                stage.setTitle("Registrarse");
                stage.setScene(registro_view);
                stage.show();
            });
            botonRegistro = (Button) registro_view.lookup("#botonRegistro");
            registro_user = (TextField) registro_view.lookup("#registro_user");
            registro_password = (TextField) registro_view.lookup("#registro_pass");
            registro_Checkpassword = (TextField) registro_view.lookup("#registro_check");
            //Aqui añadir el usuario a la BD
            botonRegistro.setOnMouseClicked(e -> {
                stage.setTitle("Iniciar Sesión");
                stage.setScene(login);
                stage.show();
            });
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
             scene = new Scene(fxmlLoader.load(), 1080, 720);
            botonLogin.setOnMouseClicked(e -> {
                String user = login_user.getText();
                this.userName = user;
                String pass = login_password.getText();
                //Aqui hacer verificaciones
                inicio = true;
                //consultar base de datos (por ahora sin el server)
                String url = "jdbc:mariadb://35.226.170.116:3306/hospital";
                String usr = "user";
                String password = "discordp";
                Connection connect;
                ResultSet result = null;
                //si el usuario y contraseña son correctos se abre la ventana de chat
                try {
                    connect = DriverManager.getConnection(url, usr, password);
                    if(!change){
                        if (connect != null  ) {
                            DatabaseMetaData meta = connect.getMetaData();
                            System.out.println("El driver es " + meta.getDriverName());
                            System.out.println("Se ha establecido una conexión con la base de datos");
                            //consulta de verificacion de usuario
                            PreparedStatement st = connect.prepareStatement("SELECT * FROM usuarios WHERE name = ? AND password = ?");
                            st.setString(1, user);
                            st.setString(2, pass);
                            result = st.executeQuery();
                            //respuesta de la consulta
                            if (result.next()) {
                                if(result.getInt("Ftime")==0)
                                {
                                    System.out.println(" usuario conectado!!!");
                                    this.userName = user;
                                    this.userType =result.getString("tipoUsuario");
                                    writer.println("/sendUser "+userName+" "+userType);
                                    stage.setTitle("Chat Hospital");
                                    stage.setScene(scene);
                                    stage.show();
                                    if (!userType.equals("admin"))
                                    {
                                        adminButton.setVisible(false);
                                        accountButton.setVisible(false);
                                        if (userType.equals("aseo")){
                                            messageButton.setVisible(false);
                                        }
                                    }
                                    else
                                    {
                                        messageButton.setVisible(false);
                                    }
                                    change = false;
                                    connect.close();
                                }
                                else
                                {
                                    System.out.println("cambia la pass");
                                    // Cambios aquí:
                                    login_user.setVisible(false); // Hacer invisible el campo login_user
                                    Text text1 = (Text) login.lookup("#text1");
                                    Text text2 = (Text) login.lookup("#text2");
                                    Text text3 = (Text) login.lookup("#text3");
                                    text1.setVisible(false);
                                    text2.setText("Ingresa tu nueva pass");
                                    text2.setLayoutX(1);
                                    text3.setText("Cambia tu contraseña para iniciar");
                                    text3.setLayoutX(100);
                                    change= true;
                                    this.userName = user;
                                    this.userType =result.getString("tipoUsuario");
                                    connect.close();
                                }
                            } else {
                                System.out.println("Usuario o contraseña incorrectos");
                                System.out.println("Usuario: " + user + " Contraseña: " + pass);
                            }
                        }
                    }
                    else
                    {
                        String newPassword = login_password.getText();
                        if(!newPassword.isEmpty()) {
                            // Actualizar la contraseña del usuario
                            PreparedStatement updatePassword = connect.prepareStatement("UPDATE usuarios SET password = ?, Ftime = ? WHERE name = ?");
                            updatePassword.setString(1, newPassword);
                            updatePassword.setInt(2, 0); // Asignar el nuevo valor para Ftime (puedes cambiarlo según tus necesidades)
                            updatePassword.setString(3, user);
                            updatePassword.executeUpdate();
                            updatePassword.executeUpdate();
                            updatePassword.close();
                            System.out.println(" usuario conectado!!!");
                            writer.println("/sendUser "+userName+" "+userType);
                            stage.setTitle("Chat Hospital");
                            stage.setScene(scene);
                            stage.show();
                            if (!userType.equals("admin"))
                            {
                                adminButton.setVisible(false);
                                accountButton.setVisible(false);
                                if (userType.equals("aseo")){
                                    messageButton.setVisible(false);
                                }
                            }
                            else
                            {
                                messageButton.setVisible(false);
                            }
                            // Cambios aquí:
                            change = false;
                            connect.close();
                        }
                    }
                }
                    catch (SQLException ex) {
                        System.out.println(ex.getMessage());
                }
            });
            chatPlace = (VBox) scene.lookup("#chat_place");
            buttonsend = (ImageView) scene.lookup("#send_button");
            mesaggeinput = (TextField) scene.lookup("#message_bar");
            messagelog =(TextFlow) scene.lookup("#message_area");
            searchBar = (TextField) scene.lookup("#search_bar");
            searchButton = (ImageView)scene.lookup("#search_button");
            messageButton = (HBox) scene.lookup("#messages_button");
            groupButton = (HBox) scene.lookup("#group_button");
            accountButton = (HBox) scene.lookup("#account_button");
            menuBar = (VBox) scene.lookup("#menu_bar");
            searchBox =(HBox) scene.lookup("#searchBox");
            adminButton = (HBox) scene.lookup("#admin_button");
            chatBar = (HBox) scene.lookup("#chat_bar");
            paneBox =(Pane) scene.lookup("#paneBox");
            AnchorPane registopane = createCustomAnchorPane();
            AnchorPane estadisticasPane = estadisticasPanel();
            paneBox.getChildren().add(registopane);
            paneBox.getChildren().add(estadisticasPane);
            this.estadisticasPane = (AnchorPane) scene.lookup("#estadisticas");
            panelRegistro =(AnchorPane) scene.lookup("#panelRegistro");
            panelRegistro.setVisible(false);
            estadisticasPane.setVisible(false);
            deleteButton =(HBox) scene.lookup("#delete_button");
            deleteButton.setVisible(false);
            Button boton_registro = (Button) panelRegistro.lookup("#botonRegistro");
            TextField registro_user = (TextField) panelRegistro.lookup("#registro_user");
            TextField registro_rut = (TextField) panelRegistro.lookup("#registro_rut");
            TextField registro_correo = (TextField) panelRegistro.lookup("#registro_correo");
            TextField registro_pass = (TextField) panelRegistro.lookup("#registro_pass");
            TextField registro_check = (TextField) panelRegistro.lookup("#registro_check");
            ComboBox<String> registro_tipo = (ComboBox) panelRegistro.lookup("#registro_tipo");
            boton_registro.setOnMouseClicked(e -> {
                String user = registro_user.getText();
                user = user.replace(" ", "_");
                String rut = registro_rut.getText();
                String correo = registro_correo.getText();
                String pass = registro_pass.getText();
                String check = registro_check.getText();
                String tipo = registro_tipo.getValue();
                Integer aux =1;
                String url = "jdbc:mariadb://35.226.170.116:3306/hospital";
                String usr = "user";
                String password = "discordp";
                Connection connect;
                int ID = 0;
                try {
                    connect = DriverManager.getConnection(url,usr,password);
                    Statement statement = connect.createStatement();
                    if(Objects.equals(pass, check)){
                        if(connect != null){
                            ResultSet st = statement.executeQuery("SELECT MAX(id) AS max_id FROM usuarios;");
                            if(st.next()){
                                int maxID = st.getInt("max_id");
                                ID = maxID + 1;
                            }
                            System.out.println("ID actual"+ID);
                            PreparedStatement algo = connect.prepareStatement("INSERT INTO usuarios (ID,name, correo, password, rut,tipoUsuario,Ftime) " + "VALUES (?, ?, ?, ?, ?, ?, ?)");
                            algo.setInt(1,ID);
                            algo.setString(2, user);
                            algo.setString(3, correo);
                            algo.setString(4, pass);
                            algo.setString(5, rut);
                            algo.setString(6, tipo);
                            algo.setInt(7,aux);
                            algo.executeUpdate();
                        }
                    }
                    connect.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
            deleteButton.setOnMouseClicked(e -> {
                writer.println("/BorrarHistorial"+" "+userName+" "+talkingTo+" "+dm);
                messagelog.getChildren().clear();
            });
            buttonsend.setOnMouseClicked(e -> {
                String message = mesaggeinput.getText(); // Get the text from the input field
                mesaggeinput.setText("");
                System.out.println("talking to: "+talkingTo);
                if(dm){
                chat(userName,talkingTo,userName+":"+message);
                solicitarHistorial(this.userName,talkingTo);
            }
                else {
                    if(userType.equals("medico") || userType.equals("admin")){
                    chatGrupo(talkingTo,userName+":"+message);
                    solicitarHistorialGrupo(talkingTo);
                    }
                    else {
                        HBox aux = (HBox) chatPlace.getChildren().get(2);
                        ComboBox aux1 = (ComboBox) aux.getChildren().get(1);
                        if (aux1.getSelectionModel().isEmpty()) {
                            chatGrupo(talkingTo, userName + ":" + message);
                            solicitarHistorialGrupo(talkingTo);
                        }
                        else {
                            String Selected = (String) aux1.getSelectionModel().getSelectedItem();
                            System.out.println("answering to " + Selected);
                            if (!Selected.equals("todos")) {
                                String mensaje = userName + ":" + message;
                                writer.println("/UpdateHistorialEspecifico" + " " + userName + " " + Selected + " " + this.talkingTo
                                        + " " + mensaje);
                                solicitarHistorialGrupo(talkingTo);
                            } else {
                                chatGrupo(talkingTo, userName + ":" + message);
                                solicitarHistorialGrupo(talkingTo);
                            }
                        }
                    }
                }
            });
            searchButton.setOnMouseClicked(e -> {
                String message = searchBar.getText(); // Get the text from the input field
                System.out.println("buscando usuarios que contengan la sig cadena "+message);
                writer.println("/getUser"+" "+userName);
            });
            messageButton.setOnMouseClicked(e -> {
                for (Node node : menuBar.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                messageButton.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                searchBox.setVisible(true);
                searchBar.setText("");
                dm=true;
                writer.println("/getUser"+" "+userName);
                messagelog.setVisible(true);
                chatBar.setVisible(true);
                panelRegistro.setVisible(false);
                estadisticasPane.setVisible(false);
                deleteButton.setVisible(true);
            });
            groupButton.setOnMouseClicked(e -> {
                for (Node node : menuBar.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                deleteButton.setVisible(true);
                gruposActivos.clear();
                groupButton.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                searchBox.setVisible(false);
                chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                dm=false;
                writer.println("/getGroup "+userName);
                System.out.println("Listar Grupos");
                messagelog.setVisible(true);
                chatBar.setVisible(true);
                panelRegistro.setVisible(false);
                estadisticasPane.setVisible(false);
                System.out.println(panelRegistro);
            });
            accountButton.setOnMouseClicked(e -> {
                for (Node node : menuBar.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                deleteButton.setVisible(false);
                accountButton.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                searchBox.setVisible(false);
                chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                usuariosActivos.clear();
                messagelog.setVisible(false);
                chatBar.setVisible(false);
                panelRegistro.setVisible(false);
                estadisticasPane.setVisible(true);
                writer.println("/EstadistcasUser "+userName);
            });
            adminButton.setOnMouseClicked(e -> {
                for (Node node : menuBar.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                deleteButton.setVisible(false);
                adminButton.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                searchBox.setVisible(false);
                chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                usuariosActivos.clear();
                messagelog.setVisible(false);
                chatBar.setVisible(false);
                panelRegistro.setVisible(true);
                estadisticasPane.setVisible(false);
            });
            // Conectarse al servidor
            try {
                // Conectarse al servidor
                socket = new Socket("34.30.76.57", 8080); // Reemplazar "localhost" con la IP real si es necesario
                // Resto del código para la comunicación con el servidor
                IniciarEscucha();
                // ...
            } catch (IOException e) {
                System.out.println("Se perdió la conexión con el servidor: " + e.getMessage());
                reconnect();
                e.printStackTrace();
            }

            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            // Setting up a thread to continuously listen for server messages

        }
        private void reconnect() {
            connected = true;
            while (connected) {
                try {
                    socket.close(); // Cerrar socket existente
                    Thread.sleep(3000); // Esperar 3 segundos antes de reconectar
                    socket = new Socket("34.30.76.57", 8080); // Intentar reconectar
                    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    this.writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println("/sendUser " + userName + " " + userType);
                    IniciarEscucha();
                    connected = false; // Conexión exitosa, salimos del bucle
                } catch (IOException | InterruptedException e) {
                    System.out.println("Intentando reconectar en 3 segundos...");
                }
            }
        }
        private void IniciarEscucha()
        {
            Thread listenThread = new Thread(() -> {
                Scanner in = new Scanner(reader);
                while (true) {
                    if (in.hasNextLine()) {
                        String message = in.nextLine();
                        System.out.println(message);
                        filter= searchBar.getText();
                        Platform.runLater(() -> {
                            System.out.println(message);
                            if(message.startsWith("Usuarios:")&&dm)
                            {
                                String [] aux= message.split(":");
                                if(aux.length>1 && (filter!="" && filter!=" ")){
                                    String[] usuarios= aux[1].split(", ");
                                    for (String part : usuarios) { // Recorrer cada elemento del mensajeff
                                        if (part.contains(filter)){
                                            if(!(userName+"-"+userType).equals(part) && !usuariosActivos.contains(part))
                                            {
                                                if (userType.equals("medico") && !part.contains("-aseo")){
                                                    usuariosActivos.add(part);
                                                    HBox elementBox = createDuplicateStructure(part);
                                                    chatPlace.getChildren().add(elementBox);
                                                } else if (!userType.equals("medico") && part.contains("medico")) {
                                                    usuariosActivos.add(part);
                                                    HBox elementBox = createDuplicateStructure(part);
                                                    chatPlace.getChildren().add(elementBox);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if(message.startsWith("Desconectado")){
                                System.out.println(message);
                                String userToDisconnect = message.split(": ")[1];
                                System.out.println(userToDisconnect);
                                Iterator<Node> iterator = chatPlace.getChildren().iterator();
                                while (iterator.hasNext()) {
                                    Node node = iterator.next();
                                    if (node instanceof HBox hbox) {
                                        for (Node childNode : hbox.getChildren()) {
                                            if (childNode instanceof VBox vbox) {
                                                for (Node vboxChild : vbox.getChildren()) {
                                                    if (vboxChild instanceof Label label && label.getText().equals(userToDisconnect)) {
                                                        System.out.println("Se encontró el texto en la etiqueta: " + label.getText());
                                                        usuariosActivos.remove(label.getText());
                                                        if(userToDisconnect.equals(talkingTo))
                                                        {
                                                            talkingTo="";
                                                        }
                                                        iterator.remove(); // Utiliza el iterador para eliminar el elemento de manera segura
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (message.startsWith("Historial entre")) {
                                String[] aux = message.split("\\*");
                                System.out.println(Arrays.toString(aux));
                                if(aux.length>3){
                                    String[] aux1 = aux[3].split(",-");
                                    if (aux[1].equals(talkingTo) || aux[2].equals(talkingTo)) {
                                        messagelog.getChildren().clear();
                                        for (String mlog:aux1)
                                        {
                                            processLine(messagelog, mlog);
                                            messagelog.getChildren().add(new Text("\n"));
                                        }
                                    }
                                }
                            }
                            if (message.startsWith("/activos:")&&dm){
                                chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                                usuariosActivos.clear();
                                String [] aux= message.split(":");
                                if(aux.length>1 && (filter!="" || filter!=" ")){
                                    String[] usuarios= aux[1].split(", ");
                                    for (String part : usuarios) { // Recorrer cada elemento del mensajeff
                                        if (part.contains(filter)){
                                            if(!(userName+"-"+userType).equals(part)  && !usuariosActivos.contains(part))
                                            {
                                                if (userType.equals("medico") && !part.contains("-aseo")){
                                                    usuariosActivos.add(part);
                                                    HBox elementBox = createDuplicateStructure(part);
                                                    chatPlace.getChildren().add(elementBox);
                                                } else if (!userType.equals("medico") && part.contains("medico")) {
                                                    usuariosActivos.add(part);
                                                    HBox elementBox = createDuplicateStructure(part);
                                                    chatPlace.getChildren().add(elementBox);
                                                }
                                            }
                                        }
                                    }
                                }
                                else if(aux.length>1 && (filter=="" || filter==" ")){
                                    String[] usuarios= aux[1].split(", ");
                                    for (String part : usuarios) { // Recorrer cada elemento del mensajeff
                                        if (part.contains(filter)){
                                            if(!(userName+"-"+userType).equals(part) && !usuariosActivos.contains(part))
                                            {
                                                if (userType.equals("medico") && !part.contains("-aseo")){
                                                    usuariosActivos.add(part);
                                                    HBox elementBox = createDuplicateStructure(part);
                                                    chatPlace.getChildren().add(elementBox);
                                                } else if (!userType.equals("medico") && part.contains("medico")) {
                                                    usuariosActivos.add(part);
                                                    HBox elementBox = createDuplicateStructure(part);
                                                    chatPlace.getChildren().add(elementBox);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else if (message.startsWith("Grupo:")){
                                //chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                                String [] aux= message.split(":");
                                String [] grupos = aux[1].split(" ");
                                if(userType.equals("medico")|| userType.equals("admin")) {
                                    for (String group : grupos)
                                    {
                                        if(!gruposActivos.contains(group)){
                                            gruposActivos.add(group);
                                            HBox grupo = createDuplicateStructure(group);
                                            chatPlace.getChildren().add(grupo);}
                                    }
                                }
                                else {
                                    if(aux.length>=3){
                                        String [] actvos = aux[2].split(" ");
                                        usuariosActivos.clear();
                                        usuariosActivos.addAll(Arrays.asList(actvos));
                                        usuariosActivos.add("todos");
                                        HBox answering = resGrupo();
                                        chatPlace.getChildren().add(answering);}
                                    else
                                    {
                                        usuariosActivos.clear();
                                        usuariosActivos.add("todos");
                                        HBox answering = resGrupo();
                                        chatPlace.getChildren().add(answering);
                                    }
                                    for (String group : grupos)
                                    {
                                        if(!gruposActivos.contains(group)){
                                            HBox grupo = createDuplicateStructure(group);
                                            chatPlace.getChildren().add(grupo);}
                                    }
                                }
                            }
                            else if (message.startsWith("EstadisticasUsers:")) {
                                String [] parts = message.split(":");
                                String [] aux =  parts[1].split(" ");
                                ComboBox userStats = (ComboBox) scene.lookup("#usuariosEstadisticas");
                                userStats.getItems().clear();
                                for(String user :aux) {
                                    userStats.getItems().add(user);
                                }
                            } else if (message.startsWith("/LostConnection")) {

                                // Agregar un mensaje de reconexión
                                Platform.runLater(() -> {
                                    // Imprimir mensaje en la interfaz gráfica o consola
                                    System.out.println("Conexión perdida. Intentando reconectar en 3 segundos...");
                                });
                                // Intentar reconexión
                                reconnect();

                            } else if (message.startsWith("ConteoMensajes:")) {
                                String [] parts = message.split(":");
                                TextFlow privadosC  = (TextFlow) scene.lookup("#Privados");
                                TextFlow grupalesC  = (TextFlow) scene.lookup("#Grupales");
                                privadosC.setTextAlignment(TextAlignment.CENTER);
                                Text privados =new Text(parts[1]);
                                privados.setFont(new Font("System Bold Italic", 16.0));
                                Text grupales = new Text(parts[2]);
                                grupales.setFont(new Font("System Bold Italic", 16.0));
                                privadosC.getChildren().add(privados);
                                grupalesC.getChildren().add(grupales);
                            }
                        });
                    }
                }
            });
            listenThread.start();
        }
        private void processLine(TextFlow textFlow, String text) {
            int startIndex = 0;
            int endIndex;
            while ((endIndex = findNextStyleMarker(text, startIndex)) != -1) {
                // Agregar texto normal antes de la parte formateada
                if (startIndex < endIndex) {
                    addTextWithStyle(textFlow, text.substring(startIndex, endIndex), null);
                }
                // Extraer el contenido formateado y los marcadores
                String formattedText = text.substring(endIndex + 2, findNextStyleMarker(text, endIndex + 2));
                String markers = text.substring(endIndex, endIndex + 2);
                // Aplicar los estilos según los marcadores
                applyStyles(textFlow, formattedText, markers);
                // Actualizar el índice de inicio para la próxima iteración
                startIndex = findNextStyleMarker(text, endIndex + 2) + 2;
            }
            // Agregar el texto restante después de la última parte formateada
            if (startIndex < text.length()) {
                addTextWithStyle(textFlow, text.substring(startIndex), null);
            }
        }
        private int findNextStyleMarker(String text, int startIndex) {
            int nextBoldIndex = text.indexOf("$$", startIndex);
            int nextItalicIndex = text.indexOf("%%", startIndex);
            int nextBoldItalicIndex = text.indexOf("&&", startIndex);
            // Devolver el índice del siguiente marcador, o -1 si no hay más marcadores
            return Stream.of(nextBoldIndex, nextItalicIndex, nextBoldItalicIndex)
                    .filter(index -> index != -1)
                    .min(Integer::compare)
                    .orElse(-1);
        }
        private void applyStyles(TextFlow textFlow, String text, String markers) {
            if (markers.contains("$$")) {
                addTextWithStyle(textFlow, text, "bold");
            } else if (markers.contains("%%")) {
                addTextWithStyle(textFlow, text, "italic");
            } else if (markers.contains("&&")) {
                addTextWithStyle(textFlow, text, "bold-italic");
            }// Puedes agregar más estilos según tus necesidades
        }
        private void addTextWithStyle(TextFlow textFlow, String text, String style) {
            Text styledText = new Text(text);
            if (style != null) {
                switch (style) {
                    case "bold":
                        styledText.setStyle("-fx-font-weight: bold;");
                        break;
                    case "italic":
                        styledText.setStyle("-fx-font-style: italic;");
                        break;
                    case "bold-italic":
                        styledText.setStyle("-fx-font-weight: bold; -fx-font-style: italic;");
                        break;
                    // Puedes agregar más casos según tus necesidades
                }
            }
            textFlow.getChildren().add(styledText);
        }
        public HBox createDuplicateStructure(String user) {
            HBox hBox = new HBox();
            ImageView imageView;
            imageView = new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/64/64572.png"));
            imageView.setFitHeight(60);
            imageView.setFitWidth(69);
            imageView.setPreserveRatio(true);
            VBox vBox = new VBox();
            vBox.setPrefHeight(81);
            vBox.setPrefWidth(185);
            vBox.setAlignment(javafx.geometry.Pos.CENTER);
            Label label = new Label(user);
            label.setPrefHeight(18);
            label.setPrefWidth(161);
            label.setTextFill(Color.WHITE);
            label.setFont(Font.font("System", 14));
            // To make the text bold, you set the FontWeight of the font
            label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, label.getFont().getSize()));
            vBox.getChildren().add(label);
            hBox.getChildren().addAll(imageView, vBox);
            hBox.setPrefHeight(81);
            hBox.setPrefWidth(257);
            hBox.setAlignment(javafx.geometry.Pos.CENTER);
            // Adding a 10-pixel top margin to the HBox
            HBox.setMargin(hBox, new Insets(10, 0, 0, 0));
            // Agregar un controlador de eventos al HBox para cambiar el fondo al hacer clic
            hBox.setOnMouseClicked(event -> {
                // Recorrer todos los hijos del chatPlace para quitar el fondo coloreado
                messagelog.getChildren().clear();
                for (Node node : chatPlace.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                hBox.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                String username = label.getText();
                // Imprimir en la consola el mensaje "Hablando con [nombre del usuario]"
                this.talkingTo = username.split("-")[0];
                messagelog.getChildren().clear();
                if (dm){
                    System.out.println("Hablando con " + talkingTo);
                solicitarHistorial(this.userName, talkingTo);
            }
                else {
                    if(!userType.equals("admin") && !userType.equals("medico")){
                        HBox aux = (HBox) chatPlace.getChildren().get(2);
                        ComboBox aux1 = (ComboBox) aux.getChildren().get(1);
                        aux1.setVisible(true);
                        if(!userType.equals("aseo") && username.equals("aseo"))
                        {
                            aux1.setVisible(false);
                        } else if (!userType.equals("aseo") && !username.equals("aseo")) {
                            aux1.setVisible(true);
                        } else if (userType.equals("aseo") &&  username.equals("aseo")) {
                            aux1.setVisible(true);
                        }
                    }
                    talkingTo=username;
                    solicitarHistorialGrupo(username);
                }
            });
            return hBox;
        }
        private void solicitarHistorialGrupo(String grupo) {
            // Enviar solicitud al servidor para obtener el historial entre los usuarios
            writer.println("/getHistorialGrupal" +  " " + grupo+ " " +userName);
        }
        public void chatGrupo(String grupo,String mensaje) {
            writer.println("/getHistorialGrupal" +  " " + grupo+ " " +userName);// Enviar solicitud al servidor para obtener el historial entre los usuarios
            writer.println("/updateHistorialGrupal " + grupo +" "+mensaje);
        }
        public void solicitarHistorial(String usuarioSolicitante, String usuarioBuscado) {
            // Enviar solicitud al servidor para obtener el historial entre los usuarios
            writer.println("/getHistorial " + usuarioSolicitante + " " + usuarioBuscado);
        }
        public void chat(String usuarioSolicitante, String usuarioBuscado,String mensaje) {
            // Enviar solicitud al servidor para obtener el historial entre los usuarios
            writer.println("/updateHistorial " + usuarioSolicitante + " " + usuarioBuscado+" "+mensaje);
        }
        public HBox resGrupo(){
            HBox hbox = new HBox();
            hbox.setId("answeringto");
            hbox.setAlignment(javafx.geometry.Pos.CENTER);
            hbox.setPrefHeight(100.0);
            hbox.setPrefWidth(200.0);
            Label label = new Label("Respondiendo");
            label.setPrefHeight(50.0);
            label.setPrefWidth(104.0);
            label.setTextFill(javafx.scene.paint.Color.WHITE);
            HBox.setMargin(label, new Insets(0, 0, 0, 30.0));
            Font font = Font.font("System Bold Italic", 14.0);
            label.setFont(font);
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setPrefHeight(26.0);
            comboBox.setPrefWidth(131.0);
            for (String usuario : usuariosActivos) {
                comboBox.getItems().add(usuario);
            }
            hbox.getChildren().addAll(label, comboBox);
            return hbox;
        }
        public AnchorPane estadisticasPanel() {
                AnchorPane anchorPane = new AnchorPane();
                anchorPane.setId("estadisticas");
                anchorPane.setMaxHeight(Double.NEGATIVE_INFINITY);
                anchorPane.setMaxWidth(Double.NEGATIVE_INFINITY);
                anchorPane.setMinHeight(Double.NEGATIVE_INFINITY);
                anchorPane.setMinWidth(Double.NEGATIVE_INFINITY);
                anchorPane.setPrefHeight(641.0);
                anchorPane.setPrefWidth(720.0);
                anchorPane.getStyleClass().add("dark-gray-background");
                anchorPane.getStylesheets().add("@values/styles.css");
                // Text elements
                Text usuarioText = new Text("Usuario:");
                usuarioText.setFill(javafx.scene.paint.Color.WHITE);
                usuarioText.setLayoutX(239.0);
                usuarioText.setLayoutY(117.0);
                Text fechaInicioText = new Text("Fecha Inicio");
                fechaInicioText.setFill(javafx.scene.paint.Color.WHITE);
                fechaInicioText.setLayoutX(228.0);
                fechaInicioText.setLayoutY(173.0);
                fechaInicioText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
                fechaInicioText.setStrokeWidth(0.0);
                fechaInicioText.setFont(new Font("System Bold Italic", 13.0));
                Text fechaFinText = new Text("Fecha Fin");
                fechaFinText.setFill(javafx.scene.paint.Color.WHITE);
                fechaFinText.setLayoutX(228.0);
                fechaFinText.setLayoutY(236.0);
                fechaFinText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
                fechaFinText.setStrokeWidth(0.0);
                fechaFinText.setFont(new Font("System Bold Italic", 13.0));
                // TextFields
                TextField textField1 = new TextField();
                textField1.setId("Fecha_inicio");
                textField1.setLayoutX(309.0);
                textField1.setLayoutY(155.0);
                textField1.getStyleClass().add("radius");
                TextField textField2 = new TextField();
                textField2.setId("Fecha_fin");
                textField2.setLayoutX(309.0);
                textField2.setLayoutY(218.0);
                textField2.getStyleClass().add("radius");
                // ComboBox
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.setLayoutX(315.0);
                comboBox.setLayoutY(99.0);
                comboBox.setPrefWidth(150.0);
                comboBox.setId("usuariosEstadisticas");
                // HBox
                HBox hBox = new HBox();
                hBox.setAlignment(javafx.geometry.Pos.CENTER);
                hBox.setLayoutX(315.0);
                hBox.setLayoutY(274.0);
                hBox.setPrefHeight(56.0);
                hBox.setPrefWidth(150.0);
                hBox.setStyle("-fx-background-color: purple;");
                hBox.setId("ButtonEstadisticas");
                hBox.getStyleClass().add("radius");
                // Text inside HBox
                Text buscarText = new Text("Buscar");
                buscarText.setFill(javafx.scene.paint.Color.WHITE);
                buscarText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
                buscarText.setStrokeWidth(0.0);
                buscarText.setFont(new Font("System Bold Italic", 14.0));
                hBox.getChildren().add(buscarText);
                // TextFlow
                TextFlow textFlow1 = new TextFlow();
                textFlow1.setVisible(false);
                textFlow1.setId("Privados");
                textFlow1.setLayoutX(303.0);
                textFlow1.setLayoutY(394.0);
                textFlow1.setPrefHeight(38.0);
                textFlow1.setPrefWidth(175.0);
                textFlow1.setStyle("-fx-background-color: white;");
                textFlow1.getStyleClass().add("radius");
                textFlow1.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                TextFlow textFlow2 = new TextFlow();
                textFlow2.setVisible(false);
                textFlow2.setId("Grupales");
                textFlow2.setLayoutX(303.0);
                textFlow2.setLayoutY(503.0);
                textFlow2.setPrefHeight(38.0);
                textFlow2.setPrefWidth(175.0);
                textFlow2.setStyle("-fx-background-color: white;");
                textFlow2.getStyleClass().add("radius");
                textFlow2.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                // Text elements for descriptions
                Text mensajesPrivadosText = new Text("Mensajes Privados entre las fechas por el usuario");
                mensajesPrivadosText.setFill(javafx.scene.paint.Color.WHITE);
                mensajesPrivadosText.setLayoutX(133.0);
                mensajesPrivadosText.setLayoutY(378.0);
                mensajesPrivadosText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
                mensajesPrivadosText.setStrokeWidth(0.0);
                mensajesPrivadosText.setVisible(false);
                mensajesPrivadosText.setFont(new Font("System Bold Italic", 22.0));
                Text mensajesGrupalesText = new Text("Mensajes Grupales entre las fechas por el usuario");
                mensajesGrupalesText.setFill(javafx.scene.paint.Color.WHITE);
                mensajesGrupalesText.setLayoutX(133.0);
                mensajesGrupalesText.setLayoutY(487.0);
                mensajesGrupalesText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
                mensajesGrupalesText.setStrokeWidth(0.0);
                mensajesGrupalesText.setVisible(false);
                mensajesGrupalesText.setFont(new Font("System Bold Italic", 22.0));
                // Adding elements to the AnchorPane
                anchorPane.getChildren().addAll(
                        usuarioText, fechaInicioText, fechaFinText,
                        textField1, textField2,
                        comboBox, hBox,
                        textFlow1, mensajesPrivadosText,
                        textFlow2, mensajesGrupalesText
                );
            hBox.setOnMouseClicked(event -> {
                textFlow1.setVisible(true);
                textFlow1.getChildren().clear();
                textFlow2.setVisible(true);
                textFlow2.getChildren().clear();
                mensajesGrupalesText.setVisible(true);
                mensajesPrivadosText.setVisible(true);
                obtenerEstadisticas(comboBox.getSelectionModel().getSelectedItem(),textField1.getText(),textField2.getText());
            });
                return anchorPane;}
        private void obtenerEstadisticas(String usuario, String date, String date1) {
            // Verificar si las fechas tienen el formato "yyyy-mm-dd" y no están en blanco
            if (isValidDateFormat(date) && isValidDateFormat(date1) && !date.isEmpty() && !date1.isEmpty()) {
                this.writer.println("/getEstadisticasU" + " " + usuario + " " + date+ " " + date1+" "+userName);
            } else {
                // Manejar el caso en el que las fechas no tengan el formato adecuado o estén en blanco
                System.out.println("Error: Las fechas deben tener el formato 'yyyy-mm-dd' y no deben estar en blanco.");
            }
        }
        private boolean isValidDateFormat(String date) {
            String regex = "\\d{4}-\\d{2}-\\d{2}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(date);
            return matcher.matches();
        }
        public AnchorPane createCustomAnchorPane() {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setPrefSize(649.0, 491.0);
            anchorPane.setStyle("-fx-background-color: #64197d;");
            ImageView imageView = new ImageView(new Image("https://github.com/BerserkerD81/hospital/blob/662f1359f8cf104abaed97b9f01b950e875dcd14/src/main/resources/com/hospital/hospital/images/usuario.png"));
            imageView.setFitWidth(241.0);
            imageView.setFitHeight(225.0);
            imageView.setLayoutX(106.0);
            imageView.setLayoutY(73.0);
            imageView.setOpacity(0.13);
            imageView.setPreserveRatio(true);
            Bloom bloom = new Bloom();
            bloom.setThreshold(0.08);
            imageView.setEffect(bloom);
            Text text1 = new Text("Nombre de usuario:");
            text1.setFill(javafx.scene.paint.Color.WHITE);
            text1.setLayoutX(96.0);
            text1.setLayoutY(90.0);
            Text text2 = new Text("Contraseña:");
            text2.setFill(javafx.scene.paint.Color.WHITE);
            text2.setLayoutX(117.0);
            text2.setLayoutY(263.0);
            Text text3 = new Text("Confirmar contraseña:");
            text3.setFill(javafx.scene.paint.Color.WHITE);
            text3.setLayoutX(90.0);
            text3.setLayoutY(320.0);
            TextField textField1 = new TextField();
            textField1.setLayoutX(255.0);
            textField1.setLayoutY(73.0);
            textField1.setId("registro_user");
            TextField textField2 = new TextField();
            textField2.setLayoutX(255.0);
            textField2.setLayoutY(246.0);
            textField2.setId("registro_pass");
            TextField textField3 = new TextField();
            textField3.setLayoutX(255.0);
            textField3.setLayoutY(303.0);
            textField3.setId("registro_check");
            Text text4 = new Text("Registrar usuario");
            text4.setFill(javafx.scene.paint.Color.WHITE);
            text4.setLayoutX(41.0);
            text4.setLayoutY(45.0);
            text4.setFont(new Font(29.0));
            Button button = new Button("Hecho");
            button.setLayoutX(285.0);
            button.setLayoutY(433.0);
            button.setId("botonRegistro");
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setLayoutX(255.0);
            comboBox.setLayoutY(370.0);
            comboBox.setPrefWidth(150.0);
            comboBox.getItems().addAll("admision", "examenes", "medico","pabellon","aseo");
            comboBox.setPromptText("tipo");
            comboBox.setId("registro_tipo");
            Text text5 = new Text("Tipo de Usuario:");
            text5.setFill(javafx.scene.paint.Color.WHITE);
            text5.setLayoutX(120.0);
            text5.setLayoutY(387.0);
            Text text6 = new Text("Rut");
            text6.setFill(javafx.scene.paint.Color.WHITE);
            text6.setLayoutX(139.0);
            text6.setLayoutY(144.0);
            Text text7 = new Text("Correo");
            text7.setFill(javafx.scene.paint.Color.WHITE);
            text7.setLayoutX(130.0);
            text7.setLayoutY(203.0);
            TextField textField4 = new TextField();
            textField4.setLayoutX(255.0);
            textField4.setLayoutY(127.0);
            textField4.setId("registro_rut");
            TextField textField5 = new TextField();
            textField5.setLayoutX(255.0);
            textField5.setLayoutY(186.0);
            textField5.setId("registro_correo");
            anchorPane.getChildren().addAll(
                    imageView, text1, text2, text3, textField1, textField2, textField3,
                    text4, button, comboBox, text5, text6, text7, textField4, textField5
            );
            anchorPane.setId("panelRegistro");
            return anchorPane;
        }
        @Override
        public void stop() throws Exception {
            // Cerrar flujos y socket al detener la aplicación
            writer.close();
            reader.close();
            socket.close();
            super.stop();
        }
        public static void main(String[] args) {
            //conecta con la base de datos
            String url = "jdbc:mariadb://35.226.170.116:3306/hospital";
            Connection connect;
            ResultSet result = null;
            try {
                connect = DriverManager.getConnection(url);
                if (connect != null) {
                    DatabaseMetaData meta = connect.getMetaData();
                    System.out.println("El driver es " + meta.getDriverName());
                    System.out.println("Se ha establecido una conexión con la base de datos");
                    //prueba consultas
                    PreparedStatement st = connect.prepareStatement("SELECT * FROM usuarios");
                    result = st.executeQuery();
                    //imprimir resultados
                    while (result.next()) {
                        System.out.println(result.getString("ID") + " " + result.getString("name"));
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            launch();
        }
    }