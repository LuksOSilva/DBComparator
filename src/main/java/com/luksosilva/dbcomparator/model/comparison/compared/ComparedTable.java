package com.luksosilva.dbcomparator.model.comparison.compared;

import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import com.luksosilva.dbcomparator.model.comparison.customization.TableFilter;
import com.luksosilva.dbcomparator.model.comparison.customization.validation.FilterValidationResult;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.*;

public class ComparedTable {

    private Map<ComparedSource, SourceTable> perSourceTable;

    private List<ComparedTableColumn> comparedTableColumns = new ArrayList<>();

    private ColumnSettingsValidationResultType columnSettingsValidationResult = ColumnSettingsValidationResultType.NOT_VALIDATED;
    private FilterValidationResult filterValidationResult = new FilterValidationResult(FilterValidationResultType.NOT_VALIDATED);


    private TableFilter filter;

    private String sqlUserFilter;
    private String sqlSelectDifferences;

    private boolean comparisonFailed;


    public ComparedTable(Map<ComparedSource, SourceTable> perSourceTable) {
        this.perSourceTable = perSourceTable;
    }


    public String getTableName() {
        return getPerSourceTable().values().stream()
                .findFirst()
                .map(SourceTable::getTableName)
                .orElse(null);
    }


    public List<ComparedTableColumn> getComparedTableColumns() {
        return comparedTableColumns;
    }

    public List<ComparedTableColumn> getOrderedComparedTableColumns() {

        return comparedTableColumns.stream()
                .sorted((tableColumn1, tableColumn2) -> {

                    // Get minimum sequence for col1
                    int minSeq1 = tableColumn1.getPerSourceTableColumn().values().stream()
                            .mapToInt(SourceTableColumn::getSequence)
                            .min()
                            .orElse(Integer.MAX_VALUE);

                    // Get minimum sequence for col2
                    int minSeq2 = tableColumn2.getPerSourceTableColumn().values().stream()
                            .mapToInt(SourceTableColumn::getSequence)
                            .min()
                            .orElse(Integer.MAX_VALUE);

                    // Primary sort: by min sequence
                    int cmp = Integer.compare(minSeq1, minSeq2);
                    if (cmp != 0) return cmp;

                    // Secondary sort: by column name (so tie is stable)
                    return tableColumn1.getColumnName().compareTo(tableColumn2.getColumnName());
                })
                .toList();
    }


    public Map<ComparedSource, SourceTable> getPerSourceTable() {
        return perSourceTable;
    }


    public String getSqlSelectDifferences() {
        return sqlSelectDifferences;
    }

    public void setSqlSelectDifferences(String sqlSelectDifferences) {
        this.sqlSelectDifferences = sqlSelectDifferences;
    }

    public String getSqlUserFilter() {
        return sqlUserFilter;
    }

    public void setSqlUserFilter(String sqlUserFilter) {
        this.sqlUserFilter = sqlUserFilter;
    }

    public TableFilter getFilter() {
        return filter;
    }

    public void setFilter(TableFilter filter) {
        this.filter = filter;
    }

    public void removeFilter() {
        this.filter = null;
    }

    public void setComparisonFailed(boolean comparisonFailed) {
        this.comparisonFailed = comparisonFailed;
    }

    public boolean hasSchemaDifference() {

        Collection<SourceTable> sourceTables = perSourceTable.values();
        if (sourceTables.isEmpty()) {
            return false;
        }
        SourceTable first = sourceTables.iterator().next();
        return !sourceTables.stream()
                .allMatch(first::equalSchema);
    }

    public boolean hasRecordCountDifference() {

        Collection<SourceTable> sourceTables = perSourceTable.values();
        if (sourceTables.isEmpty()) {
            return false;
        }
        SourceTable first = sourceTables.iterator().next();
        return !sourceTables.stream()
                .allMatch(first::equalRecordCount);
    }

    public boolean hasTableFilter() {
        return getFilter() != null;
    }

    public boolean hasColumnFilter() {
        return getComparedTableColumns().stream().anyMatch(ComparedTableColumn::hasFilter);
    }

    public boolean hasFilter() {
        return hasTableFilter() || hasColumnFilter();
    }

    public int getTotalRecordCount() {
        int totalRecordCount = 0;

        for (SourceTable sourceTable : perSourceTable.values()) {
            totalRecordCount += sourceTable.getRecordCount();
        }

        return totalRecordCount;

    }

    /// COLUMN SETTINGS VALIDATION

    public ColumnSettingsValidationResultType getColumnSettingsValidationResult() {
        return columnSettingsValidationResult;
    }

    public void setColumnSettingsValidationResult(ColumnSettingsValidationResultType columnSettingsValidationResult) {
        this.columnSettingsValidationResult = columnSettingsValidationResult;
    }

    public void clearColumnSettingValidation() {
        columnSettingsValidationResult = ColumnSettingsValidationResultType.NOT_VALIDATED;
    }

    public boolean isColumnSettingsValid() {
        if (columnSettingsValidationResult == null) return false;

        return columnSettingsValidationResult == ColumnSettingsValidationResultType.VALID;
    }

    public boolean isColumnSettingsInvalid() {
        if (columnSettingsValidationResult == null) return false;

        return columnSettingsValidationResult != ColumnSettingsValidationResultType.VALID
                && columnSettingsValidationResult != ColumnSettingsValidationResultType.NOT_VALIDATED;
    }

    /// FILTER VALIDATION

    public FilterValidationResult getFilterValidationResult() {
        return filterValidationResult;
    }

    public void setFilterValidationResult(FilterValidationResult filterValidationResult) {
        this.filterValidationResult = filterValidationResult;
    }


    public void clearFilterValidation() {
        this.filterValidationResult = new FilterValidationResult(FilterValidationResultType.NOT_VALIDATED);
    }


}
