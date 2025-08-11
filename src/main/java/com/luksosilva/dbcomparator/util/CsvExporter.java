package com.luksosilva.dbcomparator.util;

import java.io.IOException;
import java.util.List;

public class CsvExporter<T> {

    private final List<String> headers;
    private final List<T> data;
    private final CsvValueProvider<T> valueProvider;

    public CsvExporter(List<String> headers, List<T> data, CsvValueProvider<T> valueProvider) {
        this.headers = headers;
        this.data = data;
        this.valueProvider = valueProvider;
    }

    public void exportToFile(java.io.File file) throws IOException {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file))) {
            writer.write(String.join(",", escapeCsvList(headers)));
            writer.newLine();

            for (T item : data) {
                List<String> row = valueProvider.provideValues(item);
                writer.write(String.join(",", escapeCsvList(row)));
                writer.newLine();
            }
        }
    }

    private List<String> escapeCsvList(List<String> values) {
        return values.stream().map(CsvExporter::escapeCsv).toList();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    // Functional interface for value extraction
    @FunctionalInterface
    public interface CsvValueProvider<T> {
        List<String> provideValues(T item);
    }
}
