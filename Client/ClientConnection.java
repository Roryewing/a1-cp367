import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientConnection {

	public interface ConnectionListener {
		void onConnected(int boardW, int boardH, int noteW, int noteH, List<String> colors);
		void onDisconnected();
		void onError(String message);
		void onServerResponse(String response);
	}

	private ConnectionListener listener;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private Thread readerThread;
	private List<String> colors = new ArrayList<>();

	public ClientConnection(ConnectionListener listener) {
		this.listener = listener;
	}

	public boolean connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			// Read handshake lines from server
			String boardLine = in.readLine(); // BOARD w h
			String noteLine = in.readLine();  // NOTE w h
			String colorsLine = in.readLine(); // COLORS c1,c2,...

			int boardW = 0, boardH = 0, noteW = 0, noteH = 0;
			if (boardLine != null && boardLine.startsWith("BOARD")) {
				String[] parts = boardLine.split("\\s+");
				if (parts.length >= 3) {
					boardW = Integer.parseInt(parts[1]);
					boardH = Integer.parseInt(parts[2]);
				}
			}
			if (noteLine != null && noteLine.startsWith("NOTE")) {
				String[] parts = noteLine.split("\\s+");
				if (parts.length >= 3) {
					noteW = Integer.parseInt(parts[1]);
					noteH = Integer.parseInt(parts[2]);
				}
			}
			if (colorsLine != null && colorsLine.startsWith("COLORS")) {
				String rest = colorsLine.substring("COLORS".length()).trim();
				if (!rest.isEmpty()) {
					colors = new ArrayList<>();
					colors.addAll(Arrays.asList(rest.split(",")));
				}
			}

			listener.onConnected(boardW, boardH, noteW, noteH, new ArrayList<>(colors));

			// Start reader thread to forward server messages
			readerThread = new Thread(() -> {
				try {
					String line;
					while ((line = in.readLine()) != null) {
						listener.onServerResponse(line);
					}
				} catch (IOException e) {
					// ignore
				} finally {
					listener.onDisconnected();
				}
			});
			readerThread.setDaemon(true);
			readerThread.start();

			return true;
		} catch (Exception e) {
			listener.onError(e.getMessage());
			return false;
		}
	}

	public void disconnect() {
		try {
			if (out != null) {
				out.println("DISCONNECT");
			}
			if (socket != null) socket.close();
		} catch (IOException ignored) {
		} finally {
			listener.onDisconnected();
		}
	}

	public List<String> getValidColors() {
		return new ArrayList<>(colors);
	}

	public void sendCommand(String cmd) {
		if (out != null) {
			out.println(cmd);
		} else {
			listener.onError("Not connected");
		}
	}
}

