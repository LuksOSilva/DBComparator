package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.comparison.result.*;
import com.luksosilva.dbcomparator.repository.ComparisonRepository;
import com.luksosilva.dbcomparator.util.FileUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class ComparisonResultBuilder {


    public static ComparisonResult build(Comparison comparison) {

        ComparisonResult comparisonResult = new ComparisonResult();

        for (ComparedTable comparedTable : comparison.getComparedTables()) {

            comparisonResult.addTableComparisonResult(buildTableComparisonResult(comparedTable));

        }


        return comparisonResult;
    }


    public static TableComparisonResult buildTableComparisonResult(ComparedTable comparedTable) {

        TableComparisonResult tableComparisonResult = new TableComparisonResult(comparedTable);

        Map<String, String> sourcesInfo = new HashMap<>();
        for (ComparedSource comparedSource : comparedTable.getPerSourceTable().keySet()) {
            sourcesInfo.put(
                    comparedSource.getSourceId(),
                    FileUtils.getCanonicalPath(comparedSource.getSource().getPath())
            );
        }

        try (Stream<Map<String,Object>> rows =
                     ComparisonRepository.streamQueryDifferences(sourcesInfo, comparedTable.getSqlSelectDifferences())) {

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

        List<ComparedSource> comparedSourceList = new ArrayList<>();
        comparedTable.getPerSourceTable().forEach((comparedSource, sourceTable) ->
                comparedSourceList.add(comparedSource));

        List<ComparedTableColumn> identifiersComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .toList();

        List<ComparedTableColumn> comparableComparedColumns = comparedTable.getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable())
                .toList();


        Map<ComparedTableColumn, Map<ComparedSource, Object>> perComparedColumnSourceValue = new HashMap<>();

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

            ComparedSource comparedSource =
                    getComparedSourceFromSourceId(sourceId, comparedSourceList);


            if (columnExistsIn(columnNameWithoutSourceId, comparableComparedColumns)) {
                perComparedColumnSourceValue
                        .computeIfAbsent(comparedTableColumn, k -> new HashMap<>())
                        .put(comparedSource, value);
            }

            if (value != null) {
                rowDifference.addExistsOnSource(comparedSource);
            }
        }

        removeEmptySources(comparedSourceList, perComparedColumnSourceValue);

        rowDifference.addAllDifferingColumns(buildComparableColumns(perComparedColumnSourceValue));

        return rowDifference;
    }



    private static IdentifierColumn buildIdentifierColumn (ComparedTableColumn comparedTableColumn, Object value) {

        return new IdentifierColumn(comparedTableColumn, value);

    }

    private static List<ComparableColumn> buildComparableColumns (Map<ComparedTableColumn,
            Map<ComparedSource, Object>> perComparedColumnSourceValue) {

        List<ComparableColumn> comparableColumnList = new ArrayList<>();

        for (Map.Entry<ComparedTableColumn, Map<ComparedSource, Object>> entry : perComparedColumnSourceValue.entrySet()) {

            ComparedTableColumn comparedTableColumn = entry.getKey();
            Map<ComparedSource, Object> perSourceValue = entry.getValue();


            comparableColumnList.add(new ComparableColumn(comparedTableColumn, perSourceValue));

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

    private static void removeEmptySources(List<ComparedSource> comparedSourceList,
                                           Map<ComparedTableColumn, Map<ComparedSource, Object>> perComparedColumnSourceValue) {

        Set<ComparedSource> sourcesWithOnlyNulls = new HashSet<>();

        for (ComparedSource comparedSource : comparedSourceList) {
            boolean allNull = true;
            for (Map<ComparedSource, Object> perSourceValue : perComparedColumnSourceValue.values()) {
                Object val = perSourceValue.get(comparedSource);
                if (val != null) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                sourcesWithOnlyNulls.add(comparedSource);
            }
        }


        for (Map<ComparedSource, Object> perSourceValue : perComparedColumnSourceValue.values()) {
            sourcesWithOnlyNulls.forEach(perSourceValue::remove);
        }

    }

}
