package com.luksosilva.dbcomparator.viewmodel.comparison;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnFilter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;

public class ColumnFilterViewModel implements FilterViewModel {
    private final ColumnFilter model;

    private final StringProperty columnName = new SimpleStringProperty();
    private final ObjectProperty<ColumnFilterType> filterType = new SimpleObjectProperty<>();
    private final StringProperty filterTypeDescription = new SimpleStringProperty();
    private final StringProperty displayValue = new SimpleStringProperty();
    private final StringProperty lowerValue = new SimpleStringProperty();
    private final StringProperty higherValue = new SimpleStringProperty();

    public ColumnFilterViewModel(ColumnFilter model) {
        this.model = model;
        this.columnName.set(model.getComparedTableColumn().getColumnName());

        this.filterType.set(model.getColumnFilterType());
        this.filterTypeDescription.bind(this.filterType.map(ColumnFilterType::getDescription));

        this.displayValue.set(computeDisplayValue());
        this.lowerValue.set(model.getLowerValue());
        this.higherValue.set(model.getHigherValue());

    }

    public Optional<StringProperty> columnNameProperty() {
        return Optional.of(columnName);
    }

    public ObjectProperty filterTypeProperty() {
        return filterType;
    }
    public Optional<StringProperty> filterTypeDescriptionProperty() {
        return Optional.of(filterTypeDescription);
    }

    public StringProperty displayValueProperty() {
        return displayValue;
    }

    public StringProperty lowerValueProperty() {
        return lowerValue;
    }

    public StringProperty higherValueProperty() {
        return higherValue;
    }

    public ColumnFilter getModel() {
        return model;
    }

    /// METHODS

    public void updateDisplayValue() {
        this.displayValue.set(computeDisplayValue());
    }

    private String computeDisplayValue() {
        return switch (filterType.get().getNumberOfArguments()) {
            case 0 -> "";
            case 2 -> lowerValue.get() + " e " + higherValue.get();
            default -> model.getValue();
        };
    }


}
