package com.tahook;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ErrorScene {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Client client;
    String errMess;
    

    @FXML
    private Label error_mess;
    @FXML
    private Button but_ans_a;

    @FXML
    public void initialize() {
        error_mess.setText(errMess);
    }

    public ErrorScene() {
        client = Client.getInstance();
        errMess = client.getErrorMessage();
    }

    public void switchToRankingScene(ActionEvent event) throws IOException {

    }

    @FXML
    public void returnToMain(MouseEvent event) throws IOException {
        String message = "ROZLACZ";//TODO
        client.write(message);
        // root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("startScene.fxml")));
        // stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // scene = new Scene(root);
        // stage.setScene(scene);
        // stage.show();
        client.nextScene("startScene.fxml");
    }
}
