package com.luksosilva.dbcomparator.viewmodel.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.result.ComparableColumn;
import com.luksosilva.dbcomparator.model.comparison.result.IdentifierColumn;
import com.luksosilva.dbcomparator.model.comparison.result.RowDifference;

import java.util.*;
import java.util.stream.Collectors;

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



    public boolean isMissingInAnySource() {

        for (ComparableColumnViewModel comparableColumnViewModel : getComparableColumnViewModels()) {
            if (!comparableColumnViewModel.existsOnAllSources()) {
                return true;
            }
        }
        return false;
    }


    public String getExistsOn() {
        return model.getExistsOnSources().stream()
                .map(ComparedSource::getSourceId)
                .collect(Collectors.joining(", "));
    }

}
