import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;

/*Connection.java
WebSocket connection handling with default java_websocket library methods
    - onStart()
    - onOpen()
    - onMessage()
    - onClose()
    - onError()
*/

public class Connection extends WebSocketServer {

    private WebSocket mobileClient;
    private Clipboard clipboard;

    public Connection(int port, Clipboard clipboard) {
        super(new InetSocketAddress(port));
        this.clipboard = clipboard;
    }

    @Override
    public void onStart() {
        LogManager.log("Trying to connect...");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        mobileClient = conn;
        String clientAddress = conn.getRemoteSocketAddress().toString();
        LogManager.log("Connected with IP:" + clientAddress);
        AppCopy.notify("Connected with IP:" + clientAddress, "INFO");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            clipboard.setClipboard(message);
        } catch (Exception e) {
            LogManager.error("Error copying: " + e.getMessage(), e);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn == mobileClient) {
            mobileClient = null;
        }
        LogManager.log("Disconnected");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LogManager.error("WebSocket error occurred", ex);
    }

    public void onCopy(String text) {
        if (mobileClient != null && mobileClient.isOpen()) {
            try {
                mobileClient.send(text);
            } catch (Exception e) {
                LogManager.error("Failed to paste", e);
                AppCopy.notify("Failed to paste", "ERROR");
            }
        } else {
            LogManager.warning("No device connected");
            AppCopy.notify("No device connected", "WARNING");
        }
    }
}
