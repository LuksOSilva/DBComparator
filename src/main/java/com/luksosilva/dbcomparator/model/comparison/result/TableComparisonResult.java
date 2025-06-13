package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.core.RowDifference;

import java.util.List;

public class TableComparisonResult {

    private ComparedTable comparedTable;
    private boolean hasDifferences;

    private List<RowDifference> rowDifferenceList;

}
