import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

/*HotKey.java
Handles hotkey registration and clipboard copying
    - start()
    - stop()
    - run()
*/

public class HotKey implements Runnable {

    private Thread thread;
    private boolean running = false;
    private Connection server;
    private Clipboard clipboard;

    HotKey(Connection server, Clipboard clipboard) {
        this.server = server;
        this.clipboard = clipboard;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        running = false;
        User32.INSTANCE.UnregisterHotKey(null, 1);
    }

    @Override
    public void run() {
        boolean registered = User32.INSTANCE.RegisterHotKey(
                null,
                1,
                0x0002 | 0x0001, // CTRL + ALT
                0x2D); // INSERT key

        if (!registered) {
            LogManager.error("Failed to register hotkey - Key combination may be in use by another application");
            return;
        }

        WinUser.MSG msg = new WinUser.MSG();

        while (running && User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
            if (msg.message == WinUser.WM_HOTKEY) {
                if (msg.wParam.intValue() == 1) { // Clicked CTRL+ALT+INSERT
                    try {
                        String clipboardContent = clipboard.getClipboard();
                        if (clipboardContent != null && !clipboardContent.isEmpty()) {
                            server.onCopy(clipboardContent);
                            LogManager.copy(
                                    (clipboardContent.length() > 50 ? clipboardContent.substring(0, 50) + "..."
                                            : clipboardContent));
                        } else {
                            LogManager.warning(
                                    "Nothing copied - Press CTRL+C to copy something before pressing CTRL+ALT+INSERT");
                        }
                    } catch (Exception e) {
                        LogManager.error("Error processing hotkey press", e);
                    }
                }
            }
        }

        User32.INSTANCE.UnregisterHotKey(null, 1);
    }

}
