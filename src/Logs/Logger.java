package Logs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {

    private static final String LOG_FILE = "world.log";
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024; // 3 MB in bytes
    private static final Logger INSTANCE = new Logger();

    private BufferedWriter bufferedWriter;

    private Logger() {
        initializeWriter(true); // Start by appending
    }

    // Helper to (re)initialize the writer
    private void initializeWriter(boolean append) {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            bufferedWriter = new BufferedWriter(new FileWriter(LOG_FILE, append));
        } catch (IOException e) {
            System.out.println("Unable to initiate writer: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Logger get() {
        return INSTANCE;
    }

    public synchronized void log(LogType logType, String msg) {
        // 1. Check file size before writing
        File file = new File(LOG_FILE);
        if (file.exists() && file.length() >= MAX_FILE_SIZE) {
            rotateLog();
        }

        String line = "[" + LocalDateTime.now() + "] [" + logType + "] [" + msg + "]";

        try {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Unable to write in runtime: " + this.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    private void rotateLog() {
        // Strategy: Clear the file and start fresh
        // You could also rename 'world.log' to 'world.log.old' here if you wanted a backup
        initializeWriter(false); // Passing 'false' overwrites/clears the file
    }

    public void info(String msg) { log(LogType.INFO, msg); }
    public void warn(String msg) { log(LogType.WARN, msg); }
    public void error(String msg) { log(LogType.ERROR, msg); }
}