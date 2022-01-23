package com.tahook.player;

import com.tahook.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.io.IOException;

public class PinScene {

    @FXML
    private TextField pinField;

    private Client client;

    public PinScene() {
        client = Client.getInstance();
    }

    public void switchToNickScene(ActionEvent event) throws IOException {

        System.out.println(pinField.getText());
        client.write(pinField.getText());
    }
}
