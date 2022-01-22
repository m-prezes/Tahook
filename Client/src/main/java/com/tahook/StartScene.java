package com.tahook;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class StartScene {
    private Stage stage;

    public void createNewGame(MouseEvent event) throws IOException {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        TestClient c = new TestClient(stage, 1111);

        Thread thread = new Thread() {
            public void run() {
                try {
                    c.work();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void joinGame(MouseEvent event) throws IOException {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        TestClient c = new TestClient(stage, 2222);

        Thread thread = new Thread() {
            public void run() {
                try {
                    c.work();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

    }

}
