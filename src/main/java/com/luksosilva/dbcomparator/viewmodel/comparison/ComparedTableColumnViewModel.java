package com.luksosilva.dbcomparator.viewmodel.comparison;

import com.luksosilva.dbcomparator.model.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ComparedTableColumnViewModel {

    //model
    private final ComparedTableColumn model;

    private ObservableList<ColumnFilter> columnFilters;
    private final ObservableList<ColumnFilterViewModel> columnFilterViewModels = FXCollections.observableArrayList();

    private final StringProperty columnName = new SimpleStringProperty();
    private final BooleanProperty isIdentifier = new SimpleBooleanProperty();
    private final BooleanProperty isComparable = new SimpleBooleanProperty();


    /// CONSTRUCTORS

    public ComparedTableColumnViewModel(ComparedTableColumn model) {
        this.model = model;
        this.columnName.set(model.getColumnName());

        this.isIdentifier.set(model.getColumnSetting().isIdentifier());
        this.isComparable.set(model.getColumnSetting().isComparable());

        this.columnFilters = FXCollections.observableList(model.getColumnFilters());

        for (ColumnFilter columnFilter : model.getColumnFilters()) {
            columnFilterViewModels.add(new ColumnFilterViewModel(columnFilter));
        }
    }


    /// GETTERS AND SETTERS

    public StringProperty columnNameProperty() {
        return columnName;
    }

    public ObservableList<ColumnFilterViewModel> getColumnFilterViewModels() {
        return columnFilterViewModels;
    }

    public BooleanProperty getIsIdentifier() {
        return isIdentifier;
    }

    public BooleanProperty getIsComparable() {
        return isComparable;
    }

    public ComparedTableColumn getModel() {
        return model;
    }


    public void updateViewModel() {
        this.isIdentifier.set(model.getColumnSetting().isIdentifier());
        this.isComparable.set(model.getColumnSetting().isComparable());

        this.columnFilters = FXCollections.observableList(model.getColumnFilters());

        for (ColumnFilter columnFilter : model.getColumnFilters()) {
            columnFilterViewModels.add(new ColumnFilterViewModel(columnFilter));
        }
    }






}
