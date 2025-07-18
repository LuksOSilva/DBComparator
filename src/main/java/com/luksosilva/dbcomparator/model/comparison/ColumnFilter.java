package com.luksosilva.dbcomparator.model.comparison;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;

public class ColumnFilter {

    private final ColumnFilterType columnFilterType;
    private String value;
    private String lowerValue;
    private String higherValue;

    public ColumnFilter(ColumnFilterType columnFilterType) {
        this.columnFilterType = columnFilterType;
    }

    public ColumnFilter(ColumnFilterType columnFilterType, String filter) {
        this.columnFilterType = columnFilterType;
        this.value = filter;
    }

    public ColumnFilter(ColumnFilterType columnFilterType, String lowerValue, String higherValue) {
        this.columnFilterType = columnFilterType;
        this.lowerValue = lowerValue;
        this.higherValue = higherValue;
    }

    public ColumnFilterType getColumnFilterType() {
        return columnFilterType;
    }

    public String getValue() {
        return value;
    }

    public String getLowerValue() {
        return lowerValue;
    }

    public String getHigherValue() {
        return higherValue;
    }

    public String getDisplayValue() {

        return switch (columnFilterType.getNumberOfArguments()){
            case 0 -> "";
            case 2 -> lowerValue + " e " + higherValue;
            default -> value;
        };

    }
}
