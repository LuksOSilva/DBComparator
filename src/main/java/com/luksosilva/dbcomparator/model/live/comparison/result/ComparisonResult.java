package com.luksosilva.dbcomparator.model.live.comparison.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResult {


    private List<TableComparisonResult> tableComparisonResults = new ArrayList<>();

    public ComparisonResult() {  }

    @JsonCreator
    public ComparisonResult(
            @JsonProperty("tableComparisonResults") List<TableComparisonResult> tableComparisonResults) {
        if (tableComparisonResults != null) {
            this.tableComparisonResults = tableComparisonResults;
        }
    }

    public List<TableComparisonResult> getTableComparisonResults() {
        return tableComparisonResults;
    }

    public void addTableComparisonResult(TableComparisonResult tableComparisonResult) {
        this.tableComparisonResults.add(tableComparisonResult);
    }
}
