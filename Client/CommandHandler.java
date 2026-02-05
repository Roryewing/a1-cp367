
public class CommandHandler {
	private ClientConnection connection;

	public CommandHandler(ClientConnection connection) {
		this.connection = connection;
	}

	public void disconnect() {
		connection.disconnect();
	}

	public void post(int x, int y, String color, String message) {
		
		connection.sendCommand(String.format("POST %d %d %s %s", x, y, color, message));
	}

	public void getPins() {
		connection.sendCommand("GET PINS");
	}

	public void get(String color, Integer x, Integer y, String refersTo) {
		StringBuilder sb = new StringBuilder();
		if (color != null && !color.isEmpty()) sb.append("color=").append(color).append(" ");
		if (x != null && y != null) sb.append("contains=").append(x).append(" ").append(y).append(" ");
		if (refersTo != null && !refersTo.isEmpty()) sb.append("refersTo=").append(refersTo).append(" ");
		String payload = sb.toString().trim();
		if (payload.isEmpty()) connection.sendCommand("GET");
		else connection.sendCommand("GET " + payload);
	}

	public void pin(int x, int y) {
		connection.sendCommand(String.format("PIN %d %d", x, y));
	}

	public void unpin(int x, int y) {
		connection.sendCommand(String.format("UNPIN %d %d", x, y));
	}

	public void shake() {
		connection.sendCommand("SHAKE");
	}

	public void clear() {
		connection.sendCommand("CLEAR");
	}
}

