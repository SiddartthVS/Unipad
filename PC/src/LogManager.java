import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*`LogManager.java
Centralized logging system that provides thread-safe logging to both console and file.
    - File creation
    - File opening
    - File rotation
    - Thread-safe logging
*/
public class LogManager {
    private static final String LOG_FILE_NAME = "uniPad.log";
    private static final String LOG_DIRECTORY = System.getProperty("user.home") + File.separator + ".uni";
    private static final String LOG_FILE_PATH = LOG_DIRECTORY + File.separator + LOG_FILE_NAME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM HH:mm");
    private static final int MAX_LOG_SIZE = 1 * 1024 * 1024;
    private static BufferedWriter writer;
    private static boolean initialized = false;

    private static LogManager instance;

    LogManager() {
        initialize();
    }

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    // helpers
    private synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Directory creation/opening
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                logDir.mkdirs();
                System.out.println("Created log directory: " + LOG_DIRECTORY);
            }

            // File rotation
            File logFile = new File(LOG_FILE_PATH);
            if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
                rotateLogFile();
            }

            // Initialization
            writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true));
            initialized = true;

        } catch (IOException e) {
            System.err.println("Failed to initialize LogManager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void rotateLogFile() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            File backupFile = new File(LOG_FILE_PATH + ".old");

            // Delete old backup if exists
            if (backupFile.exists()) {
                backupFile.delete();
            }

            // Rename current log to backup
            logFile.renameTo(backupFile);
            System.out.println("Log file rotated: " + backupFile.getName());

        } catch (Exception e) {
            System.err.println("Failed to rotate log file: " + e.getMessage());
        }
    }

    // log types
    public static void log(String message) {
        getInstance();

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] %s", timestamp, message);

        System.out.println(logEntry);

        synchronized (LogManager.class) {
            try {
                if (writer != null) {
                    writer.write(logEntry);
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        }
    }

    public static void error(String message) {
        log("ERROR: " + message);
        AppCopy.notify("ERROR: " + message, "ERROR");
    }

    public static void error(String message, Exception e) {
        log("ERROR: " + message + " - " + e.getMessage());

        synchronized (LogManager.class) {
            try {
                if (writer != null) {
                    e.printStackTrace(new PrintWriter(writer));
                    writer.flush();
                }
            } catch (IOException ioException) {
                System.err.println("Failed to write exception to log: " + ioException.getMessage());
            }
        }
    }

    public static void warning(String message) {
        log("WARNING: " + message);
        AppCopy.notify("WARNING: " + message, "WARNING");
    }

    public static void info(String message) {
        log("INFO: " + message);
        AppCopy.notify("INFO: " + message, "INFO");
    }

    public static void debug(String message) {
        log("DEBUG: " + message);
        AppCopy.notify("DEBUG: " + message, "WARNING");
    }

    public static void copy(String message) {
        log("COPIED: " + message);
        AppCopy.notify("Sent: " + message, "INFO");
    }

    public static void paste(String message) {
        log("PASTED: " + message);
        AppCopy.notify("Received: " + message, "INFO");
    }

    // log window
    public static List<String> readLogs() {
        List<String> logs = new ArrayList<>();

        try {
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                logs.add("No log file found. Logs will be created when the application runs.");
                return logs;
            }

            // Read all lines from the log file
            logs = Files.readAllLines(Paths.get(LOG_FILE_PATH));

            if (logs.isEmpty()) {
                logs.add("Log file is empty.");
            }

        } catch (IOException e) {
            logs.add("Error reading log file: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    public static String getLogFilePath() {
        return LOG_FILE_PATH;
    }

    public static synchronized void clearLogs() {
        try {
            if (writer != null) {
                writer.close();
            }

            // Delete the log file
            File logFile = new File(LOG_FILE_PATH);
            if (logFile.exists()) {
                logFile.delete();
            }

            // Reinitialize
            initialized = false;
            getInstance().initialize();

            // log("Logs cleared by user");

        } catch (IOException e) {
            System.err.println("Failed to clear logs: " + e.getMessage());
        }
    }

    public static synchronized void close() {
        try {
            if (writer != null) {
                log("=".repeat(80));
                writer.close();
                writer = null;
                initialized = false;
            }
        } catch (IOException e) {
            System.err.println("Failed to close log file: " + e.getMessage());
        }
    }

    public static String getLogFileSize() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                return "0 bytes";
            }

            long bytes = logFile.length();

            if (bytes < 1024) {
                return bytes + " bytes";
            } else if (bytes < 1024 * 1024) {
                return String.format("%.2f KB", bytes / 1024.0);
            } else {
                return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
            }

        } catch (Exception e) {
            return "Unknown";
        }
    }
}
