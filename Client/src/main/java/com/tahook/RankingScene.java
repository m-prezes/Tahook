package com.tahook;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RankingScene {

    private Client client;
    private JSONArray ranking;
    private Label playerLabel;

    @FXML
    private GridPane rankingGrid;

    @FXML
    private Label resultLabel;

    @FXML
    private Pane resultPane;

    public RankingScene() {

    }

    @FXML
    public void initialize() {
        JSONParser parser = new JSONParser();
        client = Client.getInstance();
        try {
            ranking = (JSONArray) parser.parse(client.getRanking());
            Collections.sort(ranking, new MyJSONComparator());

            for (int i = 0; i < ranking.size() && i < 5; i++) {
                JSONObject obj = (JSONObject) ranking.get(i);

                playerLabel = new Label();
                playerLabel.setText(obj.get("userName").toString() + " - " + obj.get("points").toString());
                playerLabel.getStyleClass().add("player-label-ranking");
                playerLabel.setAlignment(Pos.CENTER);
                rankingGrid.add(playerLabel, 0, i);

            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        client.sortedRanking = ranking;

        if (client.isHost) {
            resultLabel.setText("Dalej...");
            resultPane.getStyleClass().add("default");
        } else {
            if (client.getIsAnswerCorrect()) {
                resultLabel.setText("Brawo!");
                resultPane.getStyleClass().add("correct");
            } else {
                resultLabel.setText("Źle!");
                resultPane.getStyleClass().add("wrong");

            }
        }

    }

    public void switchToPodiumScene(MouseEvent event) throws IOException {
        if (client.isHost && client.gamestate == 2) {
            client.write("send");
        }

    }

}

class MyJSONComparator implements Comparator<JSONObject> {

    @Override
    public int compare(JSONObject o1, JSONObject o2) {
        Integer v1 = Integer.parseInt(o1.get("points").toString());
        Integer v3 = Integer.parseInt(o2.get("points").toString());
        return v3.compareTo(v1);
    }
}