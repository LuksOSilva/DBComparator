package com.luksosilva.dbcomparator.controller.dialogs;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.comparison.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.*;

public class AddFilterDialogController {



    private Stage stage;


    @FXML
    public ComboBox<String> tableComboBox;
    @FXML
    public ComboBox<String> columnComboBox;
    @FXML
    public ComboBox<String> filterTypeComboBox;

    @FXML
    public Label filterTipLabel;

    @FXML
    public HBox OneTextFieldHBox;
    @FXML
    public TextField filterTextField;

    @FXML
    public HBox TwoTextFieldsHBox;
    @FXML
    public TextField lowerValueTextField;
    @FXML
    public TextField higherValueTextField;

    @FXML
    public CheckBox addToAllTablesCheckBox;



    private final Map<ComparedTableColumn, List<ColumnFilter>> filterMap = new HashMap<>();
    private List<ComparedTable> comparedTables;

    private ComparedTable selectedComparedTable;
    private ComparedTableColumn selectedComparedTableColumn;
    private ColumnFilterType selectedFilterType;

    private final ObservableList<String> fullTableList = FXCollections.observableArrayList();
    private final FilteredList<String> filteredTableList = new FilteredList<>(fullTableList, s -> true);

    private final ObservableList<String> fullColumnList = FXCollections.observableArrayList();
    private final FilteredList<String> filteredColumnList = new FilteredList<>(fullColumnList, s -> true);

