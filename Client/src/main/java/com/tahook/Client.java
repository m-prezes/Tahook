package com.tahook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;

import org.json.simple.JSONArray;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class Client {
    private final static Client INSTANCE = new Client();
    String inputBuffer;
    boolean isHost;
    Stage stage;
    Selector selector;
    SocketChannel clientChanel;
    public String pin;
    String question;
    String players;
    int gamestate;
    String currentAnswers;
    String ranking;
    String errorMessage;
    JSONArray sortedRanking;
    Boolean isAnswerCorrect;
    Boolean isActiveSelector;

    private Client() {
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    public String getPin() {
        return pin;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getPlayers() {
        return players;
    }

    public String getQuestion() {
        return question;
    }

    public String getCurrentAnswers() {
        return currentAnswers;
    }

    public String getRanking() {
        return ranking;
    }

    public Boolean getIsAnswerCorrect() {
        return isAnswerCorrect;
    }
    public String getConfigInfo(){
        String ip = "localhost";
        // System.out.println(new File(".").getAbsolutePath());
        try {
            File config = new File("config.txt");
            Scanner myReader = new Scanner(config);
                if (myReader.hasNextLine()) {
                    ip = myReader.nextLine();
                    // System.out.println(ip);
                }else{
                    System.out.println("Plik konfiguracyjny jest pusty!");
                }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Brak pliku konfiguracyjego!");
            e.printStackTrace();
        }
        System.out.println(ip);
        return ip;
    }

    public void joinGame(Stage s, int port, boolean host) throws IOException {
        stage = s;
        isActiveSelector = true;
        selector = Selector.open();
        clientChanel = SocketChannel.open();
        clientChanel.connect(new InetSocketAddress(getConfigInfo(), port));
        clientChanel.configureBlocking(false);
        clientChanel.register(selector, SelectionKey.OP_READ);
        gamestate = 0;
        isHost = host;
        players = "{}";
        currentAnswers = "{\"currAnswers\":0}";
        inputBuffer = "";
        isAnswerCorrect = false;
    }

    public void work() throws IOException {

        while (isActiveSelector) {
            // select() can block!
            if (selector.select() == 0) {
                continue;
            }

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();

                    // zczytywanie po jednym znaku i zapisywanie do inputBuffora
                    ByteBuffer buffer = ByteBuffer.allocate(1);
                    int byteRead = channel.read(buffer);
                    if (byteRead > 0) {
                        if (buffer.array()[0] != (byte) '\n') {
                            inputBuffer += new String(buffer.array(), "UTF-8");
                        } else {
                            String str = inputBuffer;
                            inputBuffer = "";

                            handleMessage(str);

                        }
                    } else if (byteRead == -1) {
                        channel.close();
                    }

                } else if (key.isValid()) {
                    System.out.println("ERROR with connection");
                }
            }
        }
    }

    void handleMessage(String str) {
        if (isHost) {
            handleHostGame(str);
        } else {
            handlePlayerGame(str);
        }
    }

    void handleHostGame(String str) {
        if (str.substring(0, 6).equals("error:")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String errMessage = str.substring(6, str.length());
                    Alert a = new Alert(AlertType.ERROR);
                    a.setTitle(errMessage);
                    a.setContentText(errMessage);
                    a.show();
                }
            });
        } else if (str.equals("Need questions!")) {
            nextScene("host/createGameScene.fxml");
        } else if (str.substring(0, 4).equals("PIN:")) {
            gamestate++;
            pin = str.substring(4, str.length());
            nextScene("host/waitingRoom.fxml");
        } else if (gamestate == 1) {
            if (str.equals("Start game")) {
                gamestate++;
            } else if (str.substring(0, 8).equals("players:")) {
                players = str.substring(8, str.length());
                nextScene("host/waitingRoom.fxml");

            }

        } else if (gamestate == 2) {
            if (str.substring(0, 9).equals("question:")) {
                question = str.substring(9, str.length());
                currentAnswers = "{\"currAnswers\":0}";
                nextScene("questionScene.fxml");
            } else if (str.substring(0, 8).equals("answers:")) {
                currentAnswers = str.substring(8, str.length());
                // nextScene("questionScene.fxml");
            } else if (str.substring(0, 8).equals("ranking:")) {
                ranking = str.substring(8, str.length());
                nextScene("rankingScene.fxml");
            } else if (str.equals("Game has ended!")) {
                gamestate++;
                nextScene("podiumScene.fxml");
            } else {
                System.out.println(str);
            }

        }
    }

    void handlePlayerGame(String str) {
        if (str.substring(0, 6).equals("error:")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String errMessage = str.substring(6, str.length());
                    Alert a = new Alert(AlertType.ERROR);
                    a.setTitle(errMessage);
                    a.setContentText(errMessage);
                    a.show();
                }
            });
        } else if (str.equals("Enter nick:")) {
            nextScene("player/nickScene.fxml");
        } else if (str.equals("Enter pin:")) {
            nextScene("player/pinScene.fxml");
        } else if (str.substring(0, 10).equals("critError:")) {
            errorMessage = str.substring(10, str.length());
            nextScene("errorScene.fxml");
        } else if (str.equals("Joined game")) {
            gamestate++;
            nextScene("player/waitingForHostScene.fxml");
        } else if (gamestate == 1) {
            if (str.substring(0, 9).equals("question:")) {
                isAnswerCorrect = false;
                question = str.substring(9, str.length());
                nextScene("questionScene.fxml");
            } else if (str.substring(0, 8).equals("ranking:")) {
                ranking = str.substring(8, str.length());
                nextScene("rankingScene.fxml");

            } else if (str.equals("Game has ended!")) {
                gamestate++;
                nextScene("podiumScene.fxml");
            } else if (str.substring(0, 16).equals("isAnswerCorrect:")) {
                System.out.print(str);
                isAnswerCorrect = Boolean.valueOf(str.substring(16, str.length()));
            }
        }
    }

    void nextScene(String template) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                Parent root;
                try {
                    root = FXMLLoader.load(Objects
                            .requireNonNull(getClass().getResource(template)));
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void write(String message) {
        try {
            clientChanel.write(str_to_bb(message + "\n", StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ByteBuffer str_to_bb(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    public static String bb_to_str(ByteBuffer buffer, Charset charset) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        return new String(bytes, charset);
    }

    void reset() {
        try {
            if (clientChanel.isOpen()) {
                isActiveSelector = false;
                selector.close();
                clientChanel.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
