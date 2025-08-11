package com.luksosilva.dbcomparator.viewmodel.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.result.ComparisonResultSummary;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ComparisonResultSummaryViewModel {

    private final ComparisonResultSummary model;

    private final StringProperty totalComparedTables = new SimpleStringProperty();
    private final StringProperty tablesWithDifferences = new SimpleStringProperty();
    private final StringProperty totalDifferences = new SimpleStringProperty();


    public ComparisonResultSummaryViewModel(ComparisonResultSummary comparisonResultSummary) {
        this.model = comparisonResultSummary;

        this.totalComparedTables.set(String.valueOf(model.getTotalComparedTables()));
        this.tablesWithDifferences.set(String.valueOf(model.getTablesWithDifferences()));
        this.totalDifferences.set(String.valueOf(model.getTotalDifferences()));

    }

    public StringProperty totalComparedTablesProperty() {
        return totalComparedTables;
    }

    public StringProperty tablesWithDifferencesProperty() {
        return tablesWithDifferences;
    }

    public StringProperty totalDifferencesProperty() {
        return totalDifferences;
    }
}
