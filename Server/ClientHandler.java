
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Board board;

    public ClientHandler(Socket socket, Board board) {
        this.socket = socket;
        this.board = board;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Handshake
            String init = board.getInitParams();
            for (String hline : init.split("\\n")) {
                out.println(hline);
                System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + hline);
            }

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                System.out.println("RECV from " + socket.getRemoteSocketAddress() + ": " + line);

                String upper = line.toUpperCase();
                try {
                    if (upper.equals("DISCONNECT")) {
                        String resp = "OK DISCONNECTED";
                        out.println(resp);
                        System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                        break;
                    } else if (upper.startsWith("POST ")) {
                        String[] parts = line.split("\\s+", 5);
                        if (parts.length < 5) {
                            String resp = "ERROR INVALID_FORMAT POST requires coordinates, color, and message";
                            out.println(resp);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                            continue;
                        }
                        int x = Integer.parseInt(parts[1]); int y = Integer.parseInt(parts[2]);
                        String color = parts[3];
                        String msg = parts[4];
                        String pres = board.post(x, y, color, msg);
                        if (pres != null && pres.startsWith("ERROR")) {
                            out.println(pres);
                        } else {
                            out.println(pres);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + pres);
                        }
                    } else if (upper.equals("GET PINS")) {
                        String pins = board.getPins();
                        if (pins != null && pins.startsWith("ERROR")) {
                            out.println(pins);
                        } else {
                            out.println(pins);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + pins);
                        }
                    } else if (upper.startsWith("GET")) {
                        String rest = line.length() > 3 ? line.substring(3).trim() : "";
                        String color = null; Integer cx = null; Integer cy = null; String refersTo = null;
                        String[] parts = rest.isEmpty() ? new String[0] : rest.split("\\s+");
                        for (int i=0;i<parts.length;i++) {
                            String t = parts[i];
                            if (t.startsWith("color=")) color = t.substring(6);
                            else if (t.startsWith("contains=")) {
                                String after = t.substring(9);
                                if (!after.isEmpty() && after.contains(",")) {
                                    String[] nums = after.split(","); cx = Integer.parseInt(nums[0]); cy = Integer.parseInt(nums[1]);
                                } else if (!after.isEmpty()) {
                                    cx = Integer.parseInt(after);
                                    if (i+1<parts.length) { cy = Integer.parseInt(parts[++i]); }
                                }
                            } else if (t.startsWith("refersTo=")) {
                                refersTo = t.substring(9);
                            }
                        }
                        String notes = board.getNotes(color, cx, cy, refersTo);
                        if (notes != null && notes.startsWith("ERROR")) {
                            out.println(notes);
                        } else {
                            out.println(notes);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + notes);
                        }
                    } else if (upper.startsWith("PIN ")) {
                        String[] p = line.split("\\s+");
                        if (p.length != 3) {
                            String resp = "ERROR INVALID_FORMAT PIN requires coordinates";
                            out.println(resp);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                            continue;
                        }
                        int x = Integer.parseInt(p[1]); int y = Integer.parseInt(p[2]);
                        String resp = board.pin(x,y);
                        if (resp != null && resp.startsWith("ERROR")) {
                            out.println(resp);
                        } else {
                            out.println(resp);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                        }
                    } else if (upper.startsWith("UNPIN ")) {
                        String[] p = line.split("\\s+");
                        if (p.length != 3) {
                            String resp = "ERROR INVALID_FORMAT UNPIN requires coordinates";
                            out.println(resp);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                            continue;
                        }
                        int x = Integer.parseInt(p[1]); int y = Integer.parseInt(p[2]);
                        String resp = board.unpin(x,y);
                        if (resp != null && resp.startsWith("ERROR")) {
                            out.println(resp);
                        } else {
                            out.println(resp);
                            System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                        }
                    } else if (upper.equals("SHAKE")) {
                        board.shake();
                        String resp = "OK SHAKE_COMPLETE";
                        out.println(resp);
                        System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                    } else if (upper.equals("CLEAR")) {
                        board.clear();
                        String resp = "OK CLEAR_COMPLETE";
                        out.println(resp);
                        System.out.println("SENT to " + socket.getRemoteSocketAddress() + ": " + resp);
                    } else {
                        String resp = "ERROR UNKNOWN_COMMAND";
                        out.println(resp);
                    }
                } catch (NumberFormatException nfe) {
                    String resp = "ERROR INVALID_FORMAT Number expected";
                    out.println(resp);
                } catch (Exception e) {
                    String resp = "ERROR SERVER_ERROR";
                    out.println(resp);
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected unexpectedly: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
