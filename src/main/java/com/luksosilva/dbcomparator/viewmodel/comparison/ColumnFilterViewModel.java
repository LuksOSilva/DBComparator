package com.luksosilva.dbcomparator.viewmodel.comparison;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.comparison.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ColumnFilterViewModel {

    private final ComparedTableColumn comparedTableColumn;
    private final ColumnFilter columnFilter;

    private final StringProperty columnName = new SimpleStringProperty();
    private StringProperty filterType = new SimpleStringProperty();
    private final StringProperty filterValue = new SimpleStringProperty();

    public ColumnFilterViewModel(ComparedTableColumn comparedTableColumn, ColumnFilter columnFilter) {
        this.comparedTableColumn = comparedTableColumn;
        this.columnFilter = columnFilter;
        setProperties();
    }

    public StringProperty getColumnNameProperty() {
        return columnName;
    }

    public StringProperty getFilterTypeProperty() {
        return filterType;
    }

    public StringProperty getFilterValueProperty() {
        return filterValue;
    }

    public ComparedTableColumn getComparedTableColumn() {
        return comparedTableColumn;
    }

    public ColumnFilter getFilter() {
        return columnFilter;
    }

    private void setProperties() {
        columnName.set(comparedTableColumn.getColumnName());
        filterType.set(columnFilter.getColumnFilterType().getDescription());
        filterValue.set(columnFilter.getDisplayValue());
    }
}
