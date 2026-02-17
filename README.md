# Unipad

> **Copy on PC. Appear on Phone. Instantly.**

Unipad is a real-time clipboard bridge between your **Windows PC** and your **Android phone** — no cables, no cloud, no account. Just your local Wi-Fi and a hotkey.

Press **CTRL+ALT+INSERT** on your PC and whatever you copied — text, image, anything — lands on your phone's clipboard in milliseconds.

---

## What It Does

| Action | How |
|---|---|
| Send PC clipboard to Phone | Press `CTRL+ALT+INSERT` on PC |
| Receive text on Phone | Tap **COPY** or enable **Auto Copy** |
| Send Phone clipboard to PC | Tap **PASTE** on the phone app |
| Send an image from PC | Works automatically — images are base64-encoded and decoded on the phone |

---

## How It Works

Unipad has two parts that talk to each other over **WebSocket** on your local Wi-Fi network:

```
[ PC - Java App ]  <----  WebSocket port 8080  ---->  [ Phone - React Native App ]
  System Tray                                              Expo Android
```

### PC Side — Java

The PC app runs silently in the **Windows system tray**. Click the icon and a dark-themed popup appears showing your local IP, an enable/disable toggle, a logs viewer, and an exit button.

- **AppCopy.java** — The entry point. Sets up the system tray icon, starts the WebSocket server, and wires everything together.
- **Connection.java** — A WebSocket server (powered by `java_websocket`) listening on port `8080`. Tracks the connected phone as `mobileClient` and handles send/receive.
- **Clipboard.java** — Reads your Windows clipboard and handles all three types: **plain text**, **images** (converted to base64 with a `~~` prefix), and **file images** dragged from Explorer. Also sets the clipboard when the phone sends text back.
- **HotKey.java** — Registers `CTRL+ALT+INSERT` using the **JNA Win32 API**. When pressed, it grabs the clipboard content and fires it to the connected phone over WebSocket. Runs in its own thread.
- **IP.java** — Scans your network interfaces and returns your local IPv4 address, shown right in the tray popup.
- **LogManager.java** — Thread-safe logging to `~/.uni/uniPad.log`. Supports log rotation at 1MB, multiple log levels (INFO, WARNING, ERROR, COPY, PASTE), and pushes tray notifications on errors.
- **LogWindow.java** — A dark-themed Swing window that displays your full log file, auto-refreshes every 5 seconds, and has Refresh, Clear, and Scroll-to-Bottom buttons.

### Phone Side — React Native (Expo)

The phone app is a clean, animated Android app with a dark UI.

- **App.js** — The heart of the app. Manages the WebSocket connection, clipboard operations, and all UI state. Auto-connects to your saved IP on launch.
  - Text received from PC → displayed below the COPY button; auto-copied if the toggle is on
  - Image received → decoded from base64, written to cache, set directly to Android clipboard
  - Floating toast notifications pop up in random screen zones (top, bottom, left, right edges) with smooth 250ms fade-in/out animations
  - The top section **crossfades between two looping background videos** based on connection state — one for connected, one for disconnected
- **Inputs.js** — The IP address input bar. Locked by default. Tap the pencil icon to edit, type your PC's IP, then tap the connect button. Locks again on blur.
- **MyButton.js** — Renders the COPY and PASTE buttons, each showing a truncated preview (15 chars) of the last text handled.
- **Toggle.js** — The Auto Copy switch. When on, every message received from the PC is instantly copied to the phone clipboard without any tap required.
- **Log.js** — An on-screen log panel that refreshes every 2 seconds, showing timestamped entries in alternating dark rows with a one-tap clear button.
- **LogManager.js** — Reads/writes a log file on-device (`clipboard_logs.txt`). Keeps the 7 most recent entries. Duplicate consecutive messages update the timestamp instead of creating a new line — no log spam.

---

## Project Structure

```
Unipad/
├── Mobile/                    # React Native (Expo) Android app
│   ├── App.js                 # Main app — WebSocket, clipboard, UI
│   ├── src/
│   │   ├── Inputs.js          # IP input + connect button
│   │   ├── MyButton.js        # COPY / PASTE buttons
│   │   ├── Toggle.js          # Auto Copy toggle
│   │   ├── Log.js             # On-screen log display
│   │   └── LogManager.js      # File-based log read/write
│   ├── assets/                # Icons, background videos, splash screen
│   ├── android/               # Android native build files
│   └── package.json
│
└── PC/                        # Java Windows tray application
    ├── src/
    │   ├── AppCopy.java        # Entry point — tray, server, hotkey
    │   ├── Connection.java     # WebSocket server
    │   ├── Clipboard.java      # Clipboard read/write (text + image)
    │   ├── HotKey.java         # CTRL+ALT+INSERT hotkey via JNA
    │   ├── IP.java             # Local IP detection
    │   ├── LogManager.java     # File logging with rotation
    │   └── LogWindow.java      # Dark-themed log viewer window
    ├── lib/                    # Java dependencies (JARs)
    └── build.bat               # Build script
```

---

## Getting Started

**Prerequisites**
- PC: Windows, Java 11+
- Phone: Android with Expo Go (or a built APK)
- Both devices on the **same Wi-Fi network**

**Run the PC App**
```bash
cd PC
build.bat
```
The app appears in your system tray. Click it to see your PC's IP address.

**Run the Phone App**
```bash
cd Mobile
npm install
npx expo start
```
Scan the QR code in Expo Go, enter your PC's IP in the app, and tap connect.

**Use It**
1. Copy anything on your PC with `CTRL+C`
2. Press `CTRL+ALT+INSERT`
3. It's on your phone's clipboard ✅

---

## Tech Stack

| Side | Technology |
|---|---|
| PC App | Java, Swing, Java-WebSocket, JNA (Win32 API) |
| Phone App | React Native, Expo, expo-clipboard, expo-av, expo-file-system |
| Protocol | WebSocket on port 8080 over local network |
| Image Transfer | Base64 encoding with `~~` prefix marker |

---

## License

© 2026 Siddartth V S — All rights reserved.
