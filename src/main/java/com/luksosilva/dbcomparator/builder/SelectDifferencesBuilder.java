package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.util.SqlFormatter;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectDifferencesBuilder {

    public static String build(ComparedTable comparedTable, List<ComparedSource> comparedSourceList) {

        List<String> sourceIds = comparedTable.getPerSourceTable().keySet().stream().toList();

        List<String> identifierColumnNames = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .map(ComparedTableColumn::getColumnName)
                .toList();

        List<String> comparableColumnNames = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable())
                .map(ComparedTableColumn::getColumnName)
                .toList();

        List<String> allColumnNames = new ArrayList<>();
        allColumnNames.addAll(identifierColumnNames);
        allColumnNames.addAll(comparableColumnNames);


        String withClause = buildWithClause(comparedTable, comparedSourceList, allColumnNames);

        String selectClause = buildSelectClause(sourceIds, identifierColumnNames, comparableColumnNames);

        String groupByClause = buildGroupByClause(identifierColumnNames);

        String havingClause = buildHavingClause(sourceIds,
                comparedTable.getComparableComparedTableColumns().isEmpty() ?
                        comparedTable.getIdentifierComparedTableColumns() : comparedTable.getComparableComparedTableColumns());

        return SqlFormatter.buildSelectDifferences(withClause, selectClause, groupByClause, havingClause);
    }



    private static String buildWithClause(ComparedTable comparedTable,
                                          List<ComparedSource> comparedSourceList,
                                          List<String> columnNames) {

        String tableName = comparedTable.getTableName();

        String columnNamesInQuotes = getColumnNamesInQuotes(columnNames);

        List<String> selectsForWithClause = new ArrayList<>();
        for (ComparedSource comparedSource : comparedSourceList) {

            String filter = FilterSqlBuilder.build(comparedTable, comparedSource);

            String selectForWithClause = buildSelectForWithClause(comparedSource.getSourceId(),
                    columnNamesInQuotes, tableName, filter);

            selectsForWithClause.add(selectForWithClause);
        }

        return String.join("\nUNION ALL\n", selectsForWithClause);
    }

    private static String buildSelectClause(List<String> sourceIds,
                                            List<String> identifierColumnNames,
                                            List<String> comparableColumnNames) {

        String identifierColumnNamesInQuotes = getColumnNamesInQuotes(identifierColumnNames);


        List<String> selectMaxCaseClauses = new ArrayList<>();
        for (String sourceId : sourceIds) {
            for (String comparableColumnName : comparableColumnNames.isEmpty() ? identifierColumnNames : comparableColumnNames) {
                String selectMaxCase = buildMaxCaseWhenForSelectClause(sourceId, comparableColumnName, true);
                selectMaxCaseClauses.add(selectMaxCase);
            }
        }

        String comparableColumnInCase = String.join(",\n", selectMaxCaseClauses);


        return String.join(",\n", identifierColumnNamesInQuotes, comparableColumnInCase);
    }

    private static String buildGroupByClause(List<String> identifierColumnNames) {
        return getColumnNamesInQuotes(identifierColumnNames);
    }

    private static String buildHavingClause(List<String> sourceIds,
                                            List<ComparedTableColumn> comparedTableColumnList) {

        List<String> havingClause = new ArrayList<>();

        for (ComparedTableColumn comparedTableColumn : comparedTableColumnList) {
            String columnName = comparedTableColumn.getColumnName();

            List<String> coalesceValues = new ArrayList<>();

            for (String sourceId : sourceIds) {

                String columnType = comparedTableColumn.getPerSourceTableColumn().get(sourceId).getType();

                String selectMaxCase = buildMaxCaseWhenForSelectClause(sourceId, columnName, false);

                coalesceValues.add(buildCoalesce(selectMaxCase, getDefaultValue(columnType)));
            }

            havingClause.add(String.join(" <> ", coalesceValues));
        }

        return String.join("\nOR ", havingClause);
    }


    ///

    private static String buildCoalesce(String first, String second) {
        return "COALESCE(" + first + "," + second + ")";
    }

    private static String buildMaxCaseWhenForSelectClause(String sourceId, String columnName, boolean useAlias) {
        String base = "MAX(CASE WHEN source = '" + sourceId + "' THEN \"" + columnName + "\" END)";

        if (useAlias) {
            base = base + " AS \"" + sourceId + "_" + columnName + "\"";
        }

        return base;
    }

    private static String buildSelectForWithClause(String sourceId,
                                            String columnNamesInQuotes,
                                            String tableName,
                                            String filter) {
        String base = "SELECT \n" +
                "'{{source_id}}' AS source, \n" +
                "{{columns}} \n" +
                "FROM \"{{source_id}}\".\"{{table_name}}\" \n" +
                "WHERE 1=1";

        if (!filter.isBlank()) {
            base = base + "\nAND {{filter}}";
        }

        return base
                .replace("{{source_id}}", sourceId)
                .replace("{{columns}}", columnNamesInQuotes)
                .replace("{{table_name}}", tableName)
                .replace("{{filter}}", filter);

    }

    ///

    private static String getDefaultValue(String columnType) {
        String t = columnType == null ? "" : columnType.toUpperCase();

        // SQLite affinity detection rules
        if (t.contains("INT")) {
            // INTEGER affinity
            return "-1";
        } else if (t.contains("CHAR") || t.contains("CLOB") || t.contains("TEXT")) {
            // TEXT affinity
            return "''";
        } else if (t.contains("BLOB")) {
            // BLOB affinity — use empty blob literal
            return "X''";
        } else if (t.contains("REAL") || t.contains("FLOA") || t.contains("DOUB")) {
            // REAL affinity
            return "0.0";
        } else if (t.contains("NUMERIC") || t.contains("DEC") || t.contains("BOOL") ||
                t.contains("DATE") || t.contains("TIME")) {
            // NUMERIC affinity
            return "-1";
        }

        // Fallback: treat unknown as TEXT
        return "''";
    }

    private static String getColumnNamesInQuotes(List<String> columnNames) {
        return columnNames.stream()
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(",\n"));
    }
}
