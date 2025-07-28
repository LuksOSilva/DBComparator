package com.luksosilva.dbcomparator.model.comparison.customization;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;

import java.util.Objects;

public class ColumnFilter implements Filter {

    private final ComparedTableColumn comparedTableColumn;
    private final ColumnFilterType columnFilterType;
    private String value;
    private String lowerValue;
    private String higherValue;

    @Override
    public void apply() {
        comparedTableColumn.addColumnFilter(this);
    }

    public ColumnFilter(ColumnFilter sampleColumnFilter, ComparedTableColumn comparedTableColumn) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = sampleColumnFilter.columnFilterType;
        this.value = sampleColumnFilter.value;
        this.lowerValue = sampleColumnFilter.lowerValue;
        this.higherValue = sampleColumnFilter.higherValue;
    }

    public ColumnFilter(ComparedTableColumn comparedTableColumn, ColumnFilterType columnFilterType) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = columnFilterType;
    }

    public ColumnFilter(ComparedTableColumn comparedTableColumn, ColumnFilterType columnFilterType, String filter) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = columnFilterType;
        this.value = filter;
    }

    public ColumnFilter(ComparedTableColumn comparedTableColumn, ColumnFilterType columnFilterType, String lowerValue, String higherValue) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = columnFilterType;
        this.lowerValue = lowerValue;
        this.higherValue = higherValue;
    }

    public ComparedTableColumn getComparedTableColumn() {
        return comparedTableColumn;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnFilter that)) return false;
        return comparedTableColumn.equals(that.comparedTableColumn) &&
                columnFilterType.equals(that.columnFilterType) &&
                Objects.equals(value, that.value) &&
                Objects.equals(lowerValue, that.lowerValue) &&
                Objects.equals(higherValue, that.higherValue);
    }

    public boolean equalsIgnoreColumn(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnFilter that)) return false;
        return columnFilterType.equals(that.columnFilterType) &&
                Objects.equals(value, that.value) &&
                Objects.equals(lowerValue, that.lowerValue) &&
                Objects.equals(higherValue, that.higherValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnFilterType, value, lowerValue, higherValue);
    }
}
