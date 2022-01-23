package com.tahook.host;

import com.tahook.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WaitingRoom {
    @FXML
    private Label pinLabel;

    private Stage stage;
    private Scene scene;
    private Parent root;
    private Client client;

    @FXML
    public void initialize() {
        client = Client.getInstance();
        pinLabel.setText(client.getData());
    }

    public void switchToQuestionScene(ActionEvent event) throws IOException {
        client.write("start");
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("questionScene.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
