package com.tahook.host;

import com.tahook.Client;
import com.tahook.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CreateGameScene {
    private Client client;
    private Stage stage;
    private Scene scene;
    private Parent root;
    private ObservableList<Question> questionList;
    private String correctAns = "a";

    @FXML
    private TextField tfQuestion;
    @FXML
    private TextField tfAnswer_a;
    @FXML
    private TextField tfAnswer_b;
    @FXML
    private TextField tfAnswer_c;
    @FXML
    private TextField tfAnswer_d;
    @FXML
    private TextField tfTime;
    @FXML
    private RadioButton tf_correct_a, tf_correct_b, tf_correct_c, tf_correct_d;

    @FXML
    private TreeView<String> treeViewQuestion;

    @FXML
    private TreeItem<String> rootItem;
    @FXML
    private TreeItem<String> selectedItem;

    @FXML
    public void initialize() {
        questionList = FXCollections.observableArrayList();
        rootItem = new TreeItem<>("root");
        treeViewQuestion.setRoot(rootItem);
        treeViewQuestion.setShowRoot(false);
        selectedItem = null;
        client = Client.getInstance();
    }

    public void addQuestionToTreeView(Question q) throws Exception {
        TreeItem<String> question = new TreeItem<>(q.getQuestion());
        TreeItem<String> a_a = new TreeItem<>(q.getAnswer_a());
        TreeItem<String> a_b = new TreeItem<>(q.getAnswer_b());
        TreeItem<String> a_c = new TreeItem<>(q.getAnswer_c());
        TreeItem<String> a_d = new TreeItem<>(q.getAnswer_d());
        TreeItem<String> corAns = new TreeItem<>(q.getCorrectAnswer());
        TreeItem<String> time = new TreeItem<>(Integer.toString(q.getAnswer_time()));
        question.getChildren().add(a_a);
        question.getChildren().add(a_b);
        question.getChildren().add(a_d);
        question.getChildren().add(a_c);
        question.getChildren().add(corAns);
        question.getChildren().add(time);
        rootItem.getChildren().add(question);
    }

    public void addQuestion(ActionEvent event) throws Exception {
        Integer time = Integer.parseInt(tfTime.getText());// TODO
        // System.out.println(correctAns);
        Question question = new Question(tfQuestion.getText(),
                tfAnswer_a.getText(), tfAnswer_b.getText(), tfAnswer_c.getText(), tfAnswer_d.getText(), time,
                correctAns);// TODO
        System.out.println(question.getJSON());
        questionList.add(question);
        addQuestionToTreeView(question);
        tfQuestion.clear();
        tfAnswer_a.clear();
        tfAnswer_b.clear();
        tfAnswer_c.clear();
        tfAnswer_d.clear();
        tfTime.setText("60");
        tf_correct_a.setSelected(true);
        tf_correct_b.setSelected(false);
        tf_correct_c.setSelected(false);
        tf_correct_d.setSelected(false);
        correctAns = "a";
    };

    public void selectQuestion() {
        selectedItem = treeViewQuestion.getSelectionModel().getSelectedItem();
        // System.out.println(selectedItem.getValue());
        if (selectedItem != null && !selectedItem.getValue().equals("root")) {
            if (selectedItem.getParent().getValue().equals("root")) {
                List<TreeItem<String>> children = selectedItem.getChildren();
                tfQuestion.setText(selectedItem.getValue());
                tfAnswer_a.setText(children.get(0).getValue());
                tfAnswer_b.setText(children.get(1).getValue());
                tfAnswer_c.setText(children.get(2).getValue());
                tfAnswer_d.setText(children.get(3).getValue());
                tfTime.setText(children.get(5).getValue());
                switch (children.get(4).getValue()) {
                    case "a":
                        tf_correct_a.setSelected(true);
                        break;
                    case "b":
                        tf_correct_b.setSelected(true);
                        break;
                    case "c":
                        tf_correct_c.setSelected(true);
                        break;
                    case "d":
                        tf_correct_d.setSelected(true);
                        break;
                }

            }
        }
    }

    public void wyczyscPola(ActionEvent event) throws IOException {
        tfQuestion.clear();
        tfAnswer_a.clear();
        tfAnswer_b.clear();
        tfAnswer_c.clear();
        tfAnswer_d.clear();
        tfTime.setText("60");
        tf_correct_a.setSelected(true);
        tf_correct_b.setSelected(false);
        tf_correct_c.setSelected(false);
        tf_correct_d.setSelected(false);

    }

    public void usunPytanie(ActionEvent event) throws IOException {
        if (selectedItem != null) {
            questionList.remove(rootItem.getChildren().indexOf(selectedItem));
            rootItem.getChildren().remove(selectedItem);
            selectedItem = null;
        }
    }

    public void switchToWaitingRoom(ActionEvent event) throws IOException {
        // ZAMIST PRINTLN WYSLIJ DO SERWERA
        System.out.println(prepareMessage(questionList));

        client.write(prepareMessage(questionList));

        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("waitingRoom.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void getCorrectAnswer(ActionEvent event) {
        if (tf_correct_a.isSelected()) {
            correctAns = "a";
        } else if (tf_correct_b.isSelected()) {
            correctAns = "b";
        } else if (tf_correct_c.isSelected()) {
            correctAns = "c";
        } else if (tf_correct_d.isSelected()) {
            correctAns = "d";
        }
        System.out.println(correctAns);
    }

    public String prepareMessage(ObservableList<Question> questionList) {
        String message = "[";

        for (Question question : questionList) {
            message += question.getJSON() + ',';
        }
        if (message != null && message.length() > 0 && message.charAt(message.length() - 1) == ',') {
            message = message.substring(0, message.length() - 1);
        }
        message += "]";

        return message;
    }
}
