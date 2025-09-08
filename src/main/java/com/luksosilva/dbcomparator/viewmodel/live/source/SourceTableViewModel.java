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

    private final SimpleStringProperty tableName = new SimpleStringProperty();
    private final SimpleIntegerProperty recordCount = new SimpleIntegerProperty();

    public SourceTableViewModel(SourceTable model) {
        this.model = model;

        this.tableName.set(model.getTableName());
        this.recordCount.set(model.getRecordCount());

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
}
