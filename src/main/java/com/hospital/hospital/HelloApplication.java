package com.hospital.hospital;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.io.IOException;

public class HelloApplication extends Application {
    private VBox chatPlace;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        stage.setTitle("Chat Hospital");
        stage.setScene(scene);
        stage.show();

        // Obtener el VBox chat_place desde el FXML
        chatPlace = (VBox) scene.lookup("#chat_place");

        // Manejar el evento de teclado para la tecla "Esc"
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (chatPlace != null) {
                    // Crear un nuevo chat_element similar al existente

                    HBox DUPLICADO =createDuplicateStructure();
                    chatPlace.getChildren().add(DUPLICADO);
                }
            }
        });
    }





    public HBox createDuplicateStructure() {
        HBox hBox = new HBox();

        ImageView imageView = new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/64/64572.png"));
        imageView.setFitHeight(60);
        imageView.setFitWidth(69);
        imageView.setPreserveRatio(true);

        VBox vBox = new VBox();
        vBox.setPrefHeight(81);
        vBox.setPrefWidth(185);
        vBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label label = new Label("Name_placeholder");
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

        return hBox;
    }


    public static void main(String[] args) {
        launch();
    }
}
