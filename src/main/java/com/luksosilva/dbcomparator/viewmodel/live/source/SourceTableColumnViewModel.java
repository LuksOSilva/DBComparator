package com.luksosilva.dbcomparator.viewmodel.live.source;

import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SourceTableColumnViewModel {

    private final SourceTableColumn model;

    private final SimpleStringProperty sequence = new SimpleStringProperty();
    private final SimpleStringProperty columnName = new SimpleStringProperty();
    private final SimpleStringProperty type = new SimpleStringProperty();
    private final SimpleStringProperty notNull = new SimpleStringProperty();
    private final SimpleStringProperty isPk = new SimpleStringProperty();

    public SourceTableColumnViewModel(SourceTableColumn model) {
        this.model = model;

        this.sequence.set(String.format("%02d", model.getSequence()));
        this.columnName.set(model.getColumnName());
        this.type.set(model.getType());
        this.notNull.set(model.isNotNull() ? "Y": "");
        this.isPk.set(model.isPk() ? "Y" : "");
    }

    public SourceTableColumn getModel() {
        return model;
    }

    public String getSequence() {
        return sequence.get();
    }

    public SimpleStringProperty sequenceProperty() {
        return sequence;
    }

    public String getColumnName() {
        return columnName.get();
    }

    public SimpleStringProperty columnNameProperty() {
        return columnName;
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public String isNotNull() { return notNull.get(); }

    public SimpleStringProperty notNullProperty() {
        return notNull;
    }

    public String isIsPk() {
        return isPk.get();
    }

    public SimpleStringProperty isPkProperty() {
        return isPk;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceTableColumnViewModel that = (SourceTableColumnViewModel) o;

        return getSequence().equals(that.getSequence())
                && getColumnName().equals(that.getColumnName())
                && getType().equals(that.getType())
                && isNotNull().equals(that.isNotNull())
                && isIsPk().equals(that.isIsPk());
    }

    @Override
    public int hashCode() {
        int result = getSequence().hashCode();
        result = 31 * result + getColumnName().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + isNotNull().hashCode();
        result = 31 * result + isIsPk().hashCode();
        return result;
    }
}
