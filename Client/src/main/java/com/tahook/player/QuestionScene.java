package com.tahook.player;

import com.tahook.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class QuestionScene {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private Client client;

    public QuestionScene() {client = Client.getInstance();}

    public void handleAnswer(MouseEvent event) throws IOException {
        switchToWaitingScene(event);
    }

    public void switchToWaitingScene(MouseEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("waitingScene.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }




}
