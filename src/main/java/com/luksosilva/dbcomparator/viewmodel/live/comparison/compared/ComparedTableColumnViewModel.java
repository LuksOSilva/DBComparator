package com.luksosilva.dbcomparator.viewmodel.live.comparison.compared;

import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.customization.ColumnFilterViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ComparedTableColumnViewModel {

    //model
    private final ComparedTableColumn model;

    private ObservableList<ColumnFilter> columnFilters;
    private final ObservableList<ColumnFilterViewModel> columnFilterViewModels = FXCollections.observableArrayList();

    private final StringProperty columnName = new SimpleStringProperty();
    private final BooleanProperty isPkAnySource = new SimpleBooleanProperty();
    private final BooleanProperty hasSchemaDifference = new SimpleBooleanProperty();
    private final BooleanProperty existsOnAllSources = new SimpleBooleanProperty();
    private final BooleanProperty isIdentifier = new SimpleBooleanProperty();
    private final BooleanProperty isComparable = new SimpleBooleanProperty();


    /// CONSTRUCTORS

    public ComparedTableColumnViewModel(ComparedTableColumn model) {
        this.model = model;
        this.columnName.set(model.getColumnName());
        this.isPkAnySource.set(model.isPkAnySource());
        this.hasSchemaDifference.set(model.hasSchemaDifference());
        this.existsOnAllSources.set(model.existsOnAllSources());

        if (model.getColumnSetting() != null) {
            this.isIdentifier.set(model.getColumnSetting().isIdentifier());
            this.isComparable.set(model.getColumnSetting().isComparable());

            this.isIdentifier.addListener((obs, oldValue, newValue) -> {
                model.getColumnSetting().setIdentifier(newValue);
                if (newValue) {
                    this.isComparable.set(false);
                }

            });
            this.isComparable.addListener((obs, oldValue, newValue) -> {
                model.getColumnSetting().setComparable(newValue);
                if (newValue) {
                    this.isIdentifier.set(false);
                }
            });
        }



        this.columnFilters = FXCollections.observableList(model.getColumnFilters());
        for (ColumnFilter columnFilter : model.getColumnFilters()) {
            columnFilterViewModels.add(new ColumnFilterViewModel(columnFilter));
        }
    }


    public String getColumnName() {
        return columnName.get();
    }

    public StringProperty columnNameProperty() {
        return columnName;
    }

    public boolean isPkAnySource() {
        return isPkAnySource.get();
    }

    public BooleanProperty isPkAnySourceProperty() {
        return isPkAnySource;
    }

    public boolean isHasSchemaDifference() {
        return hasSchemaDifference.get();
    }

    public BooleanProperty hasSchemaDifferenceProperty() {
        return hasSchemaDifference;
    }

    public boolean existsOnAllSources() {
        return existsOnAllSources.get();
    }

    public BooleanProperty existsOnAllSourcesProperty() {
        return existsOnAllSources;
    }

    public boolean isIdentifier() {
        return isIdentifier.get();
    }

    public BooleanProperty isIdentifierProperty() {
        return isIdentifier;
    }

    public boolean isComparable() {
        return isComparable.get();
    }

    public BooleanProperty isComparableProperty() {
        return isComparable;
    }

    public SimpleStringProperty isPkAnySourceStringProperty() {
        return new SimpleStringProperty(isPkAnySource.get() ? "Y" : "");
    }


    /// GETTERS AND SETTERS




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








}
