package com.luksosilva.dbcomparator.model.comparison;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import com.luksosilva.dbcomparator.model.source.SourceTable;

import java.util.*;

public class ComparedTable {

    private Map<ComparedSource, SourceTable> perSourceTable;

    private List<ComparedTableColumn> comparedTableColumns = new ArrayList<>();

    private ColumnSettingsValidationResultType columnSettingsValidationResult;

    private String queryDifferences;




    public ComparedTable(Map<ComparedSource, SourceTable> perSourceTable) {
        this.perSourceTable = perSourceTable;
    }


    public String getTableName() {
        return getPerSourceTable().values().stream()
                .findFirst()
                .map(SourceTable::getTableName)
                .orElse(null);
    }

    @JsonIgnore
    public List<ComparedTableColumn> getComparedTableColumns() {
        return comparedTableColumns;
    }

    @JsonIgnore
    public Map<ComparedSource, SourceTable> getPerSourceTable() {
        return perSourceTable;
    }

    @JsonIgnore
    public String getQueryDifferences() {
        return queryDifferences;
    }

    public void setQueryDifferences(String queryDifferences) {
        this.queryDifferences = queryDifferences;
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


    public ColumnSettingsValidationResultType getColumnSettingsValidationResult() {
        return columnSettingsValidationResult;
    }

    public void setColumnSettingsValidationResult(ColumnSettingsValidationResultType columnSettingsValidationResult) {
        this.columnSettingsValidationResult = columnSettingsValidationResult;
    }

    public void clearColumnSettingValidation() {
        columnSettingsValidationResult = null;
    }

    public boolean isColumnSettingsValid() {
        if (columnSettingsValidationResult == null) return false;

        return columnSettingsValidationResult == ColumnSettingsValidationResultType.VALID;
    }

    public boolean isColumnSettingsInvalid() {
        if (columnSettingsValidationResult == null) return false;

        return columnSettingsValidationResult != ColumnSettingsValidationResultType.VALID;
    }


}
