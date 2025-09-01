package com.luksosilva.dbcomparator.model.live.comparison.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;


import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({
        "tableName"
})
public class TableComparisonResult {

    private ComparedTable comparedTable;
    private List<RowDifference> rowDifferences = new ArrayList<>();

    public TableComparisonResult() { }

    public TableComparisonResult(ComparedTable comparedTable) {
        this.comparedTable = comparedTable;
    }

    @JsonCreator
    public TableComparisonResult(
            @JsonProperty("comparedTable") ComparedTable comparedTable,
            @JsonProperty("rowDifferences") List<RowDifference> rowDifferences) {
        this.comparedTable = comparedTable;
        if (rowDifferences != null) {
            this.rowDifferences = rowDifferences;
        }
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
