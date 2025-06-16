package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.exception.ComparisonException;
import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SelectDifferencesBuilder {



    public static String build(ComparedTable comparedTable) {

        String tableName = comparedTable.getTableName();

        List<ComparedTableColumn> identifiersComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .toList();

        List<ComparedTableColumn> comparableComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable())
                .toList();


        List<ComparedSource> comparedSourceList = new ArrayList<>();
        comparedTable.getPerSourceTable().forEach((comparedSource, sourceTable) ->
                comparedSourceList.add(comparedSource));


        String withClause = buildWithClause(comparedSourceList, tableName);

        String selectClause = buildSelectClause(comparedSourceList, identifiersComparedColumns, comparableComparedColumns);

        String fromClause = buildFromClause(comparedSourceList, identifiersComparedColumns);

        String whereClause = buildWhereClause(comparedSourceList, comparableComparedColumns);


        return SqlFormatter.buildSelectDifferences(withClause, selectClause, fromClause, whereClause);
    }





    private static String buildWithClause(List<ComparedSource> comparedSourceList, String tableName) {
        List<String> withClauseList = new ArrayList<>();
        for (ComparedSource comparedSource : comparedSourceList) {
            withClauseList.add(SqlFormatter.buildSDWithClause(comparedSource.getSourceId(), tableName));
        }

        return String.join(",\n", withClauseList);
    }

    private static String buildSelectClause(List<ComparedSource> comparedSourceList,
                                            List<ComparedTableColumn> identifiersComparedColumns,
                                            List<ComparedTableColumn> comparableComparedColumns) {

        String coalesceIdentifierColumns = buildCoalesceIdentifierColumns(comparedSourceList, identifiersComparedColumns);

        String selectComparableColumns = buildSelectComparableColumns(comparedSourceList, comparableComparedColumns);

        if (selectComparableColumns.isEmpty()) {
            return coalesceIdentifierColumns;
        }

        return String.join(",\n", coalesceIdentifierColumns, selectComparableColumns);

    }

    private static String buildCoalesceIdentifierColumns(List<ComparedSource> comparedSourceList,
                                                         List<ComparedTableColumn> identifierComparedColumns) {

        List<String> coalesceIdentifierColumns = new ArrayList<>();

        for (ComparedTableColumn identifierComparedColumn : identifierComparedColumns) {
            String columnName = identifierComparedColumn.getColumnName();

            String allIdentifierColumnsWithSource = comparedSourceList.stream()
                    .map(comparedSource -> "\"" + comparedSource.getSourceId() + "\".\"" + columnName + "\"")
                    .collect(Collectors.joining(", "));


            coalesceIdentifierColumns.add(SqlFormatter.buildSDCoalesceIdentifierColumns(allIdentifierColumnsWithSource, columnName));
        }

        if (coalesceIdentifierColumns.isEmpty()) {
            throw new ComparisonException("Todas as tabelas comparadas devem ter ao menos uma coluna identificadora");
        }

        return String.join(",\n", coalesceIdentifierColumns);

    }

    private static String buildSelectComparableColumns (List<ComparedSource> comparedSourceList,
                                                        List<ComparedTableColumn> comparableComparedColumns) {

        List<String> selectComparableColumns = new ArrayList<>();

        for (ComparedSource comparedSource : comparedSourceList) {

            for (ComparedTableColumn comparableComparedColumn : comparableComparedColumns) {

                selectComparableColumns.add(
                        SqlFormatter.buildSDSelectComparableColumns(comparedSource.getSourceId(),
                        comparableComparedColumn.getColumnName()));

            }
        }

        return selectComparableColumns.isEmpty() ? "" : String.join(",\n", selectComparableColumns);
    }

    private static String buildFromClause (List<ComparedSource> comparedSourceList,
                                           List<ComparedTableColumn> identifiersComparedColumns) {


        ComparedSource firstComparedSource = comparedSourceList.getFirst();

        List<String> joinClauseList = new ArrayList<>();

        //skips first compared source on purpose (first selected item doesn't need join).
        for (int i = 1; i < comparedSourceList.size(); i++) {

            joinClauseList.add(buildJoinClause(comparedSourceList, i, identifiersComparedColumns));

        }


        String joinClause = joinClauseList.isEmpty() ? "" : String.join("\n", joinClauseList);


        return SqlFormatter.buildSDFromClause(firstComparedSource.getSourceId(), joinClause);
    }




    private static String buildJoinClause (List<ComparedSource> comparedSourceList,
                                           int currentIndex,
                                           List<ComparedTableColumn> identifiersComparedColumns) {

        ComparedSource currentComparedSource = comparedSourceList.get(currentIndex);

        List<String> onClauseList = new ArrayList<>();

        //loops through all previous compared sources to create all necessary ON clauses.
        for (int i = 0; i < currentIndex; i++) {

        ComparedSource previousComparedSource = comparedSourceList.get(i);

        onClauseList.add(buildOnClause(currentComparedSource, previousComparedSource, identifiersComparedColumns));

        }


        String onClause = onClauseList.isEmpty() ? "" : String.join("\nOR\n", onClauseList);


        return SqlFormatter.buildSDJoinClause(currentComparedSource.getSourceId(), onClause);
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


        return SqlFormatter.buildSDOnClause(equalsIdentifierColumns);

    }

    private static String buildWhereClause(List<ComparedSource> comparedSourceList,
                                           List<ComparedTableColumn> comparableComparedColumns) {

        List<String> whereClauseList = new ArrayList<>();

        // Loop through all sources for the first operand of the comparison.
        for (int i = 0; i < comparedSourceList.size(); i++) {
            ComparedSource currentComparedSource = comparedSourceList.get(i);

            // Loop through all subsequent sources for the second operand,
            for (int j = i + 1; j < comparedSourceList.size(); j++) {
                ComparedSource nextComparedSource = comparedSourceList.get(j);

                String conditionComparableColumns = buildCoalesceComparableColumn(
                        comparableComparedColumns, currentComparedSource, nextComparedSource);


                whereClauseList.add(SqlFormatter.buildSDWhereClause(conditionComparableColumns));
            }
        }


        return whereClauseList.isEmpty() ? "" : "WHERE\n" + String.join("\n\nOR\n\n", whereClauseList);
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

            individualComparisonParts.add(SqlFormatter.buildSDCoalesceComparableColumn(
                    currentComparedSource.getSourceId(), columnName, currentSourceDefaultValue));

            individualComparisonParts.add(SqlFormatter.buildSDCoalesceComparableColumn(
                    nextComparedSource.getSourceId(), columnName, nextSourceDefaultValue));

            coalesceComparableColumns.add(String.join(" <> ", individualComparisonParts));
        }


        return String.join("\nOR ", coalesceComparableColumns);
    }

    private static String getDefaultValue(String columnType) {
        return switch (columnType) {
            case String t when t.contains("NUMERIC") -> "-1";
            case String t when t.contains("INTEGER") -> "-1";
            case String t when t.contains("REAL") -> "0.0";
            case String t when t.contains("DATE") -> "'1970-01-01'";
            default -> "''";
        };
    }







}
