package com.luksosilva.dbcomparator.model.live.comparison.customization;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;

import java.time.LocalDateTime;
import java.util.Objects;

public class ColumnFilter implements Filter {

    private final ComparedTableColumn comparedTableColumn;
    private final ColumnFilterType columnFilterType;
    private String value;
    private String lowerValue;
    private String higherValue;
    private LocalDateTime date;
    private LocalDateTime lowerDate;
    private LocalDateTime higherDate;

    @Override
    public void apply() {
        comparedTableColumn.addColumnFilter(this);
    }



    /// constructors

    public ColumnFilter(ColumnFilter sampleColumnFilter, ComparedTableColumn comparedTableColumn) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = sampleColumnFilter.columnFilterType;
        this.value = sampleColumnFilter.value;
        this.lowerValue = sampleColumnFilter.lowerValue;
        this.higherValue = sampleColumnFilter.higherValue;
        this.date = sampleColumnFilter.date;
        this.lowerDate = sampleColumnFilter.lowerDate;
        this.higherDate = sampleColumnFilter.higherDate;
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

    public ColumnFilter(ComparedTableColumn comparedTableColumn, ColumnFilterType columnFilterType,
                        String lowerValue, String higherValue) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = columnFilterType;
        this.lowerValue = lowerValue;
        this.higherValue = higherValue;
    }

    public ColumnFilter(ComparedTableColumn comparedTableColumn, ColumnFilterType columnFilterType, LocalDateTime date) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = columnFilterType;
        this.date = date;
    }

    public ColumnFilter(ComparedTableColumn comparedTableColumn, ColumnFilterType columnFilterType,
                        LocalDateTime lowerDate, LocalDateTime higherDate) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilterType = columnFilterType;
        this.lowerDate = lowerDate;
        this.higherDate = higherDate;
    }

    /// getters and setters

    public ComparedTable getComparedTable() { return  comparedTableColumn.getComparedTable(); }

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

    public LocalDateTime getDate() { return date; }

    public LocalDateTime getLowerDate() { return lowerDate; }

    public LocalDateTime getHigherDate() { return higherDate; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnFilter that)) return false;
        return comparedTableColumn.equals(that.comparedTableColumn) &&
                columnFilterType.equals(that.columnFilterType) &&
                Objects.equals(value, that.value) &&
                Objects.equals(lowerValue, that.lowerValue) &&
                Objects.equals(higherValue, that.higherValue) &&
                Objects.equals(date, that.date) &&
                Objects.equals(lowerDate, that.lowerDate) &&
                Objects.equals(higherDate, that.higherDate);
    }

    public boolean equalsIgnoreColumn(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnFilter that)) return false;
        return columnFilterType.equals(that.columnFilterType) &&
                Objects.equals(value, that.value) &&
                Objects.equals(lowerValue, that.lowerValue) &&
                Objects.equals(higherValue, that.higherValue) &&
                Objects.equals(date, that.date) &&
                Objects.equals(lowerDate, that.lowerDate) &&
                Objects.equals(higherDate, that.higherDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnFilterType, value, lowerValue, higherValue, date, lowerDate, higherDate);
    }
}
