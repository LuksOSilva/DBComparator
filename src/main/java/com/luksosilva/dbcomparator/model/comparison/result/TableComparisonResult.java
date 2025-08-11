package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;


import java.util.ArrayList;
import java.util.List;

public class TableComparisonResult {

    private final ComparedTable comparedTable;

    private final List<RowDifference> rowDifferences = new ArrayList<>();

    public TableComparisonResult(ComparedTable comparedTable) {
        this.comparedTable = comparedTable;
    }

    public void addRowDifference(RowDifference rowDifference) {
        this.rowDifferences.add(rowDifference);
    }


    public String getTableName() {
        return comparedTable.getTableName();
    }

    public ComparedTable getComparedTable() {
        return comparedTable;
    }

    public List<RowDifference> getRowDifferences() {
        return rowDifferences;
    }
}
