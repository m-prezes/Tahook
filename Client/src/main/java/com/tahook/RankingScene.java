package com.tahook;

import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class RankingScene {

    private Client client;

    public RankingScene() {
        client = Client.getInstance();
    }

    public void switchToPodiumScene(MouseEvent event) throws IOException {
        client.write("send");
    }
}
