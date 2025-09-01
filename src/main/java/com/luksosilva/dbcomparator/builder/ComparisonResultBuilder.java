package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.model.live.comparison.result.*;
import com.luksosilva.dbcomparator.persistence.ComparisonQueryExecutor;
import com.luksosilva.dbcomparator.util.FileUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class ComparisonResultBuilder {


    public static ComparisonResult build(Comparison comparison) {

        ComparisonResult comparisonResult = new ComparisonResult();

        for (ComparedTable comparedTable : comparison.getComparedTables()) {

            comparisonResult.addTableComparisonResult(buildTableComparisonResult(comparedTable, comparison.getComparedSources()));

        }


        return comparisonResult;
    }


    public static TableComparisonResult buildTableComparisonResult(ComparedTable comparedTable, List<ComparedSource> comparedSourceList) {

        TableComparisonResult tableComparisonResult = new TableComparisonResult(comparedTable);

        Map<String, String> sourcesInfo = new HashMap<>();
        for (ComparedSource comparedSource : comparedSourceList) {

            sourcesInfo.put(
                    comparedSource.getSourceId(),
                    FileUtils.getCanonicalPath(comparedSource.getSource().getPath())
            );
        }

        try (Stream<Map<String,Object>> rows =
                     ComparisonQueryExecutor.streamQueryDifferences(sourcesInfo, comparedTable.getSqlSelectDifferences())) {

            rows.forEach(row -> {
                RowDifference rowDifference = buildRowDifference(comparedTable, row);
                tableComparisonResult.addRowDifference(rowDifference);
            });

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while comparing table: " + comparedTable.getTableName(), e);
        }

        return tableComparisonResult;
    }


    private static RowDifference buildRowDifference(ComparedTable comparedTable, Map<String, Object> rowData) {

        RowDifference rowDifference = new RowDifference();

        List<String> sourceIdList = new ArrayList<>();
        comparedTable.getPerSourceTable().forEach((sourceId, sourceTable) ->
                sourceIdList.add(sourceId));

        List<ComparedTableColumn> identifiersComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .toList();

        List<ComparedTableColumn> comparableComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable())
                .toList();


        Map<ComparedTableColumn, Map<String, Object>> perComparedColumnSourceValue = new HashMap<>();

        for (Map.Entry<String, Object> entry : rowData.entrySet()) {

            String columnName = entry.getKey();
            Object value = entry.getValue();


            if (columnExistsIn(columnName, identifiersComparedColumns)) {

                ComparedTableColumn comparedTableColumn =
                        getComparedTableColumnFromColumnName(columnName, identifiersComparedColumns);


                rowDifference.addIdentifierColumn(buildIdentifierColumn(comparedTableColumn, value));
                continue;
            }

            // comparable OR if all are identifiers:

            int separatorIndex = columnName.indexOf('_');
            if (separatorIndex == -1) continue;

            String sourceId = columnName.substring(0, separatorIndex);
            String columnNameWithoutSourceId = columnName.substring(separatorIndex +1);


            ComparedTableColumn comparedTableColumn =
                    getComparedTableColumnFromColumnName(columnNameWithoutSourceId, comparedTable.getComparedTableColumns());



            if (columnExistsIn(columnNameWithoutSourceId, comparableComparedColumns)) {
                perComparedColumnSourceValue
                        .computeIfAbsent(comparedTableColumn, k -> new HashMap<>())
                        .put(sourceId, value);
            }

            if (value != null) {
                rowDifference.addExistsOnSource(sourceId);
            }
        }

        removeEmptySources(sourceIdList, perComparedColumnSourceValue);

        rowDifference.addAllDifferingColumns(buildComparableColumns(perComparedColumnSourceValue));

        return rowDifference;
    }



    private static IdentifierColumn buildIdentifierColumn (ComparedTableColumn comparedTableColumn, Object value) {

        IdentifierColumn identifierColumn = new IdentifierColumn();
        identifierColumn.setComparedTableColumn(comparedTableColumn);
        identifierColumn.setValue(value);

        return identifierColumn;

    }

    private static List<ComparableColumn> buildComparableColumns (Map<ComparedTableColumn,
            Map<String, Object>> perComparedColumnSourceValue) {

        List<ComparableColumn> comparableColumnList = new ArrayList<>();

        for (Map.Entry<ComparedTableColumn, Map<String, Object>> entry : perComparedColumnSourceValue.entrySet()) {

            ComparedTableColumn comparedTableColumn = entry.getKey();
            Map<String, Object> perSourceValue = entry.getValue();


            ComparableColumn comparableColumn = new ComparableColumn();
            comparableColumn.setComparedTableColumn(comparedTableColumn);
            comparableColumn.setPerSourceValue(perSourceValue);

            comparableColumnList.add(comparableColumn);

        }

        return comparableColumnList;
    }


    //helper methods

    private static boolean columnDiffers(Map<ComparedSource, Object> perSourceValue) {

        if (perSourceValue == null) {
            return false;
        }


        List<Object> values = new ArrayList<>();
        for (Object value : perSourceValue.values()) {
            values.add((value == null) ? "" : value);
        }

        HashSet<Object> distinctValues = new HashSet<>(values);

        return distinctValues.size() > 1;


    }

    private static boolean columnExistsIn (String columnName, List<ComparedTableColumn> columns) {
        ComparedTableColumn comparedTableColumn = getComparedTableColumnFromColumnName(columnName, columns);

        if (comparedTableColumn != null) {
            return true;
        }
        return false;
    }


    private static ComparedTableColumn getComparedTableColumnFromColumnName(String columnName,
                                                                            List<ComparedTableColumn> comparedTableColumnList) {

        for (ComparedTableColumn comparedTableColumn : comparedTableColumnList) {
            if (!comparedTableColumn.getColumnName().equals(columnName)) continue;

            return comparedTableColumn;

        }

        return null;

    }

    private static ComparedSource getComparedSourceFromSourceId(String sourceId,
                                                                List<ComparedSource> comparedSourceList) {

        for (ComparedSource comparedSource : comparedSourceList) {
            if (!comparedSource.getSourceId().equals(sourceId)) continue;

            return comparedSource;
        }

        return null;
    }

    private static void removeEmptySources(List<String> sourceIdList,
                                           Map<ComparedTableColumn, Map<String, Object>> perComparedColumnSourceValue) {

        Set<String> sourcesWithOnlyNulls = new HashSet<>();

        for (String sourceId : sourceIdList) {
            boolean allNull = true;
            for (Map<String, Object> perSourceValue : perComparedColumnSourceValue.values()) {
                Object val = perSourceValue.get(sourceId);
                if (val != null) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                sourcesWithOnlyNulls.add(sourceId);
            }
        }


        for (Map<String, Object> perSourceValue : perComparedColumnSourceValue.values()) {
            sourcesWithOnlyNulls.forEach(perSourceValue::remove);
        }

    }

}
