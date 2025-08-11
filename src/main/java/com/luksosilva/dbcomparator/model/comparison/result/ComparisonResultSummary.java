package com.luksosilva.dbcomparator.model.comparison.result;

public class ComparisonResultSummary {

    private int totalComparedTables = 0;
    private int tablesWithDifferences = 0;
    private int totalDifferences = 0;


    public ComparisonResultSummary() {}

    public int getTotalComparedTables() {
        return totalComparedTables;
    }

    public int getTablesWithDifferences() {
        return tablesWithDifferences;
    }

    public int getTotalDifferences() {
        return totalDifferences;
    }

    public void addTotalComparedTables() {
        totalComparedTables++;
    }
    public void addTablesWithDifferences() {
        tablesWithDifferences++;
    }
    public void addTotalDifferences() {
        totalDifferences++;
    }
}
