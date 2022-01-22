package com.tahook;

import java.net.*;
import java.io.*;

public final class Client {
    private final static Client INSTANCE = new Client();
    private String hostName = "localhost";
    private Socket socket;
    private OutputStream output;
    private PrintWriter writer;
    private InputStream input;
    private InputStreamReader reader;

    private Client() {
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    public void joinServer(int port) {
        try {
            socket = new Socket(hostName, port);
            output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            input = socket.getInputStream();
            reader = new InputStreamReader(input);

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public void write(String message) {
        writer.println(message);
    }

    public void read() {
        try {

            int character;
            StringBuilder data = new StringBuilder();

            while ((character = reader.read()) != -1) {
                data.append((char) character);
            }

            System.out.println(data);

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
