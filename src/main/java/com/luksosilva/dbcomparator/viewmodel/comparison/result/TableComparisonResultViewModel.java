package com.luksosilva.dbcomparator.viewmodel.comparison.result;


import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.result.RowDifference;
import com.luksosilva.dbcomparator.model.comparison.result.TableComparisonResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class TableComparisonResultViewModel {

    private final TableComparisonResult model;

    private final StringProperty tableName = new SimpleStringProperty();
    private final List<RowDifferenceViewModel> rowDifferenceViewModels = new ArrayList<>();


    public TableComparisonResultViewModel(TableComparisonResult tableComparisonResult) {
        this.model = tableComparisonResult;

        this.tableName.set(tableComparisonResult.getTableName());

        for (RowDifference rowDifference : tableComparisonResult.getRowDifferences()){
            rowDifferenceViewModels.add(new RowDifferenceViewModel(rowDifference));
        }
    }

    public TableComparisonResult getModel() {
        return model;
    }

    public String getTableName() {
        return tableName.get();
    }

    public List<RowDifferenceViewModel> getRowDifferenceViewModels() {
        return rowDifferenceViewModels;
    }

    ///

    public String getDiffRecordCount() {
        return String.valueOf(rowDifferenceViewModels.size());
    }

    public String getPerSourceRecordCount() {
        ComparedTable comparedTable = model.getComparedTable();

        StringBuilder perSourceRecordCount = new StringBuilder();
        comparedTable.getPerSourceTable().forEach((comparedSource, sourceTable) -> {
            perSourceRecordCount.append(comparedSource.getSourceId())
                                .append(": ")
                                .append(sourceTable.getRecordCount())
                                .append("\n");

        });

        return perSourceRecordCount.toString();
    }

    public String hasSchemaDifference() {
        return model.getComparedTable().hasSchemaDifference() ? "Y" : "N";
    }
}
