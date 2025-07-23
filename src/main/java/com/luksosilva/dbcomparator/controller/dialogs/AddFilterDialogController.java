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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.*;

public class AddFilterDialogController {


    private Stage stage;

    boolean isAdding = false;
    boolean isEditing = false;

    @FXML
    public Label titleLabel;

    @FXML
    public ComboBox<String> tableComboBox;
    @FXML
    public ComboBox<String> columnComboBox;
    @FXML
    public ComboBox<String> filterTypeComboBox;

    @FXML
    public HBox filterHeaderHBox;
    @FXML
    public Label filterTipLabel;

    @FXML
    public HBox oneTextFieldHBox;
    @FXML
    public TextField filterTextField;

    @FXML
    public HBox twoTextFieldsHBox;
    @FXML
    public TextField lowerValueTextField;
    @FXML
    public TextField higherValueTextField;

    @FXML
    public CheckBox applyToMatchingColumnsCheckBox;

    @FXML
    public Button addButton;
    @FXML
    public Button addAndCloseButton;


    private final Map<ComparedTableColumn, List<ColumnFilter>> addedFilterMap = new HashMap<>();
    private final Map<ComparedTableColumn, Map<ColumnFilter, ColumnFilter>> editedFilterMap = new HashMap<>();
    private List<ComparedTable> comparedTables;

    private ComparedTable selectedComparedTable;
    private ComparedTableColumn selectedComparedTableColumn;
    private ColumnFilterType selectedFilterType;
    private ColumnFilter editingColumnFilter;

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

    public void initializeAddDialog(List<ComparedTable> comparedTableList) {
        isAdding = true;

        titleLabel.setText("Adicionar novo filtro");

        setComparedTables(comparedTableList);
        setupSearchListeners();
        setupDisablingListeners();
        constructTableComboBox();
    }

    public void initializeEditDialog(List<ComparedTable> comparedTableList, ComparedTable comparedTable, ComparedTableColumn comparedTableColumn, ColumnFilter columnFilter) {
        titleLabel.setText("Editar filtro");
        isEditing = true;
        editingColumnFilter = columnFilter;

        setComparedTables(comparedTableList);

        selectedComparedTable = comparedTable;
        selectedComparedTableColumn = comparedTableColumn;
        selectedFilterType = columnFilter.getColumnFilterType();

        tableComboBox.setValue(selectedComparedTable.getTableName());
        columnComboBox.setValue(selectedComparedTableColumn.getColumnName());
        filterTypeComboBox.setValue(selectedFilterType.getDescription());

        tableComboBox.setDisable(true);
        filterTypeComboBox.setDisable(false);

        constructFilterTypeComboBox(selectedComparedTableColumn);
        constructFilterUserInput(editingColumnFilter.getColumnFilterType());
        switch (selectedFilterType.getNumberOfArguments()){
            case 0 -> {

            }
            case 2 -> {
                lowerValueTextField.setText(editingColumnFilter.getLowerValue());
                higherValueTextField.setText(editingColumnFilter.getHigherValue());
            }
            default -> {
                filterTextField.setText(editingColumnFilter.getValue());
            }
        }

        addButton.setVisible(false);
        applyToMatchingColumnsCheckBox.setText("Editar filtro em todas tabelas com o mesmo campo e filtro");
        addAndCloseButton.setText("aplicar");
    }

