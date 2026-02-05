import java.net.*;
import java.util.*;

public class BBoard {
    public static void main(String[] args) throws IOException {
        if (args.length < 6) return;
        int port = Integer.parseInt(args[0]);
        Board board = new Board(Integer.parseInt(args[1]), Integer.parseInt(args[2]), 
                                Integer.parseInt(args[3]), Integer.parseInt(args[4]), 
                                Arrays.asList(Arrays.copyOfRange(args, 5, args.length)));

        ServerSocket Server = new ServerSocket(port);
        System.out.println("Server started on " + port);

        while (true) {
            Socket s = Server.accept();
            new Thread(() -> handleClient(s, board)).start();
        }
    }

    private static void handleClient(Socket s, Board b) {
        try (Scanner in = new Scanner(s.getInputStream());
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            
            out.println(b.getInitParams());
    
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(" ");
                String cmd = parts[0].toUpperCase();
    
                try {
                    switch (cmd) {
                        case "POST":
                            // POST <x> <y> <color> <msg...>
                            String[] postParts = line.split(" ", 5);
                            out.println(b.post(Integer.parseInt(postParts[1]), Integer.parseInt(postParts[2]), postParts[3], postParts[4]));
                            break;
    
                        case "PIN":
                            out.println(b.pin(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                            break;
    
                        case "UNPIN":
                            out.println(b.unpin(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                            break;
    
                        case "GET":
                            if (line.equalsIgnoreCase("GET PINS")) {
                                out.println(b.getPins());
                            } else {
                                // Parse filters
                                String color = line.contains("color=") ? extract(line, "color=") : null;
                                String refersTo = line.contains("refersTo=") ? extract(line, "refersTo=") : null;
                                Integer cx = null, cy = null;
                                if (line.contains("contains=")) {
                                    String[] c = extract(line, "contains=").split(" ");
                                    cx = Integer.parseInt(c[0]);
                                    cy = Integer.parseInt(c[1]);
                                }
                                out.println(b.getNotes(color, cx, cy, refersTo));
                            }
                            break;
    
                        case "SHAKE":
                            b.shake();
                            out.println("OK SHAKEN");
                            break;
    
                        case "CLEAR":
                            b.clear();
                            out.println("OK CLEARED");
                            break;
    
                        case "DISCONNECT":
                            out.println("OK DISCONNECTED");
                            return;
    
                        default:
                            out.println("ERROR UNKNOWN_COMMAND");
                    }
                } catch (Exception e) {
                    out.println("ERROR INVALID_FORMAT");
                }
            }
        } catch (IOException e) {
            System.out.println("Client lost connection.");
        }
    }
    
    // Helper to pull values out of the GET string
    private static String extract(String line, String key) {
        int start = line.indexOf(key) + key.length();
        int end = line.indexOf(" ", start);
        return (end == -1) ? line.substring(start) : line.substring(start, end);
    }
}
