package com.luksosilva.dbcomparator.model.comparison.result;

import java.util.ArrayList;
import java.util.List;

public class RowDifference {

    private final List<IdentifierColumn> identifierColumns = new ArrayList<>();
    private final List<ComparableColumn> comparableColumns = new ArrayList<>();

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
    public void addDifferingColumn(ComparableColumn comparableColumn) {
        this.comparableColumns.add(comparableColumn);
    }
    public void addAllDifferingColumns(List<ComparableColumn> comparableColumnList) {
        this.comparableColumns.addAll(comparableColumnList);
    }


}
