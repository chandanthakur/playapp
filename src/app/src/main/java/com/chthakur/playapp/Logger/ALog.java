package com.chthakur.playapp.Logger;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.chthakur.playapp.App;
import com.chthakur.playapp.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ALog {

    public static final int VERBOSE = Log.VERBOSE;

    public static final int DEBUG = Log.DEBUG;

    public static final int INFO = Log.INFO;

    public static final int WARN = Log.WARN;

    public static final int ERROR = Log.ERROR;

    public static final int ASSERT = Log.ASSERT;

    private static LoggingDelegate sHandler = ALogDefaultLoggingDelegate.getInstance();

    /**
     * Sets the logging delegate that overrides the default delegate.
     *
     * @param delegate the delegate to use
     */
    public static void setLoggingDelegate(LoggingDelegate delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException();
        }
        sHandler = delegate;
    }

    public static boolean isLoggable(int level) {
        return sHandler.isLoggable(level);
    }

    public static void setMinimumLoggingLevel(int level) {
        sHandler.setMinimumLoggingLevel(level);
    }

    public static int getMinimumLoggingLevel() {
        return sHandler.getMinimumLoggingLevel();
    }

    public static void v(String tag, String msg) {
        if (sHandler.isLoggable(VERBOSE)) {
            sHandler.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (sHandler.isLoggable(VERBOSE)) {
            sHandler.v(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (sHandler.isLoggable(DEBUG)) {
            sHandler.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (sHandler.isLoggable(DEBUG)) {
            sHandler.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (sHandler.isLoggable(INFO)) {
            sHandler.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (sHandler.isLoggable(INFO)) {
            sHandler.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        if (sHandler.isLoggable(WARN)) {
            sHandler.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (sHandler.isLoggable(WARN)) {
            sHandler.w(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (sHandler.isLoggable(ERROR)) {
            sHandler.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (sHandler.isLoggable(ERROR)) {
            sHandler.e(tag, msg, tr);
        }
    }

    /**
     * Logger configuration interface
     *
     */
    public static class LoggerConfiguration {
        private static final String TAG = "LoggerConfiguration";
        private static FileHandler fileHandler;
        private static final String APP_LOG_PREFIX = BuildConfig.APPLICATION_ID;
        private static final String APP_LOG_PATTERN = APP_LOG_PREFIX + ".%g.log";
        private static final int LOG_FILE_SIZE = 2 * 1024 * 1024;
        private static final int LOGS_COUNT = 2;

        public static void initializeLogger() {
            initializeLoggerImpl();
        }

        /**
         * Android provides two different ways for logging. Once through Log.* functions & other via Logger.getLogger(<LoggerName>)
         * Logger.getLogger() API has a benefit over Log.* that we can redirect the logs to different channels(For example file) as we desire
         * Code which uses Logger.getLogger() api, doesn't need any change to redirect logs to file. Just that we need to register
         * FileHander which will write logs to file as well.
         * Code which uses the Log.* api will need change, we have defined a wrapper ALog.* functions with exactly same API as Log.*
         * This ALog class will internally use Logger.getLogger() functionality instead of Log.* functionality and hence we will be
         * able to write to file using FileHandler provided by Android.
         */
        private static void initializeLoggerImpl() {
            File logsDir;
            // Strict mode in DEBUG throws exceptions.
            // This is essentially an IO operation being run on UI thread. We do not have much choice
            // to circumvent it, we need to run this onCreate to instantiate logging to file. This takes less than 10ms on 10K device.
            StrictMode.ThreadPolicy originalThreadPolicy = null;
            if(BuildConfig.DEBUG) {
                originalThreadPolicy = StrictMode.getThreadPolicy();
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(originalThreadPolicy)
                        .permitDiskReads()
                        .build());
            }

            final int minLogPriority = BuildConfig.DEBUG ? Log.VERBOSE : Log.INFO;
            File filePattern = new File(getAppLogsPath(App.getContext()), APP_LOG_PATTERN);
            LoggerConfiguration.configureLogger(filePattern, minLogPriority, LOG_FILE_SIZE, LOGS_COUNT);

            if(BuildConfig.DEBUG) {
                if(originalThreadPolicy != null) {
                    StrictMode.setThreadPolicy(originalThreadPolicy);
                }
            }
        }

        /**
         * @param file file to write logs to
         * @param minLogPriority minimum log level would be written to file
         * @param maxFileSize maximum file size
         * @param countFiles maximum count of files stored
         */
        private synchronized static void configureLogger(File file, int minLogPriority, int maxFileSize, int countFiles) {
            setMinimumLoggingLevel(minLogPriority);
            if (fileHandler != null) {
                return;
            }

            try {
                fileHandler = new FileHandler(file.getAbsolutePath(), maxFileSize, countFiles, true);
                fileHandler.setFormatter(new LogFormatter());
                final Logger parentLogger = Logger.getAnonymousLogger().getParent();
                parentLogger.addHandler(fileHandler);
                resetLevelForLoggingHandlers(minLogPriority);
            } catch (IOException e) {
                Log.e(TAG, "logging initialization failed", e);
            }
        }

        private static File getAppLogsPath(Context context) {
            return context.getExternalCacheDir();
        }

        /**
         * we want to make sure the registered handlers also follow the logLevel set by the app.
         * @param minLogPriority the minimum logPriority to set for already existing handlers
         */
        private static void resetLevelForLoggingHandlers(int minLogPriority) {
            Level logLevel = ALogDefaultLoggingDelegate.getLevelFromPriority(minLogPriority);
            final Logger parentLogger = Logger.getAnonymousLogger().getParent();
            final Handler[] handlers = parentLogger.getHandlers();
            for(Handler loggerHandler: handlers) {
                loggerHandler.setLevel(logLevel);
            }
        }
    }
}