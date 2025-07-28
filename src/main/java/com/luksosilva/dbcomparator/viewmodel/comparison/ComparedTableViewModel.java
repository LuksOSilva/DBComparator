package com.luksosilva.dbcomparator.viewmodel.comparison;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.customization.TableFilter;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class ComparedTableViewModel {

    //model
    private ComparedTable model;

    private final ObservableList<ComparedTableColumnViewModel> comparedTableColumnViewModels = FXCollections.observableArrayList();

    private final StringProperty tableName = new SimpleStringProperty();
    private final ObjectProperty<TableFilterViewModel> tableFilterViewModel = new SimpleObjectProperty<>();
    private final BooleanProperty usingTableFilter = new SimpleBooleanProperty();


    /// CONSTRUCTORS

    public ComparedTableViewModel(ComparedTable model) {
        this.model = model;
        this.tableName.set(model.getTableName());

        updateViewModel();
    }

    /// GETTERS AND SETTERS

    public StringProperty tableNameProperty() {
        return tableName;
    }

    public ObservableList<ComparedTableColumnViewModel> getComparedTableColumnViewModels() {
        return comparedTableColumnViewModels;
    }

    public BooleanProperty usingTableFilterProperty() {
        return usingTableFilter;
    }


    public TableFilterViewModel getTableFilterViewModel() {
        return tableFilterViewModel.get();
    }

    public ObjectProperty<TableFilterViewModel> tableFilterViewModelProperty() {
        return tableFilterViewModel;
    }

    public ComparedTable getModel() {
        return model;
    }

    public void updateViewModel() {
        this.usingTableFilter.set(model.getFilter() != null);
        if (usingTableFilter.get() && (this.tableFilterViewModel.get() == null || !this.tableFilterViewModel.get().getModel().equals(model.getFilter()))) {
            this.tableFilterViewModel.set(new TableFilterViewModel(model.getFilter()));
        } else {
            this.tableFilterViewModel.set(null);
        }

        comparedTableColumnViewModels.clear();
        for (ComparedTableColumn comparedTableColumn : model.getComparedTableColumns()) {
            comparedTableColumnViewModels.add(new ComparedTableColumnViewModel(comparedTableColumn));
        }
    }


}
