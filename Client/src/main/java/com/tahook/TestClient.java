package com.tahook;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestClient {

    Stage stage;
    Selector selector;
    SocketChannel clientChanel;

    public TestClient(Stage s, int pin) throws IOException {
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
                    str = str.substring(0, byteRead); // [TODO]: -1 BO NOWA LNIA
                    System.out.println(str);

                    if (str.equals("Enter nick:")) {
                        nextScene("player/nickScene.fxml");
                    } else if (str.equals("Need questions!")) {
                        nextScene("host/createGameScene.fxml");
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

}
