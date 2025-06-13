package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SelectDifferencesService {

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


        List<String> withClause = buildWithClause(comparedSourceList, tableName);

        List<String> coalesceIdentifierColumns = buildCoalesceIdentifierColumns(comparedSourceList, identifiersComparedColumns);



        return "";
    }

    private static List<String> buildWithClause(List<ComparedSource> comparedSourceList, String tableName) {
        List<String> withClause = new ArrayList<>();
        for (ComparedSource comparedSource : comparedSourceList) {
            String sourceIdData = comparedSource.getSourceId() + "_data";
            withClause.add(SqlService.buildSDWithClause(sourceIdData, comparedSource.getSourceId(), tableName));
        }
        return withClause;
    }

    private static List<String> buildCoalesceIdentifierColumns(List<ComparedSource> comparedSourceList,
                                                               List<ComparedTableColumn> identifierComparedColumns) {

        List<String> coalesceIdentifierColumns = new ArrayList<>();

        for (ComparedTableColumn identifierComparedColumn : identifierComparedColumns) {
            String columnName = identifierComparedColumn.getColumnName();

            String allIdentifierColumnsWithSource = comparedSourceList.stream()
                    .map(comparedSource -> "\"" + comparedSource.getSourceId() + "\".\"" + columnName + "\"")
                    .collect(Collectors.joining(", "));


            coalesceIdentifierColumns.add(SqlService.buildSDIdentifierColumns(allIdentifierColumnsWithSource, columnName));
        }


        return coalesceIdentifierColumns;

    }







}
