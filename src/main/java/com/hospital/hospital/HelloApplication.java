package com.hospital.hospital;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;

public class HelloApplication extends Application {
    private VBox chatPlace;
    private Socket socket;
    private PrintWriter out;

    public String UsuariosActivos ="";

    @Override
    public void start(Stage stage) throws IOException {
        // Pedir al usuario que ingrese su nombre por consola
        Scanner scanner = new Scanner(System.in);
        System.out.print("Por favor, ingrese su nombre de usuario: ");
        String userName = scanner.nextLine();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        stage.setTitle("Chat Hospital");
        stage.setScene(scene);
        stage.show();

        chatPlace = (VBox) scene.lookup("#chat_place");

        // Conectarse al servidor
        socket = new Socket("localhost", 9090); // Reemplazar "localhost" con la IP real si es necesario

        // Enviar el nombre de usuario al servidor
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(userName);

        // Setting up a thread to continuously listen for server messages
        Thread listenThread = new Thread(() -> {
            try {
                Scanner in = new Scanner(socket.getInputStream());
                while (true) {
                    if (in.hasNextLine()) {
                        String message = in.nextLine();

                            Platform.runLater(() -> {
                                if(!(message.split(":")[0].contains("Desconectados")))
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

                                if(message.split(":")[0].contains("Desconectados")){
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



                            });



                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
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
            System.out.println("Hablando con " + username);

        });

        return hBox;
    }


    public static void main(String[] args) {
        launch();
    }
}
