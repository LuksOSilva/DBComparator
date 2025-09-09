package com.luksosilva.dbcomparator.log;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComparisonLogger {

    private final PrintWriter writer;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ComparisonLogger(PrintWriter writer) {
        this.writer = writer;
    }

    public void log(String message) {
        writer.println(getTimestamp() + " " + message);
        writer.flush();
    }

    private String getTimestamp() {
        return "[" +LocalDateTime.now().format(formatter) + "]";
    }

}
