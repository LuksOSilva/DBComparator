package com.luksosilva.dbcomparator.viewmodel.live.comparison.result;

import com.luksosilva.dbcomparator.model.live.comparison.result.ComparableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.result.IdentifierColumn;
import com.luksosilva.dbcomparator.model.live.comparison.result.RowDifference;

import java.util.*;

public class RowDifferenceViewModel {

    private final RowDifference model;


    private final List<IdentifierColumnViewModel> identifierColumnViewModels = new ArrayList<>();
    private final List<ComparableColumnViewModel> comparableColumnViewModels = new ArrayList<>();

    public RowDifferenceViewModel(RowDifference rowDifference) {
        this.model = rowDifference;


        for (IdentifierColumn identifierColumn : model.getIdentifierColumns()) {
            identifierColumnViewModels.add(new IdentifierColumnViewModel(identifierColumn));
        }
        for (ComparableColumn comparableColumn : model.getComparableColumns()) {
            comparableColumnViewModels.add(new ComparableColumnViewModel(comparableColumn));
        }
    }


    public List<IdentifierColumnViewModel> getIdentifierColumnViewModels() {
        return identifierColumnViewModels;
    }

    public List<ComparableColumnViewModel> getComparableColumnViewModels() { return comparableColumnViewModels; }



    public boolean isMissingInAnySource() {

        for (ComparableColumnViewModel comparableColumnViewModel : getComparableColumnViewModels()) {
            if (!comparableColumnViewModel.existsOnAllSources()) {
                return true;
            }
        }
        return false;
    }


    public String getExistsOn() {
        return String.join(", ", model.getExistsOnSources());
    }

}
