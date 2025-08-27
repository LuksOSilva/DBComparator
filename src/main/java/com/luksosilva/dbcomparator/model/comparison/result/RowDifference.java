package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RowDifference {

    private final List<IdentifierColumn> identifierColumns = new ArrayList<>();
    private final List<ComparableColumn> comparableColumns = new ArrayList<>();
    private final Set<ComparedSource> existsOnSources = new HashSet<>();

    public RowDifference() {}

    public List<IdentifierColumn> getIdentifierColumns() {
        return identifierColumns;
    }

    public List<ComparableColumn> getDifferingColumns() {
        return comparableColumns;
    }

    public void addIdentifierColumn(IdentifierColumn identifierColumn) {


        this.identifierColumns.add(identifierColumn);
    }
    public void addAllDifferingColumns(List<ComparableColumn> comparableColumnList) {
        this.comparableColumns.addAll(comparableColumnList);
    }

    public void addExistsOnSource(ComparedSource comparedSource) {
        existsOnSources.add(comparedSource);
    }

    public Set<ComparedSource> getExistsOnSources() {
        return existsOnSources;
    }
}
