package Server;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple, easy-to-read thread-safe Board model using synchronized methods.
 * This implementation favors clarity over concurrency optimizations.
 */
public class Board {
	private int bW, bH, nW, nH;
	private List<String> validColors;
	private  List<Note> notes = new ArrayList<>();
	private List<Pin> pins = new ArrayList<>();

	public Board(int bW, int bH, int nW, int nH, List<String> colors) {
		this.bW = bW; this.bH = bH; this.nW = nW; this.nH = nH;
		this.validColors = new ArrayList<>(Objects.requireNonNull(colors));
	}

	private boolean inBounds(int x, int y) {
		return x >= 0 && y >= 0 && x + nW <= bW && y + nH <= bH;
	}

	public synchronized String post(int x, int y, String color, String msg) {
		if (!inBounds(x, y)) return "ERROR OUT_OF_BOUNDS";
		if (!validColors.contains(color)) return "ERROR INVALID_COLOR";
		for (Note n : notes) if (n.isIdenticalRegion(x, y)) return "ERROR OVERLAP_ERROR";
		notes.add(new Note(x, y, nW, nH, color, msg));
		return "OK";
	}

	public synchronized String pin(int x, int y) {
		boolean hit = false;
		for (Note n : notes) {
			if (n.contains(x, y)) { n.pins.add(new Pin(x, y)); hit = true; }
		}
		if (hit) { pins.add(new Pin(x, y)); return "OK"; }
		return "ERROR PIN_MISS";
	}

	public synchronized String unpin(int x, int y) {
		boolean removed = pins.removeIf(p -> p.getX() == x && p.getY() == y);
		if (!removed) return "ERROR NO_PIN_AT_COORD";
		for (Note n : notes) n.pins.removeIf(p -> p.getX() == x && p.getY() == y);
		return "OK";
	}

	public synchronized void shake() {
		notes.removeIf(n -> !n.isPinned());
		pins.removeIf(p -> notes.stream().noneMatch(n -> n.contains(p.getX(), p.getY())));
	}

	public synchronized void clear() {
		notes.clear();
		pins.clear();
	}

	public synchronized String getNotes(String colorParam, Integer containsX, Integer containsY, String refersTo) {
		List<Note> filtered = notes.stream().filter(n -> {
			boolean match = true;
			if (colorParam != null) match &= n.getColor().equalsIgnoreCase(colorParam);
			if (containsX != null && containsY != null) match &= n.contains(containsX, containsY);
			if (refersTo != null) match &= n.getMessage().toLowerCase().contains(refersTo.toLowerCase());
			return match;
		}).collect(Collectors.toList());

		if (filtered.isEmpty()) return "NOTES EMPTY";
		StringBuilder sb = new StringBuilder("NOTES ");
		for (Note n : filtered) sb.append(String.format("(%d,%d,%s,%s,%b) ", n.getX(), n.getY(), n.getColor(), n.getMessage(), n.isPinned()));
		return sb.toString().trim();
	}

	public synchronized String getPins() {
		if (pins.isEmpty()) return "PINS EMPTY";
		StringBuilder sb = new StringBuilder("PINS ");
		for (Pin p : pins) sb.append(String.format("(%d,%d) ", p.getX(), p.getY()));
		return sb.toString().trim();
	}

	public synchronized String getInitParams() {
		return String.format("BOARD %d %d\nNOTE %d %d\nCOLORS %s", bW, bH, nW, nH, String.join(",", validColors));
	}
}
