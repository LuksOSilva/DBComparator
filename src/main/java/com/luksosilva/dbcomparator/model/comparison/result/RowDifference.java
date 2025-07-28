package com.luksosilva.dbcomparator.model.comparison.result;

import java.util.ArrayList;
import java.util.List;

public class RowDifference {

    private final List<IdentifierColumn> identifierColumns = new ArrayList<>();
    private final List<DifferingColumn> differingColumns = new ArrayList<>();


    public List<IdentifierColumn> getIdentifierColumns() {
        return identifierColumns;
    }

    public List<DifferingColumn> getDifferingColumns() {
        return differingColumns;
    }

    public void addIdentifierColumn(IdentifierColumn identifierColumn) {
        this.identifierColumns.add(identifierColumn);
    }
    public void addDifferingColumn(DifferingColumn differingColumn) {
        this.differingColumns.add(differingColumn);
    }
    public void addAllDifferingColumns(List<DifferingColumn> differingColumnList) {
        this.differingColumns.addAll(differingColumnList);
    }


}
