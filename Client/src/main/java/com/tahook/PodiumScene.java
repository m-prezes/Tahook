package com.tahook;

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

import org.json.simple.JSONObject;

public class PodiumScene {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Client client;

    @FXML
    private Label firstPlaceLabel;
    @FXML
    private Label secondPlaceLabel;
    @FXML
    private Label thirdPlaceLabel;

    public PodiumScene() {
        client = Client.getInstance();
    }

    @FXML
    public void initialize() {
        firstPlaceLabel.setText(((JSONObject) client.sortedRanking.get(0)).get("userName").toString());
        secondPlaceLabel.setText(((JSONObject) client.sortedRanking.get(1)).get("userName").toString());
        if (client.sortedRanking.size() > 2) {
            thirdPlaceLabel.setText(((JSONObject) client.sortedRanking.get(2)).get("userName").toString());
        }
    }

    public void switchToStartScene(ActionEvent event) throws IOException {
        client.reset();
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("startScene.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