    private void setupSearchListeners() {
        tableComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            tableComboBox.show();
            Platform.runLater(() -> {
                if (newText.equals(tableComboBox.getValue())) return;
                filteredTableList.setPredicate(name -> name.toLowerCase().contains(newText.toLowerCase()));
            });
        });

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

    }

    private void setupDisablingListeners() {
        tableComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterTypeComboBox.setDisable(true);
            filterTypeComboBox.getSelectionModel().clearSelection();
            clearTextFields();
        });

        columnComboBox.disabledProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                filterTypeComboBox.setDisable(true);
            }
        });

        columnComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            clearTextFields();
        });

        filterTypeComboBox.disabledProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                oneTextFieldHBox.setDisable(true);
                twoTextFieldsHBox.setDisable(true);
                filterTipLabel.setText("");
                filterHeaderHBox.setVisible(true);
            }
        });
    }

    private void constructTableComboBox() {
        fullTableList.clear();
        for (ComparedTable table : comparedTables) {
            fullTableList.add(table.getTableName());
        }

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

        columnComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) newVal = "";
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

        filterHeaderHBox.setVisible(true);

        switch (columnFilterType) {
            case EQUALS, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, GREATER_THAN, LESS_THAN, NOT_EQUALS -> {
                showOneTextFieldHBox("");
            }
            case LIKE, NOT_LIKE -> {
                showOneTextFieldHBox("(use os coringas % e _ livremente)");
            }
            case IN, NOT_IN -> {
                showOneTextFieldHBox("(separe os valores por vírgula)");
            }
            case BETWEEN, NOT_BETWEEN -> {
                showTwoTextFieldsHBox("");
            }
            case IS_NOT_NULL, IS_NULL -> {
                hideAllTextFieldsHBox();
            }
        }

    }

    private void showOneTextFieldHBox(String tip) {
        hideTwoTextFieldsHBox();
        oneTextFieldHBox.setDisable(false);
        oneTextFieldHBox.setManaged(true);
        oneTextFieldHBox.setVisible(true);
        filterTipLabel.setText(tip);
    }
    private void showTwoTextFieldsHBox(String tip) {
        hideOneTextFieldHBox();
        twoTextFieldsHBox.setDisable(false);
        twoTextFieldsHBox.setManaged(true);
        twoTextFieldsHBox.setVisible(true);
        filterTipLabel.setText(tip);
    }

    private void hideOneTextFieldHBox() {
        oneTextFieldHBox.setDisable(true);
        oneTextFieldHBox.setManaged(false);
        oneTextFieldHBox.setVisible(false);
    }
    private void hideTwoTextFieldsHBox() {
        twoTextFieldsHBox.setDisable(true);
        twoTextFieldsHBox.setVisible(false);
        twoTextFieldsHBox.setManaged(false);
    }
    private void hideAllTextFieldsHBox() {
        filterHeaderHBox.setVisible(false);
        hideOneTextFieldHBox();
        hideTwoTextFieldsHBox();
    }

    private void resetUI() {
        tableComboBox.getSelectionModel().clearSelection();
        columnComboBox.getSelectionModel().clearSelection();
        applyToMatchingColumnsCheckBox.setSelected(false);
        clearTextFields();
    }

    private void clearTextFields() {
        filterTextField.setText("");
        lowerValueTextField.setText("");
        higherValueTextField.setText("");
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

    private void processFilter(boolean closeAfter) {
        if (selectedComparedTable == null || selectedComparedTableColumn == null || selectedFilterType == null) return;

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

        if (isAdding) {
            addedFilterMap.computeIfAbsent(selectedComparedTableColumn, k -> new ArrayList<>())
                    .add(columnFilter);

            if (applyToMatchingColumnsCheckBox.isSelected()) {
                for (ComparedTableColumn comparedTableColumn : getAllEqualColumns(selectedComparedTableColumn)) {
                    addedFilterMap.computeIfAbsent(comparedTableColumn, k -> new ArrayList<>())
                            .add(columnFilter);
                }
            }
        }
        else if (isEditing && !editingColumnFilter.equals(columnFilter)) {
            editedFilterMap.put(selectedComparedTableColumn, Map.of(editingColumnFilter, columnFilter));

            if (applyToMatchingColumnsCheckBox.isSelected()) {
                for (ComparedTableColumn comparedTableColumn : getAllEqualColumns(selectedComparedTableColumn)) {

                    ColumnFilter oldColumnFilter = comparedTableColumn.getColumnFilter().stream()
                            .filter(cf -> cf.equals(editingColumnFilter))
                            .findFirst()
                            .orElse(null);
                    if (oldColumnFilter == null) continue;

                    editedFilterMap.computeIfAbsent(comparedTableColumn, k -> new HashMap<>())
                            .put(oldColumnFilter, columnFilter);
                }
            }
        }

        if (closeAfter) {
            stage.close();
        }
    }


    @FXML
    private void onAddAndCloseButtonClicked() {
        boolean closeAfter = true;
        processFilter(closeAfter);
    }

    @FXML
    private void onAddButtonClicked() {
        boolean closeAfter = false;
        processFilter(closeAfter);
        resetUI();
    }

    @FXML
    private void onCancelClicked() {
        stage.close();
    }

    public Map<ComparedTableColumn, List<ColumnFilter>> getAddedFilterMap() {
        return addedFilterMap;
    }
    public Map<ComparedTableColumn, Map<ColumnFilter, ColumnFilter>> getEditedFilterMap() {
        return editedFilterMap;
    }
}