package com.wzj.sign.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogEntry {

    public enum Level {
        DEBUG, INFO, WARNING, ERROR
    }

    private final long timestamp;
    private final Level level;
    private final String message;
    private final String throwable;

    public LogEntry(Level level, String message) {
        this(level, message, null);
    }

    public LogEntry(Level level, String message, Throwable throwable) {
        this.timestamp = System.currentTimeMillis();
        this.level = level;
        this.message = message;
        this.throwable = throwable != null ? android.util.Log.getStackTraceString(throwable) : null;
    }

    public long getTimestamp() { return timestamp; }
    public Level getLevel() { return level; }
    public String getMessage() { return message; }
    public String getThrowable() { return throwable; }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getLevelTag() {
        switch (level) {
            case DEBUG: return "D";
            case INFO: return "I";
            case WARNING: return "W";
            case ERROR: return "E";
            default: return "?";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getFormattedTime()).append("] ");
        sb.append("[").append(getLevelTag()).append("] ");
        sb.append(message);
        if (throwable != null) {
            sb.append("\n").append(throwable);
        }
        return sb.toString();
    }
}
