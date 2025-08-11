package com.luksosilva.dbcomparator.viewmodel.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.result.ComparableColumn;
import com.luksosilva.dbcomparator.model.comparison.result.IdentifierColumn;
import com.luksosilva.dbcomparator.model.comparison.result.RowDifference;

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
        for (ComparableColumn comparableColumn : model.getDifferingColumns()) {
            comparableColumnViewModels.add(new ComparableColumnViewModel(comparableColumn));
        }
    }


    public List<IdentifierColumnViewModel> getIdentifierColumnViewModels() {
        return identifierColumnViewModels;
    }

    public List<ComparableColumnViewModel> getComparableColumnViewModels() { return comparableColumnViewModels; }

    ///


    public boolean isMissingInAnySource() {

        for (ComparableColumnViewModel comparableColumnViewModel : getComparableColumnViewModels()) {
            if (!comparableColumnViewModel.existsOnAllSources()) {
                return true;
            }
        }
        return false;
    }

    private boolean isValuePresent(String v) {
        return v != null && !v.isBlank() && !"NULL".equalsIgnoreCase(v);
    }

}
