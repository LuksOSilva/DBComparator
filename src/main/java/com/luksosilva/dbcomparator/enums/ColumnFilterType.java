package com.luksosilva.dbcomparator.enums;

import java.util.Arrays;
import java.util.List;

public enum ColumnFilterType {


    EQUALS("=", "igual", concat(TEXT_TYPES(), NUMERIC_TYPES(), DATE_TYPES())),
    NOT_EQUALS("<>", "diferente", concat(TEXT_TYPES(), NUMERIC_TYPES(), DATE_TYPES())),
    GREATER_THAN_OR_EQUAL(">=", "maior ou igual", concat(NUMERIC_TYPES(), DATE_TYPES())),
    LESS_THAN_OR_EQUAL("<=", "menor ou igual", concat(NUMERIC_TYPES(), DATE_TYPES())),
    GREATER_THAN(">", "maior", concat(NUMERIC_TYPES(), DATE_TYPES())),
    LESS_THAN("<", "menor", concat(NUMERIC_TYPES(), DATE_TYPES())),
    LIKE("LIKE", "contém", concat(TEXT_TYPES())),
    NOT_LIKE("NOT LIKE", "não contém", concat(TEXT_TYPES())),
    BETWEEN("BETWEEN", "entre", concat(NUMERIC_TYPES(), DATE_TYPES())),
    NOT_BETWEEN("NOT BETWEEN", "não entre", concat(NUMERIC_TYPES(), DATE_TYPES())),
    IN("IN", "em", concat(TEXT_TYPES(), NUMERIC_TYPES())),
    NOT_IN("NOT IN", "não está em", concat(TEXT_TYPES(), NUMERIC_TYPES())),
    IS_NOT_NULL("IS NOT NULL", "não nulo", concat(TEXT_TYPES(), NUMERIC_TYPES(), DATE_TYPES())),
    IS_NULL("IS NULL", "nulo", concat(TEXT_TYPES(), NUMERIC_TYPES(), DATE_TYPES()));


    private final String symbol;
    private final String description;
    private final List<String> applicableTypes;

    ColumnFilterType(String symbol, String description, List<String> applicableTypes) {
        this.symbol = symbol;
        this.description = description;
        this.applicableTypes = applicableTypes;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public String getDescriptionWithDetail() {
        return description + " " + "[" + symbol + "]";
    }

    public boolean supportsType(String type) {
        return applicableTypes.contains(type.toUpperCase());
    }

    public int getNumberOfArguments() {
        return switch (this){
            case IS_NULL, IS_NOT_NULL -> 0;
            case BETWEEN, NOT_BETWEEN -> 2;
            default -> 1;
        };
    }


    public static List<ColumnFilterType> getSupportedForType(String type) {
        return Arrays.stream(values())
                .filter(f -> f.supportsType(type))
                .toList();
    }

    public static List<ColumnFilterType> getSupportedForTypes(List<String> types) {
        List<String> upperTypes = types.stream()
                .map(String::toUpperCase)
                .map(ColumnFilterType::trimColumnSize)
                .distinct()
                .toList();

        return Arrays.stream(values())
                .filter(filterType -> upperTypes.stream().allMatch(filterType::supportsType))
                .toList();
    }

    public static ColumnFilterType getColumnTypeFromDescription(String description) {
        return Arrays.stream(values())
                .filter(f -> f.getDescription().equalsIgnoreCase(description))
                .findFirst()
                .orElse(null);
    }


    private static String trimColumnSize(String type) {
        return type.replaceAll("\\s*\\(.*\\)$", "");
    }

    @SafeVarargs
    private static List<String> concat(List<String>... lists) {
        return Arrays.stream(lists)
                .flatMap(List::stream)
                .map(String::toUpperCase)
                .distinct()
                .toList();
    }


    private static List<String> TEXT_TYPES() {
        return List.of(
                "TEXT", "VARCHAR", "CHAR", "NVARCHAR", "LONGTEXT", "STRING"
        );
    }

    private static List<String> NUMERIC_TYPES() {
        return List.of(
                "INTEGER", "INT", "BIGINT", "SMALLINT", "TINYINT",
                "REAL", "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC", "NUM"
        );
    }

    private static List<String> DATE_TYPES() {
        return List.of(
                "DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR"
        );
    }
}
