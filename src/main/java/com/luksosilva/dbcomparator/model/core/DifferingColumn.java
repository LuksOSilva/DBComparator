package com.luksosilva.dbcomparator.model.core;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.source.SourceTable;

import java.util.Map;

public class DifferingColumn {

    private ComparedTableColumn comparedTableColumn;
    private Map<ComparedSource, Object> perSourceValue;


}
