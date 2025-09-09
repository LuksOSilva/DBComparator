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
import java.util.stream.Collectors;

public class FilterSqlBuilder {

    public static String build(ComparedTable comparedTable, ComparedSource comparedSource) {

        // CASE 1: table filter
        TableFilter tableFilter = comparedTable.getFilter();
        if (tableFilter != null) {
            return "(" + tableFilter.getUserWrittenFilter() + ")";
        }

        // CASE 2: column filters
        List<ComparedTableColumn> comparedTableColumns = comparedTable.getComparedTableColumns();

        List<String> conditionList = new ArrayList<>();

        for (ComparedTableColumn comparedTableColumn : comparedTableColumns) {
            if (comparedTableColumn.getColumnFilters().isEmpty()) continue;


            String condition = buildCondition(comparedTableColumn, comparedSource);

            conditionList.add(condition);
        }

        if (!conditionList.isEmpty()) {
            return String.join("\nAND ", conditionList);
        }

        // CASE 3: no filters
        return "";
    }

    private static String buildCondition(ComparedTableColumn comparedTableColumn, ComparedSource comparedSource) {

        String columnName = comparedTableColumn.getColumnName();

        String columnTypeInSource = comparedTableColumn
                .getPerSourceTableColumn()
                .get(comparedSource.getSourceId())
                .getType();

        return comparedTableColumn.getColumnFilters().stream()
                .map(filter -> buildConditionFromFilter(columnName, columnTypeInSource, filter))
                .collect(Collectors.joining("\nAND "));

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
            case 0 -> "\"" + columnName + "\"" + " " + type.getSymbol();
            case 2 -> {
                String lower = formatValue(filter.getLowerValue(), filter.getLowerDate(), columnType);
                String higher = formatValue(filter.getHigherValue(), filter.getHigherDate(), columnType);
                yield "\"" + columnName + "\"" + " " + type.getSymbol() + " " + lower + " AND " + higher;
            }
            default -> {
                String valueStr = formatValue(filter.getValue(), filter.getDate(), columnType);
                yield "\"" + columnName + "\"" + " " + type.getSymbol() + " " + valueStr;
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
