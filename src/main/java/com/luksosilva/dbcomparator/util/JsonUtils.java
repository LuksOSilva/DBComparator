package com.luksosilva.dbcomparator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;

import java.io.File;
import java.io.IOException;

public class JsonUtils {

    public static Comparison loadComparisonFromJson(File file) throws IOException {
        try {
            if (!file.exists() || !file.isFile()) {
                throw new IOException("O arquivo não existe: " + file.getCanonicalPath());
            }
            if (!isFileExtensionValid(file)) {
                throw new IOException("extensão do arquivo deve ser .dbc ou .json");
            }

            ObjectMapper mapper = new ObjectMapper();
            Comparison comparison;

            try {

                comparison = mapper.readValue(file, Comparison.class);

            } catch (IOException e) {
                throw new IOException("Esse arquivo não parece ser uma comparação.");
            }

            // Validate the contents
            validateComparison(comparison);

            return comparison;

        }
        catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static void validateComparison(Comparison comparison) throws IOException {
        if (comparison == null) {
            throw new IOException("O JSON está vazio ou não corresponde à uma comparação válida.");
        }

        // Example: assuming Comparison has a name and a list of sources
        if (comparison.getComparedTables() == null || comparison.getComparedTables().isEmpty()) {
            throw new IOException("Não foi possível ler as informações das tabelas comparadas.");
        }
        if (comparison.getComparedSources() == null || comparison.getComparedSources().isEmpty()) {
            throw new IOException("Não foi possível ler as informações das fontes comparadas.");
        }
        if (comparison.getComparisonResult() == null
                || comparison.getComparisonResult().getTableComparisonResults().isEmpty()) {
            throw new IOException("Não foi possível ler as informações do resultado da comparação.");
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

        return !extension.isBlank() && (!extension.equals(".json") && !extension.equals(".dbc"));
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        int indexOfLastDot = fileName.lastIndexOf('.');

        return (indexOfLastDot == -1) ? "" : fileName.substring(indexOfLastDot +1);
    }

}
