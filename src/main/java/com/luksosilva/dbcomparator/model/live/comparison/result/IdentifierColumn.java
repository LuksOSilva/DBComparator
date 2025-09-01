package com.luksosilva.dbcomparator.model.live.comparison.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;

@JsonIgnoreProperties({"columnName"})
public class IdentifierColumn {

    private ComparedTableColumn comparedTableColumn;
    private Object value;

    public IdentifierColumn() { }


    @JsonCreator
    public IdentifierColumn(
            @JsonProperty("column") ComparedTableColumn comparableColumn,
            @JsonProperty("value") Object value) {
        this.comparedTableColumn = comparableColumn;
        this.value = value;
    }

    public void setComparedTableColumn(ComparedTableColumn comparedTableColumn) {
        this.comparedTableColumn = comparedTableColumn;
    }

    public void setValue(Object value) {
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
