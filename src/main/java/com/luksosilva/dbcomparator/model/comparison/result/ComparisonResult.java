package com.luksosilva.dbcomparator.model.comparison.result;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResult {

    private ComparisonResultSummary comparisonResultSummary;
    private final List<TableComparisonResult> tableComparisonResults = new ArrayList<>();

    public ComparisonResult() {  }


    public ComparisonResultSummary getComparisonSummary() {
        return comparisonResultSummary;
    }

    public List<TableComparisonResult> getTableComparisonResults() {
        return tableComparisonResults;
    }

    public void addTableComparisonResult(TableComparisonResult tableComparisonResult) {
        this.tableComparisonResults.add(tableComparisonResult);
    }
}
