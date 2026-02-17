import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/*`LogWindow.java
A sub-window that displays application logs    - File creation
    - Auto-refresh
    - Clear logs
    - Scroll to bottom
*/
public class LogWindow extends JFrame {
    private JTextArea logTextArea;
    private JScrollPane scrollPane;
    private JButton refreshButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private Timer autoRefreshTimer;

    // Dark theme colors
    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color BUTTON_BG = new Color(50, 50, 50);
    private static final Color BUTTON_HOVER = new Color(70, 70, 70);
    private static final Color BORDER_COLOR = new Color(60, 60, 60);

    public LogWindow() {
        initializeUI();
        loadLogs();
        startAutoRefresh();
    }

    private void initializeUI() {
        // Window settings
        setTitle("UNi - Logs");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Set icon if available
        try {
            Image icon = Toolkit.getDefaultToolkit().createImage("icon.png");
            if (icon != null) {
                setIconImage(icon);
            }
        } catch (Exception e) {
            // Ignore icon loading errors
        }

        // Main panel with dark theme
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Log text area with scroll pane
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(BG_COLOR);
        logTextArea.setForeground(TEXT_COLOR);
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with buttons and status
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add window listener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopAutoRefresh();
            }
        });
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JLabel titleLabel = new JLabel("Logs");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.WEST);

        JLabel pathLabel = new JLabel("Location: " + LogManager.getLogFilePath());
        pathLabel.setForeground(new Color(150, 150, 150));
        pathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        panel.add(pathLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BG_COLOR);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(BG_COLOR);

        // Refresh
        refreshButton = createStyledButton("Refresh", e -> {
            loadLogs();
            scrollToBottom();
        });
        buttonPanel.add(refreshButton);

        // Clear
        clearButton = createStyledButton("Clear Logs", e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to clear all logs?",
                    "Clear Logs",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                LogManager.clearLogs();
                loadLogs();
            }
        });
        buttonPanel.add(clearButton);

        // Scroll to bottom
        JButton scrollButton = createStyledButton("Scroll to Bottom", e -> scrollToBottom());
        buttonPanel.add(scrollButton);

        panel.add(buttonPanel, BorderLayout.WEST);

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(100, 200, 100));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private JButton createStyledButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_BG);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 15, 8, 15));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_BG);
            }
        });

        if (action != null) {
            button.addActionListener(action);
        }

        return button;
    }

    private void loadLogs() {
        SwingUtilities.invokeLater(() -> {
            List<String> logs = LogManager.readLogs();
            StringBuilder logContent = new StringBuilder();

            for (String log : logs) {
                logContent.append(log).append("\n");
            }

            logTextArea.setText(logContent.toString());

            // Update status
            String fileSize = LogManager.getLogFileSize();
            int lineCount = logs.size();
            showStatus(String.format("Loaded %d lines (%s)", lineCount, fileSize));
        });
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    private void showStatus(String message) {
        statusLabel.setText(message);

        // Clear status after 3 seconds
        Timer timer = new Timer(3000, e -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    private void startAutoRefresh() {
        // Auto-refresh every 5 seconds
        autoRefreshTimer = new Timer(5000, e -> loadLogs());
        autoRefreshTimer.start();
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
    }

    public void showWindow() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    public static void showLogs() {
        SwingUtilities.invokeLater(() -> {
            LogWindow logWindow = new LogWindow();
            logWindow.showWindow();
        });
    }
}
