package com.luksosilva.dbcomparator.viewmodel.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.result.ComparableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparableColumnViewModel {

    private ComparableColumn model;

    private final StringProperty columnName = new SimpleStringProperty();
    private Map<String, String> perSourceValue;


    public ComparableColumnViewModel() {}

    public ComparableColumnViewModel(ComparableColumn comparableColumn) {
        this.model = comparableColumn;

        this.columnName.set(model.getColumnName());

        perSourceValue = model.getPerSourceValue().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getSourceId(),
                        entry -> entry.getValue() == null ? "NULL" : entry.getValue().toString()
                ));
    }



    public ComparableColumn getModel() {
        return model;
    }

    public String getColumnName() {
        return columnName.get();
    }

    public StringProperty columnNameProperty() {
        return columnName;
    }

    public Map<String, String> getPerSourceValue() {
        return perSourceValue;
    }

    ///

    public boolean existsOnAllSources() {
        ComparedTableColumn comparedTableColumn = model.getComparedTableColumn();
        List<ComparedSource> comparedSourceList = comparedTableColumn.getPerSourceTableColumn().keySet().stream().toList();

        for (ComparedSource comparedSource : comparedSourceList) {
            if (!perSourceValue.containsKey(comparedSource.getSourceId())) {
                return false;
            }
        }
        return true;
    }

    public boolean allValuesAreEqual() {

        return perSourceValue.values().stream()
                .distinct()
                .count() <= 1;

    }

    public StringProperty getDisplayValue() {
        StringBuilder displayValue = new StringBuilder();

        perSourceValue.forEach((sourceId, value) -> {
            displayValue.append(sourceId)
                    .append(": ")
                    .append(value)
                    .append("\n");
        });

        return new SimpleStringProperty(displayValue.toString());
    }
}
