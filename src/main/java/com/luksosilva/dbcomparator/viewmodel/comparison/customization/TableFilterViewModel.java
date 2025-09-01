package com.luksosilva.dbcomparator.viewmodel.comparison.customization;

import com.luksosilva.dbcomparator.model.live.comparison.customization.TableFilter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;

public class TableFilterViewModel implements FilterViewModel {

    private final TableFilter model;

    private final StringProperty userWrittenFilter = new SimpleStringProperty();
    private final StringProperty displayValue = new SimpleStringProperty();


    public TableFilterViewModel(TableFilter model) {
        this.model = model;
        this.userWrittenFilter.set(model.getUserWrittenFilter());

        this.displayValue.set(computeDisplayValue());
    }

    public StringProperty userWrittenFilterProperty() {
        return userWrittenFilter;
    }

    public StringProperty displayValueProperty() {
        return displayValue;
    }

    @Override
    public Optional<StringProperty> columnNameProperty() {
        return Optional.empty();
    }

    @Override
    public Optional<StringProperty> filterTypeDescriptionProperty() {
        return Optional.empty();
    }

    public TableFilter getModel() {
        return model;
    }

    /// METHDOS

    private String computeDisplayValue() {
        return "WHERE " + userWrittenFilter.get();
    }
}
