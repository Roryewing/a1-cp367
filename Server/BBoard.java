import java.net.*;
import java.io.*;

public class BBoard {

    public static void main(String[] args) {
        if (args.length != 6) {
            System.err.println(
                "Usage: java BBoard <port> <board_w> <board_h> <note_w> <note_h> <colors>"
            );
            return;
        }

        int port = Integer.parseInt(args[0]);
        int boardW = Integer.parseInt(args[1]);
        int boardH = Integer.parseInt(args[2]);
        int noteW = Integer.parseInt(args[3]);
        int noteH = Integer.parseInt(args[4]);
        String colors = args[5];

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client connected");
                new Thread(new ClientHandler(
                        client, boardW, boardH, noteW, noteH, colors)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
