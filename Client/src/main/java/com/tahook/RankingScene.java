package com.tahook;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Objects;

public class RankingScene {

    private Client client;

    public RankingScene() {
        client = Client.getInstance();
    }

    public void switchToPodiumScene(MouseEvent event) throws IOException {
        if (client.gamestate == 2) {
            client.write("send");
        } else {
            client.write("end");
            Parent root;
            try {
                root = FXMLLoader.load(Objects
                        .requireNonNull(getClass().getResource("podiumScene.fxml")));
                Scene scene = new Scene(root);
                client.stage.setScene(scene);
                client.stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
