package com.luksosilva.dbcomparator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;

import java.io.File;
import java.io.IOException;

public class JsonUtils {

    public static Comparison loadComparisonFromJson(File file) throws IOException {
        try {
            if (!isFileExtensionValid(file)) throw new IOException("extensão do arquivo deve ser .dbc ou .json");

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(file, Comparison.class);

        }
        catch (IOException e) {
            throw new IOException("Erro ao ler arquivo: " + e.getMessage());
        }
    }

    public static void saveComparisonAsJson(Comparison comparison, File file) throws IOException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(file, comparison);

        } catch (IOException e) {
            throw new IOException(e);
        }

    }

    private static boolean isFileExtensionValid(File file) {
        String extension = getFileExtension(file);

        return !extension.isBlank() || (!extension.equals("json") && !extension.equals("dbc"));
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        int indexOfLastDot = fileName.lastIndexOf('.');

        return (indexOfLastDot == -1) ? "" : fileName.substring(indexOfLastDot +1);
    }

}
