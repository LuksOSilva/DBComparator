package com.luksosilva.dbcomparator.exception;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;

import java.util.List;

public class FilterException extends RuntimeException {

    private final List<ComparedTable> tablesWithIssues;

    public FilterException(List<ComparedTable> comparedTables) {
        this.tablesWithIssues = comparedTables;
    }

    public List<ComparedTable> getTablesWithIssues() {
        return tablesWithIssues;
    }
}
