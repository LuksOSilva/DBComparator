package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;

public class IdentifierColumn {

    private ComparedTableColumn comparedTableColumn;
    private Object value;

    public IdentifierColumn(ComparedTableColumn comparedTableColumn, Object value) {
        this.comparedTableColumn = comparedTableColumn;
        this.value = value;
    }

    public ComparedTableColumn getComparedTableColumn() { return comparedTableColumn; }

    public Object getValue() {
        return value;
    }

    public String getColumnName() {
        return comparedTableColumn.getColumnName();
    }
}
