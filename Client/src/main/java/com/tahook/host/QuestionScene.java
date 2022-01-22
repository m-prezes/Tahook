package com.tahook.host;

import com.tahook.Client;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Objects;

public class QuestionScene {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Client client;
    JSONParser parser = new JSONParser();
    private JSONObject receivedMessage;

    @FXML
    private Label fx_nAnswers, fxTime_left, fxAnswer_a, fxAnswer_b, fxAnswer_c, fxAnswer_d, fx_question;
    // private Label fxAnswer_a;

    @FXML
    public void initialize() {
        fxAnswer_a.setText(receivedMessage.get("answer_a").toString());
        fxAnswer_b.setText(receivedMessage.get("answer_b").toString());
        fxAnswer_c.setText(receivedMessage.get("answer_c").toString());
        fxAnswer_d.setText(receivedMessage.get("answer_d").toString());
        fx_nAnswers.setText(receivedMessage.get("answers_count").toString());
        fxTime_left.setText(Integer.toString(Integer.parseInt(receivedMessage.get("answer_time").toString()) / 100));
        fx_question.setText(receivedMessage.get("question").toString());
        PauseTransition wait = new PauseTransition(Duration.seconds(1));
        wait.setOnFinished((e) -> {
            fxTime_left.setText(Integer.toString((Integer.parseInt(fxTime_left.getText())) - 1));
            wait.playFromStart();
        });
        wait.play();
    }

    public QuestionScene() throws ParseException {
        client = Client.getInstance();
        // przykladowa wiadomość TODO
        receivedMessage = (JSONObject) parser.parse(
                "{\"question\":\"test\",\"answer_a\":\"1\",\"answers_count\":12,\"answer_b\":\"2\",\"answer_time\":10000,\"answer_c\":\"3\",\"answer_d\":\"4\"}");
    }

    // Kiedy przyjdzie odpowiedz z serwera wykonaj:
    public void switchToRankingScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/tahook/rankingScene.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
