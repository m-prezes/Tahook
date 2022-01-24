package com.tahook.host;

import com.tahook.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Label;

import java.io.IOException;

public class WaitingRoom {
    @FXML
    private Label pinLabel;

    private Client client;

    @FXML
    public void initialize() {
        client = Client.getInstance();
        pinLabel.setText(client.getPin());

    }

    public void switchToQuestionScene(ActionEvent event) throws IOException {
        client.write("start");
    }
}
