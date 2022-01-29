package com.tahook.player;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

import com.tahook.Client;

public class WaitingForHostScene {

    @FXML
    private Label pinLabel;

    private Client client;

    public WaitingForHostScene() {
        client = Client.getInstance();
    }

    @FXML
    public void initialize() {
        pinLabel.setText(client.getPin());
    }

    public void switchToQuestionScene(MouseEvent event) throws IOException {

    }
}
