package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.enums.SqlPlaceholders;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.util.*;
import java.util.stream.Collectors;

public class SelectDifferencesBuilder {

    public static String build(ComparedTable comparedTable) {

        String tableName = comparedTable.getTableName();

        List<ComparedTableColumn> identifierComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .toList();

        List<ComparedTableColumn> comparableComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable())
                .toList();

        List<ComparedSource> comparedSources = new ArrayList<>(comparedTable.getPerSourceTable().keySet());




        String withClause = buildWithClause(comparedSources, tableName);

        String selectClause = buildSelectClause(comparedSources, identifierComparedColumns, comparableComparedColumns);

        String fromClause = buildFromClause(comparedSources,
                identifierComparedColumns.isEmpty() ? comparableComparedColumns : identifierComparedColumns);

        String whereClause = buildWhereClause(comparedTable, comparedSources, comparableComparedColumns);

        String userFilter = comparedTable.getSqlUserFilter();

        if (!userFilter.isBlank()) {
            whereClause = whereClause + "\nAND " + userFilter;
        }

        return SqlFormatter.buildSelectDiff(withClause, selectClause, fromClause, whereClause);
    }

    private static String buildWithClause(List<ComparedSource> comparedSourceList, String tableName) {
        List<String> withClauseList = new ArrayList<>();

        for (ComparedSource comparedSource : comparedSourceList) {
            withClauseList.add(getSQLWithClause(comparedSource.getSourceId(), tableName));
        }

        return String.join(",\n", withClauseList);
    }

    private static String buildSelectClause(List<ComparedSource> comparedSourceList,
                                            List<ComparedTableColumn> identifiersComparedColumns,
                                            List<ComparedTableColumn> comparableComparedColumns) {


        String coalesceIdentifierColumns = buildSelectCoalesceColumns(comparedSourceList, identifiersComparedColumns);

        String selectComparableColumns = buildSelectColumns(comparedSourceList, comparableComparedColumns);

        if (selectComparableColumns.isEmpty()) {
            return coalesceIdentifierColumns;
        }

        return String.join(",\n", coalesceIdentifierColumns, selectComparableColumns);

    }

    private static String buildFromClause (List<ComparedSource> comparedSourceList,
                                           List<ComparedTableColumn> joinersComparedColumns) {


        ComparedSource firstComparedSource = comparedSourceList.getFirst();

        List<String> joinClauseList = new ArrayList<>();

        //skips first because doesn't need join.
        for (int i = 1; i < comparedSourceList.size(); i++) {

            joinClauseList.add(buildJoinClause(comparedSourceList, i, joinersComparedColumns));

        }


        String joinClause = joinClauseList.isEmpty() ? "" : String.join("\n", joinClauseList);


        return getSQLFromClause(firstComparedSource.getSourceId(), joinClause);
    }

    private static String buildWhereClause(ComparedTable comparedTable,
                                           List<ComparedSource> comparedSourceList,
                                           List<ComparedTableColumn> comparableComparedColumns) {

        String whereColumnIsDifferent = buildWhereComparableColumnIsDifferent(
                comparedSourceList, comparableComparedColumns);



        return whereColumnIsDifferent;
    }

    private static String buildWhereComparableColumnIsDifferent(List<ComparedSource> comparedSourceList,
                                                                List<ComparedTableColumn> comparableComparedColumns) {

        List<String> whereComparableColumnIsDifferentList = new ArrayList<>();

        // Loop through all sources for the first operand of the comparison.
        for (int i = 0; i < comparedSourceList.size(); i++) {
            ComparedSource currentComparedSource = comparedSourceList.get(i);

            // Loop through all subsequent sources for the second operand,
            for (int j = i + 1; j < comparedSourceList.size(); j++) {
                ComparedSource nextComparedSource = comparedSourceList.get(j);

                String conditionComparableColumns = buildCoalesceComparableColumn(
                        comparableComparedColumns, currentComparedSource, nextComparedSource);

                if (!conditionComparableColumns.isEmpty()){
                    whereComparableColumnIsDifferentList.add(getSQLWhereClause(conditionComparableColumns));
                }
            }
        }

        return whereComparableColumnIsDifferentList.isEmpty() ? "" : "AND\n(" + String.join("\n\nOR\n\n", whereComparableColumnIsDifferentList) + ")";
    }

    private static String buildCoalesceComparableColumn(List<ComparedTableColumn> comparableComparedColumns,
                                                        ComparedSource currentComparedSource,
                                                        ComparedSource nextComparedSource) {

        List<String> coalesceComparableColumns = new ArrayList<>();

        for (ComparedTableColumn comparableComparedColumn : comparableComparedColumns) {

            String columnName = comparableComparedColumn.getColumnName();

            String currentSourceColumnType =
                    comparableComparedColumn.getPerSourceTableColumn().get(currentComparedSource).getType();
            String nextSourceColumnType =
                    comparableComparedColumn.getPerSourceTableColumn().get(nextComparedSource).getType();


            String currentSourceDefaultValue = getDefaultValue(currentSourceColumnType);
            String nextSourceDefaultValue = getDefaultValue(nextSourceColumnType);

            List<String> individualComparisonParts = new ArrayList<>();

            individualComparisonParts.add(getSQLCoalesceComparableColumn(
                    currentComparedSource.getSourceId(), columnName, currentSourceDefaultValue));

            individualComparisonParts.add(getSQLCoalesceComparableColumn(
                    nextComparedSource.getSourceId(), columnName, nextSourceDefaultValue));

            coalesceComparableColumns.add(String.join(" <> ", individualComparisonParts));
        }


        return coalesceComparableColumns.isEmpty() ? "" : String.join("\nOR ", coalesceComparableColumns);
    }





    private static String buildJoinClause (List<ComparedSource> comparedSourceList,
                                           int currentIndex,
                                           List<ComparedTableColumn> joinersComparedColumns) {

        ComparedSource currentComparedSource = comparedSourceList.get(currentIndex);

        List<String> onClauseList = new ArrayList<>();



        //loops through all previous compared sources to create all necessary ON clauses.
        for (int i = 0; i < currentIndex; i++) {
            ComparedSource previousComparedSource = comparedSourceList.get(i);
            onClauseList.add(buildOnClause(currentComparedSource, previousComparedSource, joinersComparedColumns));
        }


        String onClause = onClauseList.isEmpty() ? "" : String.join("\nOR\n", onClauseList);


        return getSQLJoinClause(currentComparedSource.getSourceId(), onClause);
    }

    private static String buildOnClause (ComparedSource currentComparedSource,
                                         ComparedSource previousComparedSource,
                                         List<ComparedTableColumn> identifiersComparedColumns) {


        String currentSourceId = currentComparedSource.getSourceId();
        String previousSourceId = previousComparedSource.getSourceId();

        String equalsIdentifierColumns = identifiersComparedColumns.stream()
                .map(comparedTableColumn -> {

                    String columnName = comparedTableColumn.getColumnName();

                    return String.format("\"%s_data\".\"%s\" = \"%s_data\".\"%s\"",
                            currentSourceId, columnName, previousSourceId, columnName);

                })
                .collect(Collectors.joining("\nAND "));


        return getSQLOnClause(equalsIdentifierColumns);

    }




    private static String buildSelectCoalesceColumns(List<ComparedSource> comparedSourceList,
                                                     List<ComparedTableColumn> identifierComparedColumns) {

        List<String> coalesceColumns = new ArrayList<>();

        for (ComparedTableColumn identifierComparedColumn : identifierComparedColumns) {
            String columnName = identifierComparedColumn.getColumnName();

            String allColumnsWithSource = comparedSourceList.stream()
                    .map(comparedSource -> "\"" + comparedSource.getSourceId() + "_data\".\"" + columnName + "\"")
                    .collect(Collectors.joining(", "));


            coalesceColumns.add(getSQLCoalesceColumns(allColumnsWithSource, columnName));
        }


        return String.join(",\n", coalesceColumns);

    }

    private static String buildSelectColumns (List<ComparedSource> comparedSourceList,
                                                        List<ComparedTableColumn> comparableComparedColumns) {

        List<String> selectComparableColumns = new ArrayList<>();

        for (ComparedSource comparedSource : comparedSourceList) {

            for (ComparedTableColumn comparableComparedColumn : comparableComparedColumns) {

                selectComparableColumns.add(
                        getSQLSelectColumns(comparedSource.getSourceId(),
                        comparableComparedColumn.getColumnName()));

            }
        }

        return selectComparableColumns.isEmpty() ? "" : String.join(",\n", selectComparableColumns);
    }

    private static String getDefaultValue(String columnType) {

        String lowerCaseColumnType = columnType.toLowerCase();

        return switch (lowerCaseColumnType) {
            case String t when t.contains("numeric") -> "-1";
            case String t when t.contains("integer") -> "-1";
            case String t when t.contains("real") -> "0.0";
            case String t when t.contains("date") -> "'1970-01-01'";
            default -> "''";
        };
    }




    /// SQL


    private static String getSQLWithClause(String sourceId, String tableName) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.TABLE_NAME, tableName
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_WITH_CLAUSE, placeholders);
    }

    public static String getSQLCoalesceColumns(String identifierColumns, String columnName) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.IDENTIFIER_COLUMNS, identifierColumns,
                SqlPlaceholders.COLUMN_NAME, columnName
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_COALESCE_IDENTIFIER_COLUMN, placeholders);

    }

    public static String getSQLSelectColumns(String sourceId, String columnName) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.COLUMN_NAME, columnName
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_SELECT_COMPARABLE_COLUMNS, placeholders);
    }

    public static String getSQLFromClause(String sourceId, String joinClause) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.JOIN_CLAUSE, joinClause
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_FROM_CLAUSE, placeholders);
    }
    public static String getSQLJoinClause(String sourceId, String onClause) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.ON_CLAUSE, onClause
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_JOIN_CLAUSE, placeholders);

    }
    public static String getSQLOnClause(String equalsIdentifierColumns) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.EQUALS_IDENTIFIER_COLUMNS, equalsIdentifierColumns
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_ON_CLAUSE, placeholders);

    }

    public static String getSQLCoalesceComparableColumn(String sourceId, String columnName, String defaultValue) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.COLUMN_NAME, columnName,
                SqlPlaceholders.DEFAULT_VALUE, defaultValue
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_COALESCE_COMPARABLE_COLUMN, placeholders);

    }

    public static String getSQLWhereClause(String conditionComparableColumns) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.CONDITION_COMPARABLE_COLUMNS, conditionComparableColumns
        );

        return SqlFormatter.buildSQL(SqlFiles.SD_WHERE_CLAUSE, placeholders);

    }

}