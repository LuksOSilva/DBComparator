package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;


import java.util.ArrayList;
import java.util.List;

public class TableComparisonResult {

    private ComparedTable comparedTable;
    private boolean hasDifferences;

    private final List<RowDifference> rowDifferences = new ArrayList<>();

    public TableComparisonResult(ComparedTable comparedTable) {
        this.comparedTable = comparedTable;
    }

    public void addRowDifference(RowDifference rowDifference) {
        this.rowDifferences.add(rowDifference);
    }

    public boolean hasDifferences() {
        return hasDifferences;
    }
    public void setHasDifferences(boolean hasDifferences){
        this.hasDifferences = hasDifferences;
    }

    public String getTableName() {
        return comparedTable.getTableName();
    }

    public List<RowDifference> getRowDifferences() {
        return rowDifferences;
    }
}
