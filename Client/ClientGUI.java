package Client;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClientGUI extends JFrame implements ServerConnection.ConnectionListener {
    private JTextField serverIpField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JTextArea outputArea;

    // POST fields
    private JTextField postXField;
    private JTextField postYField;
    private JComboBox<String> colorCombo;
    private JTextField messageField;
    private JButton postButton;

    // GET fields
    private JButton getButton;
    private JButton getPinsButton;

    // PIN/UNPIN fields
    private JTextField pinXField;
    private JTextField pinYField;
    private JButton pinButton;
    private JButton unpinButton;

    // SHAKE/CLEAR fields
    private JButton shakeButton;
    private JButton clearButton;

    private ServerConnection connection;
    private CommandHandler commandHandler;

    public ClientGUI() {
        setTitle("Bulletin Board Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        connection = new ServerConnection(this);
        commandHandler = new CommandHandler(connection);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Connection", createConnectionPanel());
        tabbedPane.addTab("POST", createPostPanel());
        tabbedPane.addTab("GET", createGetPanel());
        tabbedPane.addTab("PIN/UNPIN", createPinPanel());
        tabbedPane.addTab("SHAKE/CLEAR", createShakePanel());

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        outputPanel.add(new JLabel("Server Output:"), BorderLayout.NORTH);
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(outputPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        updateCommandButtonStates(false);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Server IP:"), gbc);
        gbc.gridx = 1;
        serverIpField = new JTextField("localhost", 15);
        panel.add(serverIpField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("4554", 15);
        panel.add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToServer());
        panel.add(connectButton, gbc);

        gbc.gridx = 1;
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> commandHandler.disconnect());
        panel.add(disconnectButton, gbc);

        return panel;
    }

    private JPanel createPostPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("X Coordinate:"), gbc);
        gbc.gridx = 1;
        postXField = new JTextField(10);
        panel.add(postXField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Y Coordinate:"), gbc);
        gbc.gridx = 1;
        postYField = new JTextField(10);
        panel.add(postYField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 1;
        colorCombo = new JComboBox<>();
        panel.add(colorCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Message:"), gbc);
        gbc.gridx = 1;
        messageField = new JTextField(30);
        panel.add(messageField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        postButton = new JButton("POST");
        postButton.addActionListener(e -> sendPost());
        panel.add(postButton, gbc);

        return panel;
    }

    private JPanel createGetPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        getPinsButton = new JButton("GET PINS");
        getPinsButton.addActionListener(e -> commandHandler.getPins());
        panel.add(getPinsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        getButton = new JButton("GET (with filters)");
        getButton.addActionListener(e -> showGetDialog());
        panel.add(getButton, gbc);

        return panel;
    }

    private JPanel createPinPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("X Coordinate:"), gbc);
        gbc.gridx = 1;
        pinXField = new JTextField(10);
        panel.add(pinXField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Y Coordinate:"), gbc);
        gbc.gridx = 1;
        pinYField = new JTextField(10);
        panel.add(pinYField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        pinButton = new JButton("PIN");
        pinButton.addActionListener(e -> sendPin());
        panel.add(pinButton, gbc);

        gbc.gridx = 1;
        unpinButton = new JButton("UNPIN");
        unpinButton.addActionListener(e -> sendUnpin());
        panel.add(unpinButton, gbc);

        return panel;
    }

    private JPanel createShakePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        shakeButton = new JButton("SHAKE");
        shakeButton.addActionListener(e -> commandHandler.shake());
        panel.add(shakeButton, gbc);

        gbc.gridx = 1;
        clearButton = new JButton("CLEAR");
        clearButton.addActionListener(e -> commandHandler.clear());
        panel.add(clearButton, gbc);

        return panel;
    }

    private void updateCommandButtonStates(boolean enabled) {
        disconnectButton.setEnabled(enabled);
        postButton.setEnabled(enabled);
        getButton.setEnabled(enabled);
        getPinsButton.setEnabled(enabled);
        pinButton.setEnabled(enabled);
        unpinButton.setEnabled(enabled);
        shakeButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        connectButton.setEnabled(!enabled);
    }

    private void connectToServer() {
        try {
            String ip = serverIpField.getText();
            int port = Integer.parseInt(portField.getText());

            if (connection.connect(ip, port)) {
                updateCommandButtonStates(true);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendPost() {
        try {
            String x = postXField.getText();
            String y = postYField.getText();
            String color = (String) colorCombo.getSelectedItem();
            String message = messageField.getText();

            if (x.isEmpty() || y.isEmpty() || message.isEmpty() || color == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int xCoord = Integer.parseInt(x);
            int yCoord = Integer.parseInt(y);
            commandHandler.post(xCoord, yCoord, color, message);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid coordinates: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showGetDialog() {
        JDialog dialog = new JDialog(this, "GET Command", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JCheckBox colorCheck = new JCheckBox("Filter by Color:");
        JComboBox<String> colorFilter = new JComboBox<>(connection.getValidColors().toArray(new String[0]));

        JCheckBox containsCheck = new JCheckBox("Filter by Contains (x, y):");
        JTextField containsX = new JTextField(5);
        JTextField containsY = new JTextField(5);

        JCheckBox refersCheck = new JCheckBox("Filter by RefersTo:");
        JTextField refersField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(colorCheck, gbc);
        gbc.gridx = 1;
        panel.add(colorFilter, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(containsCheck, gbc);
        gbc.gridx = 1;
        panel.add(new JLabel("X:"), gbc);
        gbc.gridx = 2;
        panel.add(containsX, gbc);
        gbc.gridx = 3;
        panel.add(new JLabel("Y:"), gbc);
        gbc.gridx = 4;
        panel.add(containsY, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(refersCheck, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 4;
        panel.add(refersField, gbc);

        JButton sendButton = new JButton("Send GET");
        sendButton.addActionListener(e -> {
            String filterColor = colorCheck.isSelected() ? (String) colorFilter.getSelectedItem() : null;
            Integer filterX = null;
            Integer filterY = null;
            if (containsCheck.isSelected()) {
                try {
                    if (!containsX.getText().isEmpty() && !containsY.getText().isEmpty()) {
                        filterX = Integer.parseInt(containsX.getText());
                        filterY = Integer.parseInt(containsY.getText());
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ClientGUI.this, "Invalid coordinates", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            String filterRefersTo = refersCheck.isSelected() ? refersField.getText() : null;

            commandHandler.get(filterColor, filterX, filterY, filterRefersTo);
            dialog.dispose();
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        panel.add(sendButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void sendPin() {
        try {
            String x = pinXField.getText();
            String y = pinYField.getText();

            if (x.isEmpty() || y.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in coordinates", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int xCoord = Integer.parseInt(x);
            int yCoord = Integer.parseInt(y);
            commandHandler.pin(xCoord, yCoord);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid coordinates: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendUnpin() {
        try {
            String x = pinXField.getText();
            String y = pinYField.getText();

            if (x.isEmpty() || y.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in coordinates", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int xCoord = Integer.parseInt(x);
            int yCoord = Integer.parseInt(y);
            commandHandler.unpin(xCoord, yCoord);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid coordinates: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onConnected(int boardW, int boardH, int noteW, int noteH, List<String> colors) {
        outputArea.append("Connected to server\n");
        colorCombo.removeAllItems();
        for (String color : colors) {
            colorCombo.addItem(color);
        }
    }

    @Override
    public void onDisconnected() {
        outputArea.append("Disconnected from server\n");
        updateCommandButtonStates(false);
    }

    @Override
    public void onError(String message) {
        outputArea.append("Error: " + message + "\n");
    }

    @Override
    public void onServerResponse(String response) {
        outputArea.append(response + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI());
    }
}
public class ClientGUI {
    
}
