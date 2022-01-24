package com.tahook;

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

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client {
    private final static Client INSTANCE = new Client();
    String inputBuffer;
    boolean isHost;
    Stage stage;
    Selector selector;
    SocketChannel clientChanel;
    String pin;
    String question;
    String players;
    int gamestate;
    String currentAnswers;
    String ranking;

    private Client() {
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    public String getPin() {
        return pin;
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

    public void joinGame(Stage s, int pin, boolean host) throws IOException {
        stage = s;
        selector = Selector.open();
        clientChanel = SocketChannel.open();
        clientChanel.connect(new InetSocketAddress("localhost", pin));
        clientChanel.configureBlocking(false);
        clientChanel.register(selector, SelectionKey.OP_READ);
        gamestate = 0;
        isHost = host;
        players = "{}";
        currentAnswers = "{}";
        inputBuffer = "";
    }

    public void work() throws IOException {

        while (true) {
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
                            System.out.println(str);

                            handleMessage(str);

                        }
                    }

                }
            }
        }
    }

    void handleMessage(String str) {
        if (isHost) {
            if (str.equals("Need questions!")) {
                nextScene("host/createGameScene.fxml");
            } else if (str.substring(0, 4).equals("PIN:")) {
                gamestate++;
                pin = str.substring(4, str.length());
                nextScene("host/waitingRoom.fxml");
            } else if (gamestate == 1) {
                if (str.equals("Start game")) {
                    gamestate++;
                } else {
                    players = str;
                    nextScene("host/waitingRoom.fxml");

                }

            } else if (gamestate == 2) {
                if (str.substring(0, 9).equals("question:")) {
                    question = str.substring(9, str.length());
                    nextScene("host/questionScene.fxml");
                } else if (str.substring(0, 8).equals("answers:")) {
                    players = str.substring(8, str.length());
                    nextScene("host/questionScene.fxml");
                } else if (str.substring(0, 8).equals("ranking:")) {
                    ranking = str.substring(8, str.length());
                    nextScene("/com/tahook/rankingScene.fxml");
                } else if (str.equals("Game has ended!")) {
                    gamestate++;
                }

            }

        } else {
            if (str.equals("Enter nick:")) {
                nextScene("player/nickScene.fxml");
            } else if (str.equals("Enter pin:")) {
                nextScene("player/pinScene.fxml");
            } else if (str.equals("Joined game")) {
                gamestate++;
                nextScene("player/waitingForHostScene.fxml");
            } else if (gamestate == 1) {
                System.out.println(str);
                question = str.substring(9, str.length());
                nextScene("player/questionScene.fxml");
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
}