    private final ObservableList<String> applicableColumnFilterTypeList = FXCollections.observableArrayList();

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
                selectedComparedTable = null;
                return;
            }
            columnComboBox.setDisable(false);
            selectedComparedTable = comparedTable;
            constructColumnComboBox(comparedTable);
        });


        tableComboBox.setItems(filteredTableList);
    }

    private void constructColumnComboBox(ComparedTable comparedTable) {
        fullColumnList.clear();
        filteredColumnList.clear();

        for (ComparedTableColumn column : comparedTable.getComparedTableColumns()) {
            fullColumnList.add(column.getColumnName() + " " + column.getColumnTypes());
        }

        columnComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            columnComboBox.show();
            Platform.runLater(() -> {
                if (newText.equals(columnComboBox.getValue())) return;
                filteredColumnList.setPredicate(name -> {
                    String columnNameWithoutType = name.replaceAll("\\s*\\[.*]$", "");

                    return columnNameWithoutType.toLowerCase().contains(newText.toLowerCase());
                });
            });
        });

        columnComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) return;
            String columnNameWithoutType = newVal.replaceAll("\\s*\\[.*]$", "");

            ComparedTableColumn comparedTableColumn = getComparedTableColumnByColumnName(comparedTable, columnNameWithoutType);
            if (comparedTableColumn == null) {
                filterTypeComboBox.setDisable(true);
                selectedComparedTableColumn = null;
                return;
            }
            filterTypeComboBox.setDisable(false);
            selectedComparedTableColumn = comparedTableColumn;
            constructFilterTypeComboBox(comparedTableColumn);
        });

        columnComboBox.setItems(filteredColumnList);
    }

    private void constructFilterTypeComboBox(ComparedTableColumn comparedTableColumn) {
        applicableColumnFilterTypeList.clear();


        List<String> columnTypes = comparedTableColumn.getColumnTypes();
        List<ColumnFilterType> supportedFilterTypes = ColumnFilterType.getSupportedForTypes(columnTypes);

        applicableColumnFilterTypeList.addAll(supportedFilterTypes.stream().map(ColumnFilterType::getDescription).toList());

        filterTypeComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            selectedFilterType = ColumnFilterType.getColumnTypeFromDescription(newValue);
            constructFilterUserInput(selectedFilterType);
        });

        filterTypeComboBox.setItems(applicableColumnFilterTypeList);
    }

    private void constructFilterUserInput(ColumnFilterType columnFilterType) {
        if (columnFilterType == null) return;

        switch (columnFilterType) {
            case EQUALS, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, GREATER_THAN, LESS_THAN, NOT_EQUALS, LIKE,
                 NOT_LIKE, IN, NOT_IN -> {
                showOneTextFieldHBox();
            }
            case BETWEEN, NOT_BETWEEN -> {
                showTwoTextFieldsHBox();
            }
            case IS_NOT_NULL, IS_NULL -> {
                hideOneTextFieldHBox();
                hideTwoTextFieldsHBox();
            }
        }

    }

    private void showOneTextFieldHBox() {
        hideTwoTextFieldsHBox();
        OneTextFieldHBox.setVisible(true);
        OneTextFieldHBox.setManaged(true);
    }
    private void showTwoTextFieldsHBox() {
        hideOneTextFieldHBox();
        TwoTextFieldsHBox.setVisible(true);
        TwoTextFieldsHBox.setManaged(true);
    }

    private void hideOneTextFieldHBox() {
        OneTextFieldHBox.setManaged(false);
        OneTextFieldHBox.setVisible(false);
    }
    private void hideTwoTextFieldsHBox() {
        TwoTextFieldsHBox.setVisible(false);
        TwoTextFieldsHBox.setManaged(false);
    }




    private ComparedTable getComparedTableByTableName(String tableName) {
        return comparedTables.stream()
                .filter(t -> t.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
    }

    private ComparedTableColumn getComparedTableColumnByColumnName(ComparedTable comparedTable, String columnName) {
        return comparedTable.getComparedTableColumns().stream()
                .filter(c -> c.getColumnName().equals(columnName))
                .findFirst()
                .orElse(null);
    }

    private List<ComparedTableColumn> getAllEqualColumns(ComparedTableColumn comparedTableColumn) {
        return comparedTables.stream()
                .flatMap(comparedTable -> comparedTable.getComparedTableColumns().stream())
                .filter(column ->
                        column.getColumnName().equals(comparedTableColumn.getColumnName())
                                && Set.copyOf(column.getColumnTypes()).equals(Set.copyOf(comparedTableColumn.getColumnTypes())) //turns to set to ignore order
                                && !column.equals(comparedTableColumn) //avoids self
                )
                .toList();
    }

    private void addFilter(boolean closeAfter) {
        if (selectedComparedTable == null || selectedComparedTableColumn == null || selectedFilterType == null) {
            return;
        }

        ColumnFilter columnFilter;

        switch (selectedFilterType.getNumberOfArguments()) {
            case 0 -> {
                columnFilter = new ColumnFilter(selectedFilterType);
            }
            case 2 -> {
                String lower = lowerValueTextField.getText().trim();
                String higher = higherValueTextField.getText().trim();
                if (lower.isEmpty() || higher.isEmpty()) return;

                columnFilter = new ColumnFilter(selectedFilterType, lower, higher);
            }
            default -> {
                String value = filterTextField.getText().trim();
                if (value.isEmpty()) return;

                columnFilter = new ColumnFilter(selectedFilterType, value);
            }
        }

        filterMap.computeIfAbsent(selectedComparedTableColumn, k -> new ArrayList<>())
                .add(columnFilter);

        if (addToAllTablesCheckBox.isSelected()) {
            for (ComparedTableColumn comparedTableColumn : getAllEqualColumns(selectedComparedTableColumn)) {
                filterMap.computeIfAbsent(comparedTableColumn, k -> new ArrayList<>())
                        .add(columnFilter);
            }
        }

        if (closeAfter) {
            stage.close();
        }
    }


    @FXML
    private void onAddAndCloseButtonClicked() {
        boolean closeAfter = true;
        addFilter(closeAfter);
    }

    @FXML
    private void onAddButtonClicked() {
        boolean closeAfter = false;
        addFilter(closeAfter);
    }

    @FXML
    private void onCancelClicked() {
        filterMap.clear();
        stage.close();
    }

    public Map<ComparedTableColumn, List<ColumnFilter>> getFilterMap() {
        return filterMap;
    }
}