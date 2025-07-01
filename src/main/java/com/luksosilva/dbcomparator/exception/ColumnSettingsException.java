package com.luksosilva.dbcomparator.exception;

import com.luksosilva.dbcomparator.model.comparison.ComparedTable;

import java.util.List;

public class ColumnSettingsException extends Exception {

    private final List<ComparedTable> tablesWithIssues;

    public ColumnSettingsException(List<ComparedTable> comparedTables) {
        this.tablesWithIssues = comparedTables;
    }

    public List<ComparedTable> getTablesWithIssues() {
        return tablesWithIssues;
    }

}
