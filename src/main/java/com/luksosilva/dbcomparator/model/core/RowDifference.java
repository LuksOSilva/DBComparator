package com.luksosilva.dbcomparator.model.core;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;

import java.util.List;

public class RowDifference {

    private List<ComparedSource> existsOnSources;
    private List<IdentifierColumn> identifierColumns;
    private List<DifferingColumn> differingColumns;

}
