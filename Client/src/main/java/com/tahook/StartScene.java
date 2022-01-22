package com.tahook;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class StartScene {
    private Client client;
    private Stage stage;
    private Scene scene;
    private Parent root;

    public StartScene() {
        client = Client.getInstance();
    }

    public void createNewGame(MouseEvent event) throws IOException {

        client.joinServer(1111);
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("host/createGameScene.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void joinGame(MouseEvent event) throws IOException {
        client.joinServer(2222);
        // client.read();
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("player/nickScene.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
