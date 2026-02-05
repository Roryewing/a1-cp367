import java.util.ArrayList;
import java.util.List;

public class Note {
    public int x, y, width, height;
    public String color, message;
    public List<Pin> pins = new ArrayList<>();

    public Note(int x, int y, int w, int h, String color, String message) {
        this.x = x; this.y = y; this.width = w; this.height = h;
        this.color = color; this.message = message;
    }

    public boolean isPinned() { return !pins.isEmpty(); }
    
    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public boolean isIdenticalRegion(int nx, int ny) {
        return this.x == nx && this.y == ny;
    }
}
