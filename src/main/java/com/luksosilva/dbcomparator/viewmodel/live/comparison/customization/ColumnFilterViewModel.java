package com.luksosilva.dbcomparator.viewmodel.live.comparison.customization;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ColumnFilterViewModel implements FilterViewModel {
    private final ColumnFilter model;

    private final StringProperty columnName = new SimpleStringProperty();
    private final ObjectProperty<ColumnFilterType> filterType = new SimpleObjectProperty<>();
    private final StringProperty filterTypeDescription = new SimpleStringProperty();
    private final StringProperty displayValue = new SimpleStringProperty();
    private final StringProperty filterValue = new SimpleStringProperty();
    private final StringProperty lowerValue = new SimpleStringProperty();
    private final StringProperty higherValue = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> filterDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> lowerDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> higherDate = new SimpleObjectProperty<>();

    public ColumnFilterViewModel(ColumnFilter model) {
        this.model = model;
        this.columnName.set(model.getComparedTableColumn().getColumnName());

        this.filterType.set(model.getColumnFilterType());
        this.filterTypeDescription.bind(this.filterType.map(ColumnFilterType::getDescription));

        this.filterValue.set(model.getValue());
        this.lowerValue.set(model.getLowerValue());
        this.higherValue.set(model.getHigherValue());
        this.filterDate.set(model.getDate());
        this.lowerDate.set(model.getLowerDate());
        this.higherDate.set(model.getHigherDate());

        this.displayValue.set(computeDisplayValue());
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
        int args = filterType.get().getNumberOfArguments();

        if (filterDate.get() != null || (lowerDate.get() != null && higherDate.get() != null)) {

            DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (args == 2) {
                return lowerDate.get().format(DATE_FORMAT) + " e " + higherDate.get().format(DATE_FORMAT);
            } else {
                return filterDate.get().format(DATE_FORMAT);
            }

        }
        else if (filterValue.get() != null || (lowerValue.get() != null && higherValue.get() != null)) {

            if (args == 2) {
                return lowerValue.get() + " e " + higherValue.get();
            } else {
                return filterValue.get();
            }

        }



        return "";
    }


}
