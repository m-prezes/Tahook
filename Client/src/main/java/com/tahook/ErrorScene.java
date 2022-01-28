package com.tahook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class ErrorScene {
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
        client.nextScene("startScene.fxml");
    }
}
