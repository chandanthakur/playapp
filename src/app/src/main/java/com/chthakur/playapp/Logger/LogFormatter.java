package com.chthakur.playapp.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Created by chthakur on 7/14/2017.
 * Performance sensitive code.
 * SimpleDateFormat might crib under lot of threads (may be > 30).
 * With Java8 move to DateTimeFormatter. Some part of this code is coming from S4L as well.
 * Kind of variation of SimpleFormatter in Android. We need bit additional details
 */

public class LogFormatter extends java.util.logging.Formatter {

    private static final String DateFormat = "dd/MM/yyyy HH:mm:ss:SSS";
    private static final String EOL = System.getProperty("line.separator");

    public LogFormatter() {
    }

    private String metaPrefix(LogRecord record) {
        StringBuilder result = new StringBuilder(64);
        result.append(getDateInFormat(record.getMillis()));
        result.append(' ')
                .append(getLevelChar(record.getLevel()))
                .append(' ')
                .append(record.getLoggerName())
                .append(": ");

        return result.toString();
    }

    private static String getDateInFormat(long timeStamp) {
        try{
            Date createdDate = new Date(timeStamp);
            SimpleDateFormat format = new SimpleDateFormat(DateFormat, Locale.US);
            return format.format(createdDate);
        } catch (Exception e) {
            return String.valueOf(timeStamp);
        }
    }

    private char getLevelChar(Level level) {
        int levelValue = level.intValue();

        if (levelValue == Level.SEVERE.intValue()) {
            return 'E';
        } else if (levelValue == Level.WARNING.intValue()) {
            return 'W';
        } if (levelValue == Level.CONFIG.intValue() || levelValue == Level.INFO.intValue()) {
            return 'I';
        } else if (levelValue == Level.FINE.intValue()) {
            return 'D';
        } else {
            return 'V';
        }
    }

    @Override
    public synchronized String format(LogRecord logRecord) {
        StringBuilder result = new StringBuilder(512);
        String prefix = metaPrefix(logRecord);
        result.append(prefix)
                .append(logRecord.getMessage())
                .append(EOL);

        Throwable throwable = logRecord.getThrown();
        if (throwable != null) {
            result.append(prefix).append(throwable.getMessage());
            result.append(EOL);
            for (StackTraceElement stackFrame : throwable.getStackTrace()) {
                result.append(prefix)
                        .append('\t')
                        .append(stackFrame.getClassName()).append('.')
                        .append(stackFrame.getMethodName()).append('(')
                        .append(stackFrame.getFileName())
                        .append(':')
                        .append(stackFrame.getLineNumber())
                        .append(')')
                        .append(EOL);
            }
        }

        return result.toString();
    }
}
