package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;

import java.util.Map;

public class ComparableColumn {

    private final ComparedTableColumn comparedTableColumn;
    private final Map<ComparedSource, Object> perSourceValue;


    public ComparableColumn(ComparedTableColumn comparedTableColumn, Map<ComparedSource, Object> perSourceValue) {
        this.comparedTableColumn = comparedTableColumn;
        this.perSourceValue = perSourceValue;
    }


    public String getColumnName() {
        return comparedTableColumn.getColumnName();
    }

    public ComparedTableColumn getComparedTableColumn() {
        return comparedTableColumn;
    }

    public Map<ComparedSource, Object> getPerSourceValue() {
        return perSourceValue;
    }
}
