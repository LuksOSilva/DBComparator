package com.luksosilva.dbcomparator.builder;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.comparison.result.*;
import com.luksosilva.dbcomparator.repository.ComparisonRepository;
import com.luksosilva.dbcomparator.util.FileUtils;

import java.util.*;

public class ComparisonResultBuilder {


    public static ComparisonResult build(Comparison comparison) {

        ComparisonResult comparisonResult = new ComparisonResult();

        for (ComparedTable comparedTable : comparison.getComparedTables()) {

            comparisonResult.addTableComparisonResult(buildTableComparisonResult(comparedTable));

        }


        return comparisonResult;
    }

    private static TableComparisonResult buildTableComparisonResult(ComparedTable comparedTable) {

        TableComparisonResult tableComparisonResult = new TableComparisonResult(comparedTable);

        Map<String, String> sourcesInfo = new HashMap<>();
        for (ComparedSource comparedSource : comparedTable.getPerSourceTable().keySet()) {

            sourcesInfo.put(comparedSource.getSourceId(), FileUtils.getCanonicalPath(comparedSource.getSource().getPath()));
        }

        List<Map<String, Object>> rowDataList = ComparisonRepository.executeQueryDifferences(sourcesInfo, comparedTable.getQueryDifferences());

        for (Map<String,Object> rowData : rowDataList) {

            RowDifference rowDifference = buildRowDifference(comparedTable, rowData);

            tableComparisonResult.addRowDifference(rowDifference);
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

            if (isColumnIdentifier(columnName, identifiersComparedColumns)) {

                ComparedTableColumn comparedTableColumn =
                        getComparedTableColumnFromColumnName(columnName, identifiersComparedColumns);

                rowDifference.addIdentifierColumn(buildIdentifierColumn(comparedTableColumn, value));
                continue;
            }

            int separatorIndex = columnName.indexOf('_');
            if (separatorIndex == -1) continue;

            String sourceId = columnName.substring(0, separatorIndex);
            String columnNameWithoutSourceId = columnName.substring(separatorIndex +1);

            if (isColumnComparable(columnNameWithoutSourceId, comparableComparedColumns)) {

                ComparedTableColumn comparedTableColumn =
                        getComparedTableColumnFromColumnName(columnNameWithoutSourceId, comparableComparedColumns);

                ComparedSource comparedSource =
                        getComparedSourceFromSourceId(sourceId, comparedSourceList);


                perComparedColumnSourceValue
                        .computeIfAbsent(comparedTableColumn, k -> new HashMap<>())
                        .put(comparedSource, value);

            }

        }
        rowDifference.addAllDifferingColumns(buildDifferingColumns(perComparedColumnSourceValue));

        return rowDifference;
    }


    private static IdentifierColumn buildIdentifierColumn (ComparedTableColumn comparedTableColumn, Object value) {

        return new IdentifierColumn(comparedTableColumn, value);

    }

    private static List<DifferingColumn> buildDifferingColumns (Map<ComparedTableColumn,
            Map<ComparedSource, Object>> perComparedColumnSourceValue) {

        List<DifferingColumn> differingColumnList = new ArrayList<>();

        for (Map.Entry<ComparedTableColumn, Map<ComparedSource, Object>> entry : perComparedColumnSourceValue.entrySet()) {

            ComparedTableColumn comparedTableColumn = entry.getKey();
            Map<ComparedSource, Object> perSourceValue = entry.getValue();

            //adds differingColumn only if there are difference between sources or if there is only 1 source compared.
            if(perSourceValue.size() == 1 || columnDiffers(perSourceValue)) {
                differingColumnList.add(new DifferingColumn(comparedTableColumn, perSourceValue));
            }


        }


        return differingColumnList;
    }


    //helper methods

    private static boolean columnDiffers(Map<ComparedSource, Object> perSourceValue) {

        if (perSourceValue == null) {
            return false;
        }


        List<Object> values = new ArrayList<>();
        for (Object value : perSourceValue.values()) {
            values.add((value == null) ? "NULL" : value);
        }

        HashSet<Object> distinctValues = new HashSet<>(values);

        return distinctValues.size() > 1;


    }


    private static boolean isColumnIdentifier(String columnName, List<ComparedTableColumn> identifiersComparedColumns) {

        ComparedTableColumn comparedTableColumn = getComparedTableColumnFromColumnName(columnName, identifiersComparedColumns);

        if (comparedTableColumn != null) {
            return true;
        }
        return false;

    }

    private static boolean isColumnComparable(String columnName, List<ComparedTableColumn> comparableComparedColumns) {

        ComparedTableColumn comparedTableColumn = getComparedTableColumnFromColumnName(columnName, comparableComparedColumns);

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

}
