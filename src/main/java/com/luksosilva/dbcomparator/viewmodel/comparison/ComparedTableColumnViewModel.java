package com.luksosilva.dbcomparator.viewmodel.comparison;

import com.luksosilva.dbcomparator.model.comparison.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.ColumnSettings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class ComparedTableColumnViewModel {

    //model
    private final ComparedTableColumn comparedTableColumn;

    private final SimpleStringProperty columnNameProperty = new SimpleStringProperty();
    private final SimpleStringProperty columnFiltersProperty = new SimpleStringProperty();

    private final SimpleBooleanProperty identifierProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty comparableProperty = new SimpleBooleanProperty();


    private final SimpleBooleanProperty defaultIdentifierProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty defaultComparableProperty = new SimpleBooleanProperty();

    /// CONSTRUCTORS

    public ComparedTableColumnViewModel(ComparedTableColumn comparedTableColumn) {
        this.comparedTableColumn = comparedTableColumn;

        setProperties();
        setListeners();

        setDefault();
    }

    public void setProperties() {

        this.columnNameProperty.set(comparedTableColumn.getColumnName());
        this.columnFiltersProperty.set(comparedTableColumn.getColumnFilter().stream().map(ColumnFilter::getDisplayValue).toString());

        this.identifierProperty.set(comparedTableColumn.getColumnSetting().isIdentifier());
        this.comparableProperty.set(comparedTableColumn.getColumnSetting().isComparable());

    }

    public void setDefault() {
        this.defaultIdentifierProperty.set(comparedTableColumn.getColumnSetting().isIdentifier());
        this.defaultComparableProperty.set(comparedTableColumn.getColumnSetting().isComparable());
    }

    public void setListeners() {
        // Radio button-like behavior: selecting one disables the other
        identifierProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) comparableProperty.set(false);
        });

        comparableProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) identifierProperty.set(false);
        });
    }


    /// GETTERS AND SETTERS

    public ComparedTableColumn getComparedTableColumn() {
        return comparedTableColumn;
    }

    public SimpleStringProperty getColumnNameProperty() {
        return columnNameProperty;
    }

    public SimpleStringProperty getColumnFiltersProperty() {
        return columnFiltersProperty;
    }


    /// METHODS

    public boolean isAltered() {
        return (identifierProperty.get() != defaultIdentifierProperty.get())
                || (comparableProperty.get() != defaultComparableProperty.get());
    }

    public ColumnSettings getViewModelColumnSetting() {
        return new ColumnSettings(comparableProperty.get(), identifierProperty.get());
    }

//    public String getPrimaryKeyCountText() {
//        Map<ComparedSource, SourceTableColumn> map = comparedTableColumn.getPerSourceTableColumn();
//
//        long pkCount = map.values().stream().filter(SourceTableColumn::isPk).count();
//        int totalSources = comparison.getComparedSources().size();
//
//        if (pkCount == 0) return "";
//        if (pkCount == totalSources) return "Y";
//
//        return pkCount + "/" + totalSources;
//    }


}
