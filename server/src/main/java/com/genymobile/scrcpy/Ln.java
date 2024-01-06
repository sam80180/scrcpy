package com.genymobile.scrcpy;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.Flattener;
import com.elvishew.xlog.flattener.Flattener2;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import org.json.JSONObject;

/**
 * Log both to Android logger (so that logs are visible in "adb logcat") and standard output/error (so that they are visible in the terminal
 * directly).
 */
public final class Ln {
    private static final String TAG = "scrcpy";
    private static final Printer androidPrinter = new AndroidPrinter(true);
    private static final LogConfiguration config = new LogConfiguration.Builder().tag(TAG).build();

    static {
        final Printer p = new ConsolePrinter(new MyFlattener());
        XLog.init(config, androidPrinter, p);
    }

    enum Level {
        ALL(LogLevel.ALL), VERBOSE(LogLevel.VERBOSE), DEBUG(LogLevel.DEBUG), INFO(LogLevel.INFO), WARN(LogLevel.WARN), ERROR(LogLevel.ERROR), NONE(LogLevel.NONE);

        private int xlog_level;

        private Level(int l) {
            this.xlog_level = l;
        }

        public int getXLogLevel() {
            return this.xlog_level;
        }
    }

    private Ln() {
        // not instantiable
    }

    public static void initLog(Options options) {
        final Printer p = new ConsolePrinter(new MyFlattener(options));
        XLog.init(config, androidPrinter, p);
    }

    public static void v(String message) {
        XLog.v(message);
    }

    public static void d(String message) {
        XLog.d(message);
    }

    public static void i(String message) {
        XLog.i(message);
    }

    public static void w(String message, Throwable throwable) {
        XLog.w(message, throwable);
    }

    public static void w(String message) {
        w(message, null);
    }

    public static void e(String message, Throwable throwable) {
        XLog.e(message, throwable);
    }

    public static void e(String message) {
        e(message, null);
    }

    private static class MyFlattener implements Flattener, Flattener2 {
        private Options options;

        public MyFlattener() {
            this(null);
        }

        public MyFlattener(Options o) {
            this.options = o;
        }

        @Override
        public CharSequence flatten(int logLevel, String tag, String message) {
            return this.flatten(System.currentTimeMillis(), logLevel, tag, message);
        }

        @Override
        public CharSequence flatten(long timeMillis, int logLevel, String tag, String message) {
            final String strTime = Long.toString(timeMillis);
            final String strLogLevel = LogLevel.getLevelName(logLevel);
            if (this.options!=null && this.options.getJsonLog()) {
                try {
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put("time", strTime);
                    jsonObject.put("level", strLogLevel);
                    jsonObject.put("tag", tag);
                    jsonObject.put("message", message);
                    return jsonObject.toString();
                } catch (Exception e) {}
            }
            return String.format("[%s] %s: %s", tag, strLogLevel, message);
        }
    }
}
