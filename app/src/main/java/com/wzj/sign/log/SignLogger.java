package com.wzj.sign.log;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class SignLogger {

    private static volatile SignLogger INSTANCE;
    private final StringBuilder logBuffer;
    private final List<LogEntry> entries;
    private final CopyOnWriteArrayList<OnLogListener> listeners;
    private final Handler mainHandler;
    private int maxBufferSize = 5000;
    private final Context appContext;

    public interface OnLogListener {
        void onNewLog(LogEntry entry);
    }

    private SignLogger(Context context) {
        this.appContext = context.getApplicationContext();
        logBuffer = new StringBuilder();
        entries = new ArrayList<>();
        listeners = new CopyOnWriteArrayList<>();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static SignLogger getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SignLogger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SignLogger(context);
                }
            }
        }
        return INSTANCE;
    }

    public void addListener(OnLogListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OnLogListener listener) {
        listeners.remove(listener);
    }

    public void debug(String tag, String message) {
        log(new LogEntry(LogEntry.Level.DEBUG, "[" + tag + "] " + message));
        android.util.Log.d(tag, message);
    }

    public void info(String tag, String message) {
        log(new LogEntry(LogEntry.Level.INFO, "[" + tag + "] " + message));
        android.util.Log.i(tag, message);
    }

    public void warn(String tag, String message) {
        log(new LogEntry(LogEntry.Level.WARNING, "[" + tag + "] " + message));
        android.util.Log.w(tag, message);
    }

    public void error(String tag, String message) {
        log(new LogEntry(LogEntry.Level.ERROR, "[" + tag + "] " + message));
        android.util.Log.e(tag, message);
    }

    public void error(String tag, String message, Throwable throwable) {
        log(new LogEntry(LogEntry.Level.ERROR, "[" + tag + "] " + message, throwable));
        android.util.Log.e(tag, message, throwable);
    }

    private void log(LogEntry entry) {
        synchronized (logBuffer) {
            entries.add(entry);
            String formatted = entry.toString() + "\n";
            logBuffer.insert(0, formatted);
            if (logBuffer.length() > maxBufferSize) {
                logBuffer.setLength(maxBufferSize);
            }
        }

        mainHandler.post(() -> {
            for (OnLogListener listener : listeners) {
                listener.onNewLog(entry);
            }
        });
    }

    public String getFormattedLog() {
        synchronized (logBuffer) {
            return logBuffer.toString();
        }
    }

    public List<LogEntry> getEntries() {
        synchronized (logBuffer) {
            return new ArrayList<>(entries);
        }
    }

    public void clear() {
        synchronized (logBuffer) {
            logBuffer.setLength(0);
            entries.clear();
        }
        mainHandler.post(() -> {
            for (OnLogListener listener : listeners) {
                listener.onNewLog(new LogEntry(LogEntry.Level.INFO, "日志已清除"));
            }
        });
    }

    public String exportToFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "wzj_sign_log_" + sdf.format(new Date()) + ".txt";
        File logDir = new File(appContext.getExternalFilesDir(null), "logs");
        if (!logDir.exists()) logDir.mkdirs();

        File logFile = new File(logDir, fileName);
        try (FileWriter writer = new FileWriter(logFile)) {
            synchronized (logBuffer) {
                for (int i = entries.size() - 1; i >= 0; i--) {
                    writer.write(entries.get(i).toString() + "\n");
                }
            }
            return logFile.getAbsolutePath();
        } catch (IOException e) {
            error("SignLogger", "日志导出失败: " + e.getMessage());
            return null;
        }
    }

    public void setMaxBufferSize(int size) {
        this.maxBufferSize = size;
    }
}
