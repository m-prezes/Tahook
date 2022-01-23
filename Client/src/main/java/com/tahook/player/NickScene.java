package com.tahook.player;

import com.tahook.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TextField;

import java.io.IOException;

public class NickScene {

    private Client client;

    @FXML
    private TextField nickField;

    @FXML
    public void initialize() {
        client = Client.getInstance();
    }

    public void switchToWaitingForHostScene(ActionEvent event) throws IOException {

        System.out.println(nickField.getText());
        client.write(nickField.getText());

    }

}
