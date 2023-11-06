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
    import javafx.scene.text.Font;
    import javafx.scene.text.FontPosture;
    import javafx.scene.text.FontWeight;
    import javafx.scene.text.Text;
    import javafx.stage.Stage;

    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import java.net.Socket;
    import java.util.*;

    public class HelloApplication extends Application {
        private boolean dm=false;
        private VBox chatPlace;

        public ImageView buttonsend;
        public String userName;

        public TextField mesaggeinput;

        public TextArea messagelog;
        private Socket socket;
        private PrintWriter out;
        public String talkingTo="*";

        private  ArrayList<String> usuariosActivos = new ArrayList<>();

        public PrintWriter writer;
        public BufferedReader reader;

        //Botones pal login
        private Button botonLogin;
        private TextField login_user;
        private TextField login_password;
        private Hyperlink login_register;
        //Variable chafa, esto es por mientras
        private boolean inicio = false;
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
            Scene scene = new Scene(fxmlLoader.load(), 1080, 720);

            botonLogin.setOnMouseClicked(e -> {
                String user = login_user.getText();
                this.userName = user;
                String pass = login_password.getText();
                //Aqui hacer verificaciones
                inicio = true;
                //consultar base de datos (por ahora sin el server)
                String url = "jdbc:sqlite:src/main/resources/db/login.db";
                Connection connect;
                ResultSet result = null;
                //si el usuario y contraseña son correctos se abre la ventana de chat
                try {
                    connect = DriverManager.getConnection(url);
                    if (connect != null) {
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
                            }

                        } else {
                            System.out.println("Usuario o contraseña incorrectos");
                            System.out.println("Usuario: " + user + " Contraseña: " + pass);
                        }


                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            });


            if(inicio){ //Inicio chat cuando se inicia sesión

            }

            chatPlace = (VBox) scene.lookup("#chat_place");
            buttonsend = (ImageView) scene.lookup("#send_button");
            mesaggeinput = (TextField) scene.lookup("#message_bar");
            messagelog =(TextArea) scene.lookup("#message_area");
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
            AnchorPane auxn = createCustomAnchorPane();
            paneBox.getChildren().add(auxn);
            panelRegistro =(AnchorPane) scene.lookup("#panelRegistro");
            panelRegistro.setVisible(false);



            buttonsend.setOnMouseClicked(e -> {
                String message = mesaggeinput.getText(); // Get the text from the input field
                mesaggeinput.setText("");
                System.out.println("talking to: "+talkingTo);
                if(dm){
                chat(userName,talkingTo,userName+":"+message);
                solicitarHistorial(this.userName,talkingTo);
            }
                else {
                    chatGrupo(talkingTo,userName+":"+message);
                    solicitarHistorialGrupo(talkingTo);

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
            });
            groupButton.setOnMouseClicked(e -> {
                for (Node node : menuBar.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                groupButton.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                searchBox.setVisible(false);
                chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                usuariosActivos.clear();
                dm=false;

                writer.println("/getGroup "+userName);
                System.out.println("Listar Grupos");
                messagelog.setVisible(true);
                chatBar.setVisible(true);
                panelRegistro.setVisible(false);
                System.out.println(panelRegistro);





            });
            accountButton.setOnMouseClicked(e -> {
                for (Node node : menuBar.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                accountButton.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                searchBox.setVisible(false);
                chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                usuariosActivos.clear();
                messagelog.setVisible(false);
                chatBar.setVisible(false);
                panelRegistro.setVisible(false);

            });
            adminButton.setOnMouseClicked(e -> {
                for (Node node : menuBar.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                adminButton.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox
                searchBox.setVisible(false);
                chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                usuariosActivos.clear();
                messagelog.setVisible(false);
                chatBar.setVisible(false);
                panelRegistro.setVisible(true);



            });


            // Conectarse al servidor
            socket = new Socket("localhost", 9090); // Reemplazar "localhost" con la IP real si es necesario




            // Enviar el nombre de usuario al servidor


            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);


            // Setting up a thread to continuously listen for server messages
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

                                  if(userType.equals("admin") && !usuariosActivos.contains("TODOS"))
                                  {
                                      HBox element = createDuplicateStructure("TODOS");
                                      chatPlace.getChildren().add(element);
                                      usuariosActivos.add("TODOS");
                                  }
                                for (String part : usuarios) { // Recorrer cada elemento del mensajeff

                                    if (part.contains(filter)){


                                    if(!(userName+"-"+userType).equals(part) && !usuariosActivos.contains(part))
                                    {

                                        usuariosActivos.add(part);
                                        HBox elementBox = createDuplicateStructure(part);
                                        chatPlace.getChildren().add(elementBox);
                                    }}

                                }}
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
                                    StringBuilder historial = new StringBuilder();
                                    for (String mlog:aux1)
                                    {
                                        historial.append(mlog).append("\n");
                                    }

                                    if (aux[1].equals(talkingTo) || aux[2].equals(talkingTo)) {
                                        messagelog.setText("");
                                        System.out.println(historial);
                                        messagelog.setText(historial.toString());
                                    }
                                }}
                            if (message.startsWith("/activos:")&&dm){

                            chatPlace.getChildren().remove(2,chatPlace.getChildren().size());
                                usuariosActivos.clear();

                                if (userType.equals("admin")) {
                                    HBox element = createDuplicateStructure("TODOS");
                                    chatPlace.getChildren().add(element);
                                }
                                String [] aux= message.split(":");
                                if(aux.length>1 && (filter!="" || filter!=" ")){
                                    String[] usuarios= aux[1].split(", ");

                                    for (String part : usuarios) { // Recorrer cada elemento del mensajeff

                                        if (part.contains(filter)){


                                            if(!(userName+"-"+userType).equals(part)  && !usuariosActivos.contains(part))
                                            {

                                                usuariosActivos.add(part);
                                                HBox elementBox = createDuplicateStructure(part);
                                                chatPlace.getChildren().add(elementBox);
                                            }}

                                    }}
                                else if(aux.length>1 && (filter=="" || filter==" ")){
                                    String[] usuarios= aux[1].split(", ");


                                    for (String part : usuarios) { // Recorrer cada elemento del mensajeff

                                        if (part.contains(filter)){


                                            if(!(userName+"-"+userType).equals(part) && !usuariosActivos.contains(part))
                                            {

                                                usuariosActivos.add(part);
                                                HBox elementBox = createDuplicateStructure(part);
                                                chatPlace.getChildren().add(elementBox);
                                            }
                                        }
                                    }
                                }
                        }
                        else if (message.startsWith("Grupo:")){
                                String [] aux= message.split(":");
                                HBox grupo = createDuplicateStructure(aux[1]);
                                chatPlace.getChildren().add(grupo);

                            }


                        });



                    }
                }

            });
            listenThread.start();
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
                messagelog.setText("");
                for (Node node : chatPlace.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                hBox.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox


                String username = label.getText();
                // Imprimir en la consola el mensaje "Hablando con [nombre del usuario]"
                this.talkingTo = username.split("-")[0];
                messagelog.setText("");
                if (dm){

                    System.out.println("Hablando con " + talkingTo);
                solicitarHistorial(this.userName, talkingTo);
            }
                else {
                    talkingTo=username;
                    solicitarHistorialGrupo(username);
                }


            });

            return hBox;
        }

        private void solicitarHistorialGrupo(String grupo) {
            // Enviar solicitud al servidor para obtener el historial entre los usuarios
            writer.println("/getHistorialGrupal" +  " " + grupo);
        }
        public void chatGrupo(String grupo,String mensaje) {
            // Enviar solicitud al servidor para obtener el historial entre los usuarios
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


        public AnchorPane createCustomAnchorPane() {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setPrefWidth(433.0 * 1.5);
            anchorPane.setPrefHeight(332.0 * 1.5);
            anchorPane.setStyle("-fx-background-color: #64197d;");

            ImageView imageView = new ImageView(new Image("https://github.com/BerserkerD81/hospital/blob/662f1359f8cf104abaed97b9f01b950e875dcd14/src/main/resources/com/hospital/hospital/images/usuario.png"));
            imageView.setFitWidth(241.0 * 1.5);
            imageView.setFitHeight(225.0 * 1.5);
            imageView.setLayoutX(106.0 * 1.5);
            imageView.setLayoutY(73.0 * 1.5);
            imageView.setOpacity(0.13);
            imageView.setPreserveRatio(true);

            Bloom bloom = new Bloom();
            bloom.setThreshold(0.08);
            imageView.setEffect(bloom);

            Text text1 = new Text("Nombre de usuario:");
            text1.setFill(javafx.scene.paint.Color.WHITE);
            text1.setLayoutX(64.0 * 1.5);
            text1.setLayoutY(114.0 * 1.5);

            Text text2 = new Text("Contraseña:");
            text2.setFill(javafx.scene.paint.Color.WHITE);
            text2.setLayoutX(64.0 * 1.5);
            text2.setLayoutY(157.0 * 1.5);

            Text text3 = new Text("Confirmar contraseña:");
            text3.setFill(javafx.scene.paint.Color.WHITE);
            text3.setLayoutX(64.0 * 1.5);
            text3.setLayoutY(202.0 * 1.5);

            TextField textField1 = new TextField();
            textField1.setLayoutX(205.0 * 1.5);
            textField1.setLayoutY(97.0 * 1.5);
            textField1.setId("userRegistro");

            TextField textField2 = new TextField();
            textField2.setLayoutX(205.0 * 1.5);
            textField2.setLayoutY(140.0 * 1.5);
            textField2.setId("passRegistro");

            TextField textField3 = new TextField();
            textField3.setLayoutX(205.0 * 1.5);
            textField3.setLayoutY(185.0 * 1.5);
            textField3.setId("confirmarPass");

            Text text4 = new Text("Registrar usuario");
            text4.setFill(javafx.scene.paint.Color.WHITE);
            text4.setLayoutX(41.0 * 1.5);
            text4.setLayoutY(64.0 * 1.5);
            text4.setFont(new Font(29.0 * 1.5));

            Button button = new Button("Hecho");
            button.setLayoutX(330.0 * 1.5);
            button.setLayoutY(295.0 * 1.5);
            button.setId("RegistroButton");

            ComboBox comboBox = new ComboBox();
            comboBox.setLayoutX(205.0 * 1.5);
            comboBox.setLayoutY(227.0 * 1.5);
            comboBox.setPrefWidth(150.0 * 1.5);
            comboBox.setPromptText("tipo");
            comboBox.getItems().addAll("medico","auxiliar","admision","examenes");
            comboBox.setId("comboRegistro");

            Text text5 = new Text("Tipo de Usuario:");
            text5.setFill(javafx.scene.paint.Color.WHITE);
            text5.setLayoutX(63.0 * 1.5);
            text5.setLayoutY(244.0 * 1.5);

            anchorPane.getChildren().addAll(
                    imageView, text1, text2, text3, textField1, textField2, textField3,
                    text4, button, comboBox, text5
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
            String url = "jdbc:sqlite:src/main/resources/db/login.db";
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
