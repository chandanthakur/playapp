package com.chthakur.playapp.Logger;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ALogDefaultLoggingDelegate implements LoggingDelegate {

    public static final ALogDefaultLoggingDelegate sInstance = new ALogDefaultLoggingDelegate();

    private String mApplicationTag = null;

    private int mMinimumLoggingLevel = Log.WARN;

    private static final String DEFAULT_TAG = "DEFAULT";

    // chthakur - we need to keep strong reference to loggers, otherwise those will be collected
    // as per Logger.java
    private static final ConcurrentHashMap<String, Logger> loggersMap= new ConcurrentHashMap<String, Logger>();

    public static ALogDefaultLoggingDelegate getInstance() {
        return sInstance;
    }

    private ALogDefaultLoggingDelegate() {
    }

    /**
     * Sets an application tag that is used for checking if a log line is loggable and also
     * to prefix to all log lines.
     *
     * @param tag the tag
     */
    public void setApplicationTag(String tag) {
        mApplicationTag = tag;
    }


    @Override
    public void setMinimumLoggingLevel(int level) {
        mMinimumLoggingLevel = level;
    }

    @Override
    public int getMinimumLoggingLevel() {
        return mMinimumLoggingLevel;
    }

    @Override
    public boolean isLoggable(int level) {
        return mMinimumLoggingLevel <= level;
    }

    @Override
    public void v(String tag, String msg) {
        println(Log.VERBOSE, tag, msg);
    }

    @Override
    public void v(String tag, String msg, Throwable tr) {
        println(Log.VERBOSE, tag, msg, tr);
    }

    @Override
    public void d(String tag, String msg) {
        println(Log.DEBUG, tag, msg);
    }

    @Override
    public void d(String tag, String msg, Throwable tr) {
        println(Log.DEBUG, tag, msg, tr);
    }

    @Override
    public void i(String tag, String msg) {
        println(Log.INFO, tag, msg);
    }

    @Override
    public void i(String tag, String msg, Throwable tr) {
        println(Log.INFO, tag, msg, tr);
    }

    @Override
    public void w(String tag, String msg) {
        println(Log.WARN, tag, msg);
    }

    @Override
    public void w(String tag, String msg, Throwable tr) {
        println(Log.WARN, tag, msg, tr);
    }

    @Override
    public void e(String tag, String msg) {
        println(Log.ERROR, tag, msg);
    }

    @Override
    public void e(String tag, String msg, Throwable tr) {
        println(Log.ERROR, tag, msg, tr);
    }

    /**
     * Forwarding to Log.ERROR since we do not want to crash the app with WTF
     */
    @Override
    public void wtf(String tag, String msg) {
        println(Log.ERROR, tag, msg);
    }

    /**
     * Forwarding to Log.ERROR since we do not want to crash the app with WTF
     */
    @Override
    public void wtf(String tag, String msg, Throwable tr) {
        println(Log.ERROR, tag, msg, tr);
    }

    @Override
    public void log(int priority, String tag, String msg) {
        println(priority, tag, msg);
    }

    private void println(int priority, String tag, String msg) {
        String normTag = prefixTag(tag);
        Logger logger = getLogger(normTag);
        logger.log(getLevelFromPriority(priority), msg);
        // This is mostly required in debug only.
        // The logger by default doesn't log below INFO.
        // Changing that settings require per tag level setting which is quite tedious. Using this workaround for the same
        if(priority < Log.INFO) {
           logCatPrintLn(priority, normTag, msg);
        }
    }

    private void println(int priority, String tag, String msg, Throwable tr) {
        String normTag = prefixTag(tag);
        Logger logger = getLogger(normTag);
        logger.log(getLevelFromPriority(priority), msg, tr);
        if(priority < Log.INFO) {
            logCatPrintLn(priority, normTag, msg, tr);
        }
    }

    private void logCatPrintLn(int priority, String tag, String msg) {
        String normTag = loggerNameToTag(tag);
        Log.println(priority, normTag, msg);
    }

    private void logCatPrintLn(int priority, String tag, String msg, Throwable tr) {
        String normTag = loggerNameToTag(tag);
        switch (priority) {
            case Log.ERROR:
            case Log.ASSERT:
                Log.e(normTag, msg, tr);
                break;
            case Log.WARN:
                Log.w(normTag, msg, tr);
                break;
            case Log.INFO:
                Log.i(normTag, msg, tr);
                break;
            case Log.DEBUG:
                Log.d(normTag, msg, tr);
                break;
            case Log.VERBOSE:
            default:
                Log.v(normTag, msg, tr);
        }
    }

    // Directly copied from DalvikLogging.loggerNameToTag
    // Absolutely no change, some constants can be improved but keeping it original
    public static String loggerNameToTag(String loggerName) {
        if (loggerName == null) {
            return DEFAULT_TAG;
        }

        int length = loggerName.length();
        if (length <= 23) {
            return loggerName;
        }

        int lastPeriod = loggerName.lastIndexOf(".");
        return length - (lastPeriod + 1) <= 23
                ? loggerName.substring(lastPeriod + 1)
                : loggerName.substring(loggerName.length() - 23);
    }

    /**
     * Return the logger available in hashmap. Ideally, getting the Logger using Logger.getLogger
     * should be the same thing. But Android might collect the logger if we do not have a strong
     * reference to it. So we keep a hashmap with all the logger instances and use it from there
     * itself.
     * @param tag
     * @return Logger for that particular tag
     */
    private Logger getLogger(String tag) {
        String normalizedTag = tag;
        if (normalizedTag == null) {
            normalizedTag = DEFAULT_TAG;
        }

        Logger logger = loggersMap.get(normalizedTag);
        if (logger == null) {
            logger = Logger.getLogger(normalizedTag);
            logger.setLevel(getLevelFromPriority(mMinimumLoggingLevel));

            Logger existingLogger = loggersMap.putIfAbsent(normalizedTag, logger);
            if(existingLogger != null) {
                logger = existingLogger;
            }
        }

        return logger;
    }

    /**
     * We have two kind of logging priorities floating around. One with Log.* and other with Logger.
     * This function helps cover the Log.* priority to Logger Level.
     * @param levelIndex
     * @return Level in Logger
     */
    static public Level getLevelFromPriority(int levelIndex) {
        Level level;
        switch (levelIndex) {
            case Log.VERBOSE:
                level = Level.FINEST;
                break;
            case Log.DEBUG:
                level = Level.FINE;
                break;
            case Log.INFO:
                level = Level.INFO;
                break;
            case Log.WARN:
                level = Level.WARNING;
                break;
            case Log.ERROR:
            case Log.ASSERT:
                level = Level.SEVERE;
                break;
            default:
                level = Level.FINEST;
        }

        return level;
    }

    private String prefixTag(String tag) {
        tag = java.lang.Thread.currentThread().getName() + ":" + tag;
        if (mApplicationTag != null) {
            return mApplicationTag + ":" + tag;
        } else {
            return tag;
        }
    }

    private static String getMsg(String msg, Throwable tr) {
        return msg + '\n' + getStackTraceString(tr);
    }

    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }
}