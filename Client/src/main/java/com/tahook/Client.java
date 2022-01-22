package com.tahook;

import java.net.*;
import java.nio.charset.StandardCharsets;
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

    public void read(String request) {
        try {

            byte[] bytearr = new byte[16];
            while (true) {
                int len = input.read(bytearr);
                if (len == -1)
                    break;

                String s = new String(bytearr, StandardCharsets.UTF_8);
                System.out.println(s);
                if (s.substring(0, len - 1).equals(request)) {

                }
            }

        } catch (Exception ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
