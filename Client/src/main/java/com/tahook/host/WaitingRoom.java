package com.tahook.host;

import com.tahook.Client;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.geometry.Pos;

import javafx.scene.control.Label;

import javafx.scene.layout.GridPane;

import javafx.util.Duration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

import java.util.Set;

public class WaitingRoom {
    @FXML
    private Label pinLabel;

    @FXML
    private GridPane playersGrid;

    private Client client;
    private JSONObject playersList;

    private Label playerLabel;

    @FXML
    public void initialize() {
        JSONParser parser = new JSONParser();
        client = Client.getInstance();
        pinLabel.setText(client.getPin());

        PauseTransition updatePlayers = new PauseTransition(Duration.seconds(0.1));
        updatePlayers.setOnFinished((e) -> {
            try {
                playersList = (JSONObject) parser.parse(client.getPlayers());
                Set<String> keys = playersList.keySet();
                for (String key : keys) {
                    Integer i = Integer.parseInt(key);
                    playerLabel = new Label();
                    playerLabel.setText(playersList.get(key).toString());
                    playerLabel.getStyleClass().add("player-label");
                    playerLabel.setAlignment(Pos.CENTER);
                    playersGrid.add(playerLabel, i % 3, i / 3);
                }

            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            updatePlayers.playFromStart();
        });
        updatePlayers.play();

    }

    public void switchToQuestionScene(ActionEvent event) throws IOException {
        client.write("start");
    }
}
