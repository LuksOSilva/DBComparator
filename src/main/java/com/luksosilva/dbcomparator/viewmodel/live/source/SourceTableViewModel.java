package com.luksosilva.dbcomparator.viewmodel.live.source;

import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class SourceTableViewModel {

    private final SourceTable model;

    private final List<SourceTableColumnViewModel> sourceTableColumnViewModels = new ArrayList<>();

    private final SimpleStringProperty sourceId = new SimpleStringProperty();
    private final SimpleStringProperty tableName = new SimpleStringProperty();
    private final SimpleIntegerProperty recordCount = new SimpleIntegerProperty();
    private final SimpleIntegerProperty columnCount = new SimpleIntegerProperty();

    public SourceTableViewModel(SourceTable model) {
        this.model = model;

        this.sourceId.set(model.getSourceId());
        this.tableName.set(model.getTableName());
        this.recordCount.set(model.getRecordCount());
        this.columnCount.set(model.getSourceTableColumns().size());

        for (SourceTableColumn sourceTableColumn : model.getSourceTableColumns()) {
            sourceTableColumnViewModels.add(new SourceTableColumnViewModel(sourceTableColumn));
        }
    }

    public SourceTable getModel() {
        return model;
    }

    public List<SourceTableColumnViewModel> getSourceTableColumnViewModels() {
        return sourceTableColumnViewModels;
    }

    public String getSourceId() {
        return sourceId.get();
    }

    public SimpleStringProperty sourceIdProperty() {
        return sourceId;
    }

    public String getTableName() {
        return tableName.get();
    }

    public SimpleStringProperty tableNameProperty() {
        return tableName;
    }

    public int getRecordCount() {
        return recordCount.get();
    }

    public SimpleIntegerProperty recordCountProperty() {
        return recordCount;
    }

    public int getColumnCount() {
        return columnCount.get();
    }

    public SimpleIntegerProperty columnCountProperty() {
        return columnCount;
    }
}
