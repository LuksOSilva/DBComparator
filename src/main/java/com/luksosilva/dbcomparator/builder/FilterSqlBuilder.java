package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.live.comparison.customization.TableFilter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterSqlBuilder {

    public static String build(ComparedTable comparedTable, List<ComparedSource> comparedSourceList) {


        // CASE 1: table filter
        TableFilter tableFilter = comparedTable.getFilter();
        if (tableFilter != null) {
            return "(" + tableFilter.getUserWrittenFilter() + ")";
        }


        // CASE 2: column filters
        List<ComparedTableColumn> comparedTableColumns = comparedTable.getComparedTableColumns();

        List<String> whereColumnIsFilteredList = new ArrayList<>();

        for (ComparedTableColumn comparedTableColumn : comparedTableColumns) {
            if (comparedTableColumn.getColumnFilters().isEmpty()) {
                continue;
            }


            // Build per-source filter strings (OR between sources)
            String sourceColumnInFilter = comparedTable.getPerSourceTable().keySet().stream()
                    .map(sourceId -> {
                        return buildPerSourceCondition(Objects.requireNonNull(comparedSourceList.stream()
                                .filter(comparedSource -> comparedSource.getSourceId().equals(sourceId))
                                .findFirst()
                                .orElse(null)), comparedTableColumn);
                    })
                    .collect(Collectors.joining("\nOR ")); // multiple sources → OR

            whereColumnIsFilteredList.add("(" + sourceColumnInFilter + ")");
        }

        if (!whereColumnIsFilteredList.isEmpty()) {
            return String.join("\nAND\n", whereColumnIsFilteredList);
        }

        // CASE 3: no filters
        return "";
    }

    public static String build(ComparedTable comparedTable, ComparedSource comparedSource) {

        // CASE 1: table filter
        TableFilter tableFilter = comparedTable.getFilter();
        if (tableFilter != null) {
            return "(" + tableFilter.getUserWrittenFilter() + ")";
        }

        // CASE 2: column filters
        List<ComparedTableColumn> comparedTableColumns = comparedTable.getComparedTableColumns();

        List<String> whereColumnIsFilteredList = new ArrayList<>();

        for (ComparedTableColumn comparedTableColumn : comparedTableColumns) {
            if (comparedTableColumn.getColumnFilters().isEmpty()) {
                continue;
            }

            // Build per-source filter strings (OR between sources)
            String perSourceCondition = buildPerSourceCondition(comparedSource, comparedTableColumn);

            whereColumnIsFilteredList.add("(" + perSourceCondition + ")");
        }

        if (!whereColumnIsFilteredList.isEmpty()) {
            return String.join("\nAND\n", whereColumnIsFilteredList);
        }

        // CASE 3: no filters
        return "";

    }

    private static String buildPerSourceCondition(ComparedSource comparedSource, ComparedTableColumn comparedTableColumn) {

        String columnName = String.format("\"%s_data\".\"%s\"",
                comparedSource.getSourceId(), comparedTableColumn.getColumnName());

        String columnTypeInSource = comparedTableColumn
                .getPerSourceTableColumn()
                .get(comparedSource.getSourceId())
                .getType();

        return comparedTableColumn.getColumnFilters().stream()
                .map(filter -> buildConditionFromFilter(columnName, columnTypeInSource, filter))
                .collect(Collectors.joining(" AND ")); // multiple filters per column → AND

    }


    private static String buildConditionFromFilter(String columnName, String columnType, ColumnFilter filter) {
        ColumnFilterType type = filter.getColumnFilterType();


        if (type == ColumnFilterType.IN || type == ColumnFilterType.NOT_IN) {
            String[] values = filter.getValue().split(",");
            String formattedValues = Arrays.stream(values)
                    .map(String::trim)
                    .map(val -> shouldQuoteValue(columnType) ? "'" + val + "'" : val)
                    .collect(Collectors.joining(", "));
            return columnName + " " + type.getSymbol() + " (" + formattedValues + ")";
        }


        return switch (type.getNumberOfArguments()) {
            case 0 -> columnName + " " + type.getSymbol();
            case 2 -> {
                String lower = formatValue(filter.getLowerValue(), filter.getLowerDate(), columnType);
                String higher = formatValue(filter.getHigherValue(), filter.getHigherDate(), columnType);
                yield columnName + " " + type.getSymbol() + " " + lower + " AND " + higher;
            }
            default -> {
                String valueStr = formatValue(filter.getValue(), filter.getDate(), columnType);
                yield columnName + " " + type.getSymbol() + " " + valueStr;
            }
        };
    }

    private static String formatValue(String stringVal, LocalDateTime dateVal, String columnType) {
        String value;

        if (stringVal != null) {
            value = stringVal;
        } else if (dateVal != null) {
            value = dateVal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            return "NULL";
        }

        return shouldQuoteValue(columnType) ? "'" + value + "'" : value;
    }

    /// helper

    private static boolean shouldQuoteValue(String columnType) {

        String lowerCaseColumnType = columnType.toLowerCase();

        return !(lowerCaseColumnType.contains("numeric") ||
                lowerCaseColumnType.contains("integer") ||
                lowerCaseColumnType.contains("real") ||
                lowerCaseColumnType.contains("bool"));

    }

}
