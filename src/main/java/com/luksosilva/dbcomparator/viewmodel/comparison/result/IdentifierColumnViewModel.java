package com.luksosilva.dbcomparator.viewmodel.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.result.IdentifierColumn;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class IdentifierColumnViewModel {

    private final IdentifierColumn model;

    private final StringProperty columnName = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();


    public IdentifierColumnViewModel(IdentifierColumn identifierColumn) {
        this.model = identifierColumn;

        this.columnName.set(model.getColumnName());

        this.value.set(model.getValue() == null ? "NULL" : model.getValue().toString());
    }

    public IdentifierColumn getModel() {
        return model;
    }

    public String getColumnName() {
        return columnName.get();
    }


    public String getValue() {
        return value.get();
    }

    public StringProperty columnNameProperty() {
        return columnName;
    }

    public StringProperty getDisplayValue() {
        return value;
    }
}
