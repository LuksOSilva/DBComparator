package com.luksosilva.dbcomparator.viewmodel.comparison;

import com.luksosilva.dbcomparator.controller.comparisonScreens.ColumnSettingsScreenController;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;

import java.util.List;

public class ComparedTableViewModel {

    //model
    private ComparedTable comparedTable;

    private List<ComparedTableColumnViewModel> comparedTableColumnViewModels;

    /// CONSTRUCTORS

    public ComparedTableViewModel(ComparedTable comparedTable) {
        this.comparedTable = comparedTable;

        this.comparedTableColumnViewModels = comparedTable.getComparedTableColumns().stream()
                .map(ComparedTableColumnViewModel::new)
                .toList();
    }

    /// GETTERS AND SETTERS

    public String getTableName() {
        return comparedTable.getTableName();
    }

    public List<ComparedTableColumnViewModel> getComparedTableColumnViewModels() {
        return comparedTableColumnViewModels;
    }

    /// METHODS


}
