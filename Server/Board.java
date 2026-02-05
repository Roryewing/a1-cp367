import java.util.*;
import java.util.stream.Collectors;

public class Board {
    private int bW, bH, nW, nH;
    private List<String> validColors;
    private List<Note> notes = new ArrayList<>();

    public Board(int bW, int bH, int nW, int nH, List<String> colors) {
        this.bW = bW; this.bH = bH; this.nW = nW; this.nH = nH; this.validColors = colors;
    }

    public synchronized String post(int x, int y, String color, String msg) {
        if (x < 0 || y < 0 || x + nW > bW || y + nH > bH) return "ERROR OUT_OF_BOUNDS";
        if (!validColors.contains(color)) return "ERROR INVALID_COLOR";
        for (Note n : notes) {
            if (n.x == x && n.y == y) return "ERROR OVERLAP_ERROR";
        }
        notes.add(new Note(x, y, nW, nH, color, msg));
        return "OK";
    }

    public synchronized String pin(int x, int y) {
        boolean hit = false;
        for (Note n : notes) {
            if (n.contains(x, y)) {
                n.pins.add(new Pin(x, y));
                hit = true;
            }
        }
        return hit ? "OK" : "ERROR PIN_MISS";
    }

    public synchronized String unpin(int x, int y) {
        boolean removed = false;
        for (Note n : notes) {
            removed |= n.pins.removeIf(p -> p.x == x && p.y == y);
        }
        return removed ? "OK" : "ERROR NO_PIN_AT_COORD";
    }

    public synchronized void shake() {
        notes.removeIf(n -> !n.isPinned());
    }

    public synchronized void clear() {
        notes.clear();
    }

    // Advanced GET logic (Section 6.5)
    public synchronized String getNotes(String colorParam, Integer containsX, Integer containsY, String refersTo) {
        List<Note> filtered = notes.stream().filter(n -> {
            boolean match = true;
            if (colorParam != null) match &= n.color.equalsIgnoreCase(colorParam);
            if (containsX != null && containsY != null) match &= n.contains(containsX, containsY);
            if (refersTo != null) match &= n.message.toLowerCase().contains(refersTo.toLowerCase());
            return match;
        }).collect(Collectors.toList());

        if (filtered.isEmpty()) return "NOTES EMPTY";
        
        StringBuilder sb = new StringBuilder("NOTES ");
        for (Note n : filtered) {
            // Format: (x,y,color,message,isPinned)
            sb.append(String.format("(%d,%d,%s,%s,%b) ", n.x, n.y, n.color, n.message, n.isPinned()));
        }
        return sb.toString().trim();
    }

    public synchronized String getPins() {
        StringBuilder sb = new StringBuilder("PINS ");
        for (Note n : notes) {
            for (Pin p : n.pins) {
                sb.append(String.format("(%d,%d) ", p.x, p.y));
            }
        }
        return sb.toString().trim();
    }

    public String getInitParams() {
        return String.format("BOARD %d %d\nNOTE %d %d\nCOLORS %s", bW, bH, nW, nH, String.join(",", validColors));
    }
}
