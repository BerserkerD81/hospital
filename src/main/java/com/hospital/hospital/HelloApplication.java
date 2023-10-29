    package com.hospital.hospital;

    import javafx.application.Application;
    import javafx.application.Platform;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXMLLoader;
    import javafx.geometry.Insets;
    import javafx.scene.Node;
    import javafx.scene.Scene;
    import javafx.scene.control.Button;
    import javafx.scene.control.Label;
    import javafx.scene.control.TextArea;
    import javafx.scene.control.TextField;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.VBox;
    import javafx.scene.paint.Color;
    import javafx.scene.text.Font;
    import javafx.scene.text.FontPosture;
    import javafx.scene.text.FontWeight;
    import javafx.stage.Stage;

    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import java.net.Socket;
    import java.util.Arrays;
    import java.util.Iterator;
    import java.util.Objects;
    import java.util.Scanner;

    public class HelloApplication extends Application {
        private VBox chatPlace;

        public ImageView buttonsend;
        public String userName;

        public TextField mesaggeinput;

        public TextArea messagelog;
        private Socket socket;
        private PrintWriter out;
        public String talkingTo="*";

        public String UsuariosActivos ="";
        public PrintWriter writer;
        public BufferedReader reader;

        @Override
        public void start(Stage stage) throws IOException {
            // Pedir al usuario que ingrese su nombre por consola
            Scanner scanner = new Scanner(System.in);
            System.out.print("Por favor, ingrese su nombre de usuario: ");
            this.userName = scanner.nextLine();

            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
            stage.setTitle("Chat Hospital");
            stage.setScene(scene);
            stage.show();

            chatPlace = (VBox) scene.lookup("#chat_place");
            buttonsend = (ImageView) scene.lookup("#send_button");
            mesaggeinput = (TextField) scene.lookup("#message_bar");
            messagelog =(TextArea) scene.lookup("#message_area");

            buttonsend.setOnMouseClicked(e -> {
                String message = mesaggeinput.getText(); // Get the text from the input field
                mesaggeinput.setText("");
                chat(userName,talkingTo,userName+":"+message);
                solicitarHistorial(this.userName,talkingTo);
            });


            // Conectarse al servidor
            socket = new Socket("localhost", 9090); // Reemplazar "localhost" con la IP real si es necesario

            // Enviar el nombre de usuario al servidor
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(userName);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);

            // Setting up a thread to continuously listen for server messages
            Thread listenThread = new Thread(() -> {
                Scanner in = new Scanner(reader);
                while (true) {
                    if (in.hasNextLine()) {
                        String message = in.nextLine();
                        System.out.println(message);

                            Platform.runLater(() -> {
                                if(!(message.split(":")[0].contains("Desconectado")) && !message.contains("Historial entre"))
                                {
                                    String[] usuarios= message.split(", ");

                                    for (String part : usuarios) { // Recorrer cada elemento del mensajeff


                                        if(!userName.equals(part) && !UsuariosActivos.contains(part))
                                        {

                                            UsuariosActivos=UsuariosActivos+part+",";
                                            HBox elementBox = createDuplicateStructure(part);
                                            chatPlace.getChildren().add(elementBox);
                                        }

                                    }
                                }

                                if(message.split(":")[0].contains("Desconectado")){
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
                                                            iterator.remove(); // Utiliza el iterador para eliminar el elemento de manera segura
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                                if (message.contains("Historial entre")) {
                                    String[] aux = message.split("\\*");
                                    System.out.println(Arrays.toString(aux));
                                    String[] aux1 = aux[3].split(",-");
                                    StringBuilder historial = new StringBuilder();
                                    for (String mlog:aux1)
                                    {
                                        historial.append(mlog).append("\n");
                                    }

                                    if (aux[1].contains(talkingTo) || aux[2].contains(talkingTo)) {
                                        messagelog.setText("");
                                        System.out.println(historial);
                                        messagelog.setText(historial.toString());
                                    }
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
                for (Node node : chatPlace.getChildren()) {
                    if (node instanceof HBox) {
                        node.setStyle(""); // Establecer el estilo vacío para eliminar el fondo coloreado
                    }
                }
                hBox.setStyle("-fx-background-color: purple;"); // Cambiar el fondo a morado al hacer clic en este HBox



                String username = label.getText();
                // Imprimir en la consola el mensaje "Hablando con [nombre del usuario]"
                this.talkingTo=username;
                System.out.println("Hablando con " + username);
                solicitarHistorial(this.userName,username);



            });

            return hBox;
        }

        public void solicitarHistorial(String usuarioSolicitante, String usuarioBuscado) {
            // Enviar solicitud al servidor para obtener el historial entre los usuarios
            writer.println("/getHistorial " + usuarioSolicitante + " " + usuarioBuscado);
        }
        public void chat(String usuarioSolicitante, String usuarioBuscado,String mensaje) {
            // Enviar solicitud al servidor para obtener el historial entre los usuarios
            writer.println("/updateHistorial " + usuarioSolicitante + " " + usuarioBuscado+" "+mensaje);
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
            launch();
        }
    }
