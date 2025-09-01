package com.luksosilva.dbcomparator.model.live.comparison.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;

import java.util.Map;

@JsonIgnoreProperties({"columnName"})
public class ComparableColumn {

    private ComparedTableColumn comparedTableColumn;
    private Map<String, Object> perSourceValue;

    public ComparableColumn() {}


    @JsonCreator
    public ComparableColumn(
            @JsonProperty("column") ComparedTableColumn comparableColumn,
            @JsonProperty("perSourceValue") Map<String, Object> perSourceValue) {
        this.comparedTableColumn = comparableColumn;
        this.perSourceValue = perSourceValue;
    }

    public void setComparedTableColumn(ComparedTableColumn comparedTableColumn) {
        this.comparedTableColumn = comparedTableColumn;
    }

    public void setPerSourceValue(Map<String, Object> perSourceValue) {
        this.perSourceValue = perSourceValue;
    }

    public String getColumnName() {
        return comparedTableColumn.getColumnName();
    }

    public ComparedTableColumn getComparedTableColumn() {
        return comparedTableColumn;
    }

    public Map<String, Object> getPerSourceValue() {
        return perSourceValue;
    }
}
