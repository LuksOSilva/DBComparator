package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SelectDifferencesBuilder {

    public static String buildSelectDifferences(ComparedTable comparedTable) {

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

        List<String> coalesceIdentifierColumns = buildCoalesceIdentifierColumns(comparedSourceList, identifiersComparedColumns);

        List<String> selectComparableColumns = buildSelectComparableColumns(comparedSourceList, comparableComparedColumns);

        String fromClause = buildFromClause(comparedSourceList, identifiersComparedColumns);


        return "";
    }

    private static String buildWithClause(List<ComparedSource> comparedSourceList, String tableName) {
        List<String> withClauseList = new ArrayList<>();
        for (ComparedSource comparedSource : comparedSourceList) {
            withClauseList.add(SqlFormatter.buildSDWithClause(comparedSource.getSourceId(), tableName));
        }

        return String.join(",\n", withClauseList);
    }

    private static List<String> buildCoalesceIdentifierColumns(List<ComparedSource> comparedSourceList,
                                                               List<ComparedTableColumn> identifierComparedColumns) {

        List<String> coalesceIdentifierColumns = new ArrayList<>();

        for (ComparedTableColumn identifierComparedColumn : identifierComparedColumns) {
            String columnName = identifierComparedColumn.getColumnName();

            String allIdentifierColumnsWithSource = comparedSourceList.stream()
                    .map(comparedSource -> "\"" + comparedSource.getSourceId() + "\".\"" + columnName + "\"")
                    .collect(Collectors.joining(", "));


            coalesceIdentifierColumns.add(SqlFormatter.buildSDCoalesceIdentifierColumns(allIdentifierColumnsWithSource, columnName));
        }


        return coalesceIdentifierColumns;

    }

    private static List<String> buildSelectComparableColumns (List<ComparedSource> comparedSourceList,
                                                              List<ComparedTableColumn> comparableComparedColumns) {

        List<String> selectComparableColumns = new ArrayList<>();

        for (ComparedSource comparedSource : comparedSourceList) {

            for (ComparedTableColumn comparableComparedColumn : comparableComparedColumns) {

                selectComparableColumns.add(
                        SqlFormatter.buildSDSelectComparableColumns(comparedSource.getSourceId(),
                        comparableComparedColumn.getColumnName()));

            }
        }

        return selectComparableColumns;
    }

    private static String buildFromClause (List<ComparedSource> comparedSourceList,
                                           List<ComparedTableColumn> identifiersComparedColumns) {


        ComparedSource firstComparedSource = comparedSourceList.getFirst();

        List<String> joinClauseList = new ArrayList<>();

        //skips first compared source on purpose.
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




}
