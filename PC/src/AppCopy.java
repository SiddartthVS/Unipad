import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

/*`AppCopy.java
This is the main class of the application that creates and handles:
    - SystemTray
    - Server
    - HotKey
    - Clipboard
    - UI
    - Logs
    - Notifications

*/
public class AppCopy {
    // Variables
    private static int port = 8080;
    private static boolean isEnabled = false;
    private static Image icon;
    private static TrayIcon trayIcon;

    // Instances
    private static Clipboard clipboard = new Clipboard();
    private static Connection server = new Connection(port, clipboard);
    private static HotKey hotKey = new HotKey(server, clipboard);

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {

        // 1 - System Tray
        if (!SystemTray.isSupported()) {
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        try {
            icon = ImageIO.read(new File("icon.png"));
        } catch (Exception e) {
            icon = createDefaultIcon();
        }
        trayIcon = new TrayIcon(icon, "UniPad");
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            return;
        }

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    show(e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    show(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    show(e.getX(), e.getY());
                }
            }
        });

        // 2 - Server
        server.start();

        // 3 - Logs
        LogManager.log("========== SESSION STARTED ==========");

    }

    // Notification
    public static void notify(String message, String type) {
        if (trayIcon == null) {
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        trayIcon.displayMessage(
                "UniPad",
                message,
                TrayIcon.MessageType.valueOf(type));
    }

    private static Image createDefaultIcon() {
        BufferedImage defaultIcon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = defaultIcon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setFont(new Font("Arial", Font.BOLD, 38));

        g2d.setPaint(Color.BLACK);
        g2d.fillRoundRect(5, 5, 22, 25, 10, 10);

        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawRoundRect(5, 5, 22, 25, 10, 10);
        g2d.drawString(".", 11, 6);

        g2d.setPaint(Color.WHITE);

        g2d.drawString("-", 9, 21);
        g2d.drawString("-", 9, 21 + 8);

        g2d.dispose();
        return defaultIcon;
    }

    private static void show(int x, int y) {
        JPopupMenu popup = menu();

        JFrame hiddenFrame = new JFrame();
        hiddenFrame.setIconImage(icon);
        hiddenFrame.setUndecorated(true);
        hiddenFrame.setSize(0, 0);
        hiddenFrame.setVisible(true);
        popup.show(hiddenFrame, x - 100, y - 300);

        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                hiddenFrame.dispose();
            }

            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                hiddenFrame.dispose();
            }
        });
    }

    private static JPopupMenu menu() {
        JPopupMenu popup = new JPopupMenu();

        Color bgColor = new Color(45, 45, 45);
        Color textColor = new Color(255, 255, 255);
        Color hoverColor = new Color(60, 60, 60);

        popup.setBackground(bgColor);
        popup.setBorder(BorderFactory.createLineBorder(hoverColor, 1));

        popup.add(item("IP: " + IP.getAddress(), "🌐", textColor, bgColor, hoverColor, false, null));
        popup.add(line(hoverColor, bgColor));
        popup.add(item("Key: CTRL+ALT+Insert", "⌨️", textColor, bgColor, hoverColor, false, null));
        popup.add(line(hoverColor, bgColor));
        popup.add(item("Logs", "📋", textColor, bgColor, hoverColor, true, (e) -> {
            LogWindow.showLogs();
        }));
        popup.add(line(hoverColor, bgColor));
        popup.add(item(isEnabled ? "Disable" : "Enable", "💡", textColor, bgColor, hoverColor, true, (e) -> {
            if (!isEnabled) {
                hotKey.start();
                isEnabled = true;
            } else {
                hotKey.stop();
                isEnabled = false;
            }
        }));

        popup.add(line(hoverColor, bgColor));
        popup.add(item("Exit", "❌", textColor, bgColor, hoverColor, false,
                (e) -> {
                    // LogManager.log("Exit requested by user");
                    LogManager.close();
                    System.exit(0);
                }));

        return popup;
    }

    private static JSeparator line(Color separatorColor, Color bgColor) {
        JSeparator separator = new JSeparator();
        separator.setForeground(separatorColor);
        separator.setBackground(bgColor);
        separator.setPreferredSize(new Dimension(280, 1));
        return separator;
    }

    private static JMenuItem item(String text, String icon, Color textColor,
            Color bgColor, Color hoverColor, boolean isLineBreak, ActionListener action) {
        JMenuItem item = new JMenuItem() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (getModel().isArmed() || getModel().isPressed()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(textColor);
                try {
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                } catch (Exception e) {
                    g2.setFont(new Font("Dialog", Font.PLAIN, 14));
                }
                g2.drawString(icon, 10, 22);
                if (isLineBreak) {
                    g2.setColor(hoverColor);
                    g2.drawLine(40, 2, 40, 32);
                    g2.setColor(textColor);
                    g2.drawString(text, 50, 22);
                } else {
                    g2.drawString(text, 40, 22);
                }

                g2.dispose();
            }
        };

        item.setText(text);
        item.setPreferredSize(new Dimension(280, 35));
        item.setBackground(bgColor);
        item.setForeground(textColor);
        item.setBorder(new EmptyBorder(5, 10, 5, 10));
        item.setOpaque(true);
        item.setFocusPainted(false);
        item.setBorderPainted(false);

        if (action != null) {
            item.addActionListener(action);
        }

        // Hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(hoverColor);
                item.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(bgColor);
                item.repaint();
            }
        });

        return item;
    }

}
