package com.tahook;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class StartScene {
    private Stage stage;

    public void createNewGame(MouseEvent event) {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            Client client = Client.getInstance();
            client.joinGame(stage, 1111, true);

            Thread thread = new Thread() {
                public void run() {
                    try {
                        client.work();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String errMessage = "Server does not respond";
                    Alert a = new Alert(AlertType.ERROR);
                    a.setTitle(errMessage);
                    a.setContentText(errMessage);
                    a.show();
                }
            });
        }
    }

    public void joinGame(MouseEvent event) {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            Client client = Client.getInstance();
            client.joinGame(stage, 2222, false);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        client.work();
                    } catch (IOException e) {

                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    String errMessage = "Server does not respond";
                    Alert a = new Alert(AlertType.ERROR);
                    a.setTitle(errMessage);
                    a.setContentText(errMessage);
                    a.show();
                }
            });
        }

    }

}
