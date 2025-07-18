package com.luksosilva.dbcomparator.builder;


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

        String whereClause = buildWhereClause(comparedSourceList, identifiersComparedColumns, comparableComparedColumns);


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
                    .map(comparedSource -> "\"" + comparedSource.getSourceId() + "_data\".\"" + columnName + "\"")
                    .collect(Collectors.joining(", "));


            coalesceIdentifierColumns.add(SqlFormatter.buildSDCoalesceIdentifierColumns(allIdentifierColumnsWithSource, columnName));
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
                                           List<ComparedTableColumn> identifiersComparedColumns,
                                           List<ComparedTableColumn> comparableComparedColumns) {

        String whereComparableColumnIsDifferent = buildWhereComparableColumnIsDifferent(
                comparedSourceList, comparableComparedColumns);

        String whereColumnIsFiltered = buildWhereColumnIsFiltered(
                comparedSourceList, identifiersComparedColumns, comparableComparedColumns);

        return whereComparableColumnIsDifferent + "\n" + whereColumnIsFiltered;
    }

    private static String buildWhereColumnIsFiltered(List<ComparedSource> comparedSourceList,
                                                     List<ComparedTableColumn> identifiersComparedColumns,
                                                     List<ComparedTableColumn> comparableComparedColumns) {

        List<String> whereColumnIsFilteredList = new ArrayList<>();

        List<ComparedTableColumn> allComparedTableColumns = new ArrayList<>();
        allComparedTableColumns.addAll(identifiersComparedColumns);
        allComparedTableColumns.addAll(comparableComparedColumns);

        List<ComparedTableColumn> allComparedTableColumnsWithFilter = allComparedTableColumns.stream()
                .filter(comparedTableColumn -> !comparedTableColumn.getColumnFilter().isEmpty())
                .toList();

        if (allComparedTableColumnsWithFilter.isEmpty()) {
            return "";
        }

        for (ComparedTableColumn comparedTableColumnWithFilter : allComparedTableColumnsWithFilter) {

            String sourceColumnInFilter = comparedSourceList.stream()
                    .map(comparedSource -> {

                        String base = String.format("\"%s_data\".\"%s\" IN ",
                                comparedSource.getSourceId(), comparedTableColumnWithFilter.getColumnName());

                        String columnTypeInSource = comparedTableColumnWithFilter
                                .getPerSourceTableColumn().get(comparedSource).getType();

                        if (shouldQuoteValue(columnTypeInSource)) {

                            String quotedFilters = comparedTableColumnWithFilter.getColumnFilter().stream()
                                    .map(s -> "\"" + s + "\"")
                                    .collect(Collectors.joining(", "));

                            //return base + "(" + quotedFilters + ")";
                            return "temp";

                        }
                        //return base + "(" + String.join(", ", comparedTableColumnWithFilter.getColumnFilter()) + ")";
                        return "temp";
                    })
                    .collect(Collectors.joining("\nOR "));


            whereColumnIsFilteredList.add("(" + sourceColumnInFilter + ")");

        }

        return "AND \n" + String.join("\nAND\n", whereColumnIsFilteredList);


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
                    whereComparableColumnIsDifferentList.add(SqlFormatter.buildSDWhereClause(conditionComparableColumns));
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

            individualComparisonParts.add(SqlFormatter.buildSDCoalesceComparableColumn(
                    currentComparedSource.getSourceId(), columnName, currentSourceDefaultValue));

            individualComparisonParts.add(SqlFormatter.buildSDCoalesceComparableColumn(
                    nextComparedSource.getSourceId(), columnName, nextSourceDefaultValue));

            coalesceComparableColumns.add(String.join(" <> ", individualComparisonParts));
        }


        return coalesceComparableColumns.isEmpty() ? "" : String.join("\nOR ", coalesceComparableColumns);
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

    private static boolean shouldQuoteValue(String columnType) {

        String lowerCaseColumnType = columnType.toLowerCase();

        return !(lowerCaseColumnType.contains("numeric") ||
                 lowerCaseColumnType.contains("integer") ||
                 lowerCaseColumnType.contains("real") ||
                 lowerCaseColumnType.contains("bool"));

    }







}
