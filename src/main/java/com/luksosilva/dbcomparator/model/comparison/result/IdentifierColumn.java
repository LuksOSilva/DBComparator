package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;

public class IdentifierColumn {

    private ComparedTableColumn comparedTableColumn;
    private Object value;

    public IdentifierColumn(ComparedTableColumn comparedTableColumn, Object value) {
        this.comparedTableColumn = comparedTableColumn;
        this.value = value;
    }

    public String getColumnName() {
        return comparedTableColumn.getColumnName();
    }


    public Object getValue() {
        return value;
    }
}
