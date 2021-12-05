package com.tahook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("startScene.fxml")));
            Scene scene = new Scene(root, 800, 600);

//            Image icon = new Image("icon.png");
//            stage.getIcons().add(icon);
            stage.setTitle("Kahoot");
            stage.setResizable(false);

            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}