package com.luksosilva.dbcomparator.controller.dialogs;

import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFilterDialogController {

    private Stage stage;

    @FXML
    private ComboBox<String> tableComboBox;
    @FXML
    private ComboBox<String> columnComboBox;
    @FXML
    private TextField filterTextField;


    private final Map<ComparedTableColumn, String> filterMap = new HashMap<>();
    private List<ComparedTable> comparedTables;

    private final ObservableList<String> fullTableList = FXCollections.observableArrayList();
    private FilteredList<String> filteredTableList = new FilteredList<>(fullTableList, s -> true);

    private final ObservableList<String> fullColumnList = FXCollections.observableArrayList();
    private FilteredList<String> filteredColumnList = new FilteredList<>(fullColumnList, s -> true);

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setComparedTables(List<ComparedTable> comparedTables) {
        this.comparedTables = comparedTables;
    }

    public void initializeDialog(List<ComparedTable> comparedTables) {
        setComparedTables(comparedTables);
        constructTableComboBox();
    }

    private void constructTableComboBox() {
        //add tables
        fullTableList.clear();
        for (ComparedTable table : comparedTables) {
            fullTableList.add(table.getTableName());
        }


        tableComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            tableComboBox.show();
            Platform.runLater(() -> {
                if (newText.equals(tableComboBox.getValue())) return;
                filteredTableList.setPredicate(name -> name.toLowerCase().contains(newText.toLowerCase()));
            });
        });


        tableComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            ComparedTable comparedTable = getComparedTableByTableName(newVal);
            if (comparedTable == null) {
                columnComboBox.setDisable(true);
                return;
            }
            columnComboBox.setDisable(false);
            constructColumnComboBox(comparedTable);
        });


        tableComboBox.setItems(filteredTableList);
    }

    private void constructColumnComboBox(ComparedTable comparedTable) {
        fullColumnList.clear();
        filteredColumnList.clear();


        for (ComparedTableColumn column : comparedTable.getComparedTableColumns()) {
            fullColumnList.add(column.getColumnName());
        }

        columnComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            columnComboBox.show();
            Platform.runLater(() -> {
                if (newText.equals(columnComboBox.getValue())) return;
                filteredColumnList.setPredicate(name -> name.toLowerCase().contains(newText.toLowerCase()));
            });
        });

        columnComboBox.setItems(filteredColumnList);
    }

    private ComparedTable getComparedTableByTableName(String tableName) {
        return comparedTables.stream()
                .filter(t -> t.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
    }

    @FXML
    private void onOkClicked() {
        String selectedTableName = tableComboBox.getValue();
        String selectedColumnName = columnComboBox.getValue();
        String filter = filterTextField.getText();

        if (selectedTableName == null || selectedColumnName == null || filter == null || filter.isBlank()) {
            return;
        }

        // Find the ComparedTableColumn object
        ComparedTable selectedTable = comparedTables.stream()
                .filter(t -> t.getTableName().equals(selectedTableName))
                .findFirst()
                .orElse(null);

        if (selectedTable != null) {
            ComparedTableColumn column = selectedTable.getComparedTableColumns().stream()
                    .filter(c -> c.getColumnName().equals(selectedColumnName))
                    .findFirst()
                    .orElse(null);

            if (column != null) {
                filterMap.put(column, filter);
            }
        }

        stage.close();
    }

    @FXML
    private void onCancelClicked() {
        filterMap.clear(); // Optional: clear to return empty if canceled
        stage.close();
    }

    public Map<ComparedTableColumn, String> getFilterMap() {
        return filterMap;
    }
}