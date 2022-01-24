package com.tahook.player;

import com.tahook.Client;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class WaitingForHostScene {
    private Client client;

    public WaitingForHostScene() {
        client = Client.getInstance();
    }

    public void switchToQuestionScene(MouseEvent event) throws IOException {

    }
}
