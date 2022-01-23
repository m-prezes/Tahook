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
    Stage stage;
    Selector selector;
    SocketChannel clientChanel;
    String data;

    private Client() {
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    public String getData() {
        return data;
    }

    public void joinGame(Stage s, int pin) throws IOException {
        stage = s;
        selector = Selector.open();
        clientChanel = SocketChannel.open();
        clientChanel.connect(new InetSocketAddress("localhost", pin));
        clientChanel.configureBlocking(false);
        clientChanel.register(selector, SelectionKey.OP_READ);
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
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int byteRead = channel.read(buffer);
                    System.out.println(byteRead);

                    String str = new String(buffer.array(), "UTF-8");
                    str = str.substring(0, byteRead);
                    System.out.println(str);

                    if (str.equals("Enter nick:")) {
                        nextScene("player/nickScene.fxml");
                    } else if (str.equals("Enter pin:")) {
                        nextScene("player/pinScene.fxml");
                    } else if (str.equals("Joined game")) {
                        nextScene("player/waitingForHostScene.fxml");
                    } else if (str.equals("Need questions!")) {
                        nextScene("host/createGameScene.fxml");
                    } else if (str.substring(0, 4).equals("PIN:")) {
                        data = str.substring(4, str.length());
                        nextScene("host/waitingRoom.fxml");
                    }

                }
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
            // TODO Auto-generated catch block
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
