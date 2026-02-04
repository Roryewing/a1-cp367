import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private int boardW, boardH, noteW, noteH;
    private String colors;

    public ClientHandler(Socket socket,
                         int boardW, int boardH,
                         int noteW, int noteH,
                         String colors) {
        this.socket = socket;
        this.boardW = boardW;
        this.boardH = boardH;
        this.noteW = noteW;
        this.noteH = noteH;
        this.colors = colors;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true)
        ) {
            // Handshake
            out.println("BOARD " + boardW + " " + boardH);
            out.println("NOTE " + noteW + " " + noteH);
            out.println("COLORS " + colors);

            String line;

            // Keep reading commands until client disconnects
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("DISCONNECT")) {
                    out.println("OK DISCONNECTED");
                    break;
                }

                // Echo back whatever the client sent
                out.println("ECHO " + line);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected unexpectedly");
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}