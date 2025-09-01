package com.luksosilva.dbcomparator.viewmodel.live.comparison.result;

import com.luksosilva.dbcomparator.model.live.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.model.live.comparison.result.TableComparisonResult;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResultViewModel {

    private final ComparisonResult model;

   // private final ComparisonResultSummaryViewModel comparisonResultSummaryViewModel;
    private final List<TableComparisonResultViewModel> tableComparisonResultViewModels = new ArrayList<>();

    public ComparisonResultViewModel(ComparisonResult comparisonResult) {
        this.model = comparisonResult;

       // this.comparisonResultSummaryViewModel = new ComparisonResultSummaryViewModel(model.getComparisonSummary());

        for (TableComparisonResult tableComparisonResult : model.getTableComparisonResults()) {
            tableComparisonResultViewModels.add(new TableComparisonResultViewModel(tableComparisonResult));
        }
    }

    public ComparisonResult getModel() {
        return model;
    }

    public List<TableComparisonResultViewModel> getTableComparisonResultViewModels() {
        return tableComparisonResultViewModels;
    }
}
