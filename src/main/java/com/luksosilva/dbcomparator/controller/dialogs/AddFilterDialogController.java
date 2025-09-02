package com.luksosilva.dbcomparator.controller.dialogs;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.Filter;
import com.luksosilva.dbcomparator.model.live.comparison.customization.TableFilter;
import com.luksosilva.dbcomparator.util.DialogUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

public class AddFilterDialogController {




    private Stage stage;

    boolean isAdding = false;
    boolean isEditing = false;

    @FXML
    public Label titleLabel;

    @FXML
    public RadioButton defaultFilterRadioButton;
    @FXML
    public RadioButton advancedFilterRadioButton;

    @FXML
    public ComboBox<String> tableComboBox;

    @FXML
    public VBox defaultFilterFieldsBox;
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
    public DatePicker filterDatePicker;

    @FXML
    public HBox twoTextFieldsHBox;
    @FXML
    public TextField lowerValueTextField;
    @FXML
    public TextField higherValueTextField;
    @FXML
    public DatePicker lowerValueDatePicker;
    @FXML
    public DatePicker higherValueDatePicker;

    @FXML
    public VBox advancedFilterFieldsBox;
    @FXML
    public TextArea advancedFilterTextArea;



    @FXML
    public CheckBox applyToMatchingColumnsCheckBox;

    @FXML
    public Button addButton;
    @FXML
    public Button addAndCloseButton;


    private final List<Filter> addedFilters = new ArrayList<>();
    private final Map<Filter, Filter> editedFilters = new HashMap<>();
    private List<ComparedTable> comparedTables;

    private ComparedTable selectedComparedTable;
    private ComparedTableColumn selectedComparedTableColumn;
    private ColumnFilterType selectedFilterType;
    private Filter editingFilter;

    private final ObservableList<String> fullTableList = FXCollections.observableArrayList();
    private final FilteredList<String> filteredTableList = new FilteredList<>(fullTableList, s -> true);

    private final ObservableList<String> fullColumnList = FXCollections.observableArrayList();
    private final FilteredList<String> filteredColumnList = new FilteredList<>(fullColumnList, s -> true);

    private final ObservableList<String> applicableColumnFilterTypeList = FXCollections.observableArrayList();


    public List<Filter> getAddedFilters() {
        return addedFilters;
    }
    public Map<Filter, Filter> getEditedFilters() {
        return editedFilters;
    }


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
        setupRadioButtons();

        constructTableComboBox();
    }

    public void initializeEditAdvancedFilterDialog(ComparedTable comparedTable) {
        titleLabel.setText("Editar filtro");
        isEditing = true;
        editingFilter = comparedTable.getFilter();

        selectedComparedTable = comparedTable;
        tableComboBox.setValue(selectedComparedTable.getTableName());

        setupRadioButtons();

        advancedFilterRadioButton.setSelected(true);
        defaultFilterRadioButton.setDisable(true);
        advancedFilterRadioButton.setDisable(true);

        tableComboBox.setDisable(true);
        advancedFilterTextArea.setText(getAdvancedFilterPrefix() + comparedTable.getFilter().getUserWrittenFilter());
        advancedFilterTextArea.setDisable(false);

        addButton.setVisible(false);
        addAndCloseButton.setText("aplicar");
    }

    public void initializeEditDefaultFilterDialog(List<ComparedTable> comparedTableList, ColumnFilter columnFilter) {
        titleLabel.setText("Editar filtro");
        isEditing = true;
        editingFilter = columnFilter;

        setComparedTables(comparedTableList);

        defaultFilterRadioButton.setSelected(true);
        defaultFilterRadioButton.setDisable(true);
        advancedFilterRadioButton.setDisable(true);

        selectedFilterType = columnFilter.getColumnFilterType();
        selectedComparedTableColumn = columnFilter.getComparedTableColumn();
        selectedComparedTable = selectedComparedTableColumn.getComparedTable();



        tableComboBox.setValue(selectedComparedTable.getTableName());
        columnComboBox.setValue(selectedComparedTableColumn.getColumnName());
        filterTypeComboBox.setValue(selectedFilterType.getDescriptionWithDetail());

        tableComboBox.setDisable(true);
        filterTypeComboBox.setDisable(false);

        constructFilterTypeComboBox(selectedComparedTableColumn);
        constructFilterUserInput(((ColumnFilter) editingFilter).getColumnFilterType());
        switch (selectedFilterType.getNumberOfArguments()){
            case 0 -> {
            }
            case 2 -> {
                if (isSelectedFieldTypeDate()) {
                    lowerValueDatePicker.setValue(((ColumnFilter) editingFilter).getLowerDate().toLocalDate());
                    higherValueDatePicker.setValue(((ColumnFilter) editingFilter).getHigherDate().toLocalDate());
                } else {
                    lowerValueTextField.setText(((ColumnFilter) editingFilter).getLowerValue());
                    higherValueTextField.setText(((ColumnFilter) editingFilter).getHigherValue());
                }
            }
            default -> {
                if (isSelectedFieldTypeDate()) {
                    filterDatePicker.setValue(((ColumnFilter) editingFilter).getDate().toLocalDate());
                } else {
                    filterTextField.setText(((ColumnFilter) editingFilter).getValue());
                }
            }
        }

        applyToMatchingColumnsCheckBox.setText("Editar filtro em todas tabelas com o mesmo campo e filtro");

        addButton.setVisible(false);
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

        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oneTextFieldHBox.setDisable(true);
                twoTextFieldsHBox.setDisable(true);
            } else {
                oneTextFieldHBox.setDisable(false);
                twoTextFieldsHBox.setDisable(false);
            }
        });
    }

    private void setupRadioButtons() {
        ToggleGroup filterModeToggleGroup = new ToggleGroup();
        defaultFilterRadioButton.setToggleGroup(filterModeToggleGroup);
        advancedFilterRadioButton.setToggleGroup(filterModeToggleGroup);

        defaultFilterRadioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                showDefaultFilterFieldsBox();
            }
        });
        advancedFilterRadioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                showAdvancedFilterFieldsBox();
            }
        });
    }

    private void setupAdvancedFilterPrefix() {
        if (selectedComparedTable == null) return;
        advancedFilterTextArea.setText(getAdvancedFilterPrefix());

        advancedFilterTextArea.textProperty().addListener((obs, oldText, newText) -> {
            String prefix = getAdvancedFilterPrefix();

            if (newText.equals(getAdvancedFilterPrefix())) return;

            if (!newText.startsWith(prefix)) {
                // Revert invalid edit
                advancedFilterTextArea.setText(oldText);
            } else if (newText.length() < prefix.length()) {
                // Prevent deleting part of the prefix
                advancedFilterTextArea.setText(prefix);
            }
        });

        Platform.runLater(() -> advancedFilterTextArea.positionCaret(getAdvancedFilterPrefix().length()));
    }

    private String getAdvancedFilterPrefix() {
        if (selectedComparedTable == null) return "";
        return "SELECT * FROM " + selectedComparedTable.getTableName() + " WHERE ";
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
                advancedFilterTextArea.setDisable(true);
                selectedComparedTable = null;
                return;
            }
            columnComboBox.setDisable(false);
            advancedFilterTextArea.setDisable(false);
            selectedComparedTable = comparedTable;
            constructColumnComboBox(comparedTable);
            setupAdvancedFilterPrefix();
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
            String columnNameWithoutType = removeDetail(newVal);

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

        applicableColumnFilterTypeList.addAll(supportedFilterTypes.stream().map(ColumnFilterType::getDescriptionWithDetail).toList());

        filterTypeComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldVal, newVal) -> {
            if (newVal == null) newVal = "";
            String filterTypeDescription = removeDetail(newVal);

            selectedFilterType = ColumnFilterType.getColumnTypeFromDescription(filterTypeDescription);
            constructFilterUserInput(selectedFilterType);
        });

        filterTypeComboBox.setItems(applicableColumnFilterTypeList);
    }

    private String removeDetail(String textWithDetail) {
        return textWithDetail.replaceAll("\\s*\\[.*]$", "");
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

    private boolean isSelectedFieldTypeDate() {
        if (selectedComparedTableColumn == null) return false;
        return selectedComparedTableColumn.getColumnTypes().contains("DATE");
    }

    private boolean isSelectedFieldTypeNumeric() {
        if (selectedComparedTable == null) return false;

        List<String> columnTypes = selectedComparedTableColumn.getColumnTypes();

        return columnTypes.stream()
                .anyMatch(t ->
                        t.contains("INT")
                        || t.contains("REAL")
                        || t.contains("FLOA")
                        || t.contains("DOUB")
                        || t.contains("NUMERIC")
                        || t.contains("DECIMAL")
                );
    }

    private void showOneTextFieldHBox(String tip) {
        hideAndDisableNodes(twoTextFieldsHBox);
        showAndEnableNodes(oneTextFieldHBox);
        filterTipLabel.setText(tip);

        if (isSelectedFieldTypeDate()) {
            hideAndDisableNodes(filterTextField);
            showAndEnableNodes(filterDatePicker);
        } else {
            hideAndDisableNodes(filterDatePicker);
            showAndEnableNodes(filterTextField);
        }

    }

    private void showTwoTextFieldsHBox(String tip) {
        hideAndDisableNodes(oneTextFieldHBox);
        showAndEnableNodes(twoTextFieldsHBox);
        filterTipLabel.setText(tip);

        if (isSelectedFieldTypeDate()) {
            hideAndDisableNodes(lowerValueTextField, higherValueTextField);
            showAndEnableNodes(lowerValueDatePicker, higherValueDatePicker);
        } else {
            hideAndDisableNodes(lowerValueDatePicker, higherValueDatePicker);
            showAndEnableNodes(lowerValueTextField, higherValueTextField);
        }
    }


    private void hideAllTextFieldsHBox() {
        filterHeaderHBox.setVisible(false);
        hideAndDisableNodes(oneTextFieldHBox, twoTextFieldsHBox);
    }

    private void showDefaultFilterFieldsBox() {
        hideNodes(advancedFilterFieldsBox);
        showNodes(defaultFilterFieldsBox);
    }

    private void showAdvancedFilterFieldsBox() {
        hideNodes(defaultFilterFieldsBox);
        setupAdvancedFilterPrefix();
        showNodes(advancedFilterFieldsBox);
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

    private void showNodes(Node ... nodes) {
        for (Node node : nodes) {
            node.setManaged(true);
            node.setVisible(true);
        }
    }
    private void hideNodes(Node ... nodes) {
        for (Node node : nodes) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    private void hideAndDisableNodes(Node ... nodes) {
        hideNodes(nodes);
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }
    private void showAndEnableNodes(Node ... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
        showNodes(nodes);
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


    private void processAdvancedFilter(boolean closeAfter) {
        if (!isAdvancedFilterValid()) {
            return;
        }

        boolean anyColumnHasFilter = selectedComparedTable.getComparedTableColumns().stream()
                .anyMatch(comparedTableColumn -> !comparedTableColumn.getColumnFilters().isEmpty());
        if (anyColumnHasFilter) {
            boolean confirm = DialogUtils.askConfirmation(stage,
                    "Os filtros das colunas serão apagados.",
                    "Essa tabela possui filtros aplicados em colunas individuais. Eles serão removidos ao aplicar um filtro avançado. Deseja continuar?");
            if (!confirm) {
                return;
            }
        }

        TableFilter tableFilter = new TableFilter(selectedComparedTable, getUserWrittenAdvancedFilter());

        if (isAdding) {
            addedFilters.add(tableFilter);
        }
        else if (isEditing && !editingFilter.equals(tableFilter)) {
            editedFilters.put(tableFilter, selectedComparedTable.getFilter());
        }

        if (closeAfter) {
            stage.close();
        }
    }


    private void processDefaultFilter(boolean closeAfter) {
        if (!isDefaultFilterValid()) {
            return;
        }

        boolean tableHasAdvancedFilter = selectedComparedTable.getFilter() != null;
        if (tableHasAdvancedFilter) {
            boolean confirm = DialogUtils.askConfirmation(stage,
                    "O filtro avançado será apagado.",
                    "Essa tabela possui filtro avançado aplicado. Ele será removido ao aplicar um filtro padrão. Deseja continuar?");
            if (!confirm) {
                return;
            }
        }

        ColumnFilter columnFilter;

        switch (selectedFilterType.getNumberOfArguments()) {
            case 0 -> {
                columnFilter = new ColumnFilter(selectedComparedTableColumn, selectedFilterType);
            }
            case 2 -> {

                if (isSelectedFieldTypeDate()) {
                    LocalDateTime lower = lowerValueDatePicker.getValue().atStartOfDay();
                    LocalDateTime higher = higherValueDatePicker.getValue().atStartOfDay();

                    columnFilter = new ColumnFilter(selectedComparedTableColumn, selectedFilterType, lower, higher);
                } else {
                    String lower = lowerValueTextField.getText().trim();
                    String higher = higherValueTextField.getText().trim();

                    columnFilter = new ColumnFilter(selectedComparedTableColumn, selectedFilterType, lower, higher);
                }

            }
            default -> {

                if (isSelectedFieldTypeDate()) {
                    LocalDateTime value = filterDatePicker.getValue().atStartOfDay();
                    columnFilter = new ColumnFilter(selectedComparedTableColumn, selectedFilterType, value);
                } else {
                    String value = filterTextField.getText().trim();
                    columnFilter = new ColumnFilter(selectedComparedTableColumn, selectedFilterType, value);
                }

            }
        }

        if (isAdding) {

            addedFilters.add(columnFilter);

            if (applyToMatchingColumnsCheckBox.isSelected()) {
                for (ComparedTableColumn comparedTableColumn : getAllEqualColumns(selectedComparedTableColumn)) {

                    addedFilters.add(new ColumnFilter(columnFilter, comparedTableColumn));
                }
            }
        }
        else if (isEditing && !editingFilter.equals(columnFilter)) {
            editedFilters.put(columnFilter, editingFilter);

            if (applyToMatchingColumnsCheckBox.isSelected()) {
                for (ComparedTableColumn comparedTableColumn : getAllEqualColumns(selectedComparedTableColumn)) {

                    ColumnFilter oldColumnFilter = comparedTableColumn.getColumnFilters().stream()
                            .filter(cf -> cf.equalsIgnoreColumn(editingFilter))
                            .findFirst()
                            .orElse(null);
                    if (oldColumnFilter == null) continue;

                    editedFilters.put(new ColumnFilter(columnFilter, comparedTableColumn), oldColumnFilter);
                }
            }
        }

        if (closeAfter) {
            stage.close();
        }
    }

    private boolean isDefaultFilterValid() {
        if (selectedComparedTable == null || selectedComparedTableColumn == null || selectedFilterType == null) {
            showMissingFieldsWarning();
            return false;
        }

        int args = selectedFilterType.getNumberOfArguments();
        if (args == 0) return true;

        // Field presence check:
        if (isSelectedFieldTypeDate()) {

            if (args == 1 && filterDatePicker.getValue() == null) {
                showMissingFieldsWarning();
                return false;
            }
            else if (args == 2 && (lowerValueDatePicker.getValue() == null || higherValueDatePicker.getValue() == null)) {
                showMissingFieldsWarning();
                return false;
            }
        } else {

            if (args == 1 && filterTextField.getText().isEmpty()) {
                showMissingFieldsWarning();
                return false;
            }
            else if (args == 2 && (lowerValueTextField.getText().isEmpty() || higherValueTextField.getText().isEmpty())) {
                showMissingFieldsWarning();
                return false;
            }

        }

        // Numeric validation:
        if (isSelectedFieldTypeNumeric()) {
            if (args == 1 && isWrittenFilterInvalid(filterTextField.getText())) {
                showInvalidFieldWarning();
                return false;
            }
            if (args == 2 && (isWrittenFilterInvalid(lowerValueTextField.getText())
                    || isWrittenFilterInvalid(higherValueTextField.getText()))) {
                showInvalidFieldWarning();
                return false;
            }
        }

        // Between validation:
        if (args == 2) {
            if (isSelectedFieldTypeDate()) {
                if (higherValueDatePicker.getValue().isBefore(lowerValueDatePicker.getValue())
                        || higherValueDatePicker.getValue().isEqual(lowerValueDatePicker.getValue()) ) {
                    showInvalidBetweenValuesWarning();
                    return false;
                }
            } else {
                if (Double.parseDouble(higherValueTextField.getText()) <= Double.parseDouble(lowerValueTextField.getText())) {
                    showInvalidBetweenValuesWarning();
                    return false;
                }
            }
        }


        return true;
    }



    private boolean isWrittenFilterInvalid(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }

    private boolean isAdvancedFilterValid() {
        String userWrittenFilter = getUserWrittenAdvancedFilter();

        if (selectedComparedTable == null || userWrittenFilter.isEmpty()) {
            showMissingFieldsWarning();
            return false;
        }

        return true;
    }

    private void showMissingFieldsWarning() {
        DialogUtils.showWarning(stage,
                "Preencha todos os campos.",
                "Todos os campos devem ser preenchidos para prosseguir."
        );
    }

    private void showInvalidFieldWarning() {
        DialogUtils.showWarning(stage,
                "Valor inválido",
                "Filtro informado não é válido para o tipo do campo."
        );
    }

    private void showInvalidBetweenValuesWarning() {
        DialogUtils.showWarning(stage,
                "Valores inválidos",
                "Informe sempre o menor valor primeiro seguido pelo maior valor."
        );
    }

    private String getUserWrittenAdvancedFilter() {
        return advancedFilterTextArea.getText().
                substring(getAdvancedFilterPrefix().length())
                .split(";", 2)[0]
                .trim();
    }

    @FXML
    private void onAddAndCloseButtonClicked() {
        boolean closeAfter = true;
        if (advancedFilterRadioButton.isSelected()) {
            processAdvancedFilter(closeAfter);
        } else {
            processDefaultFilter(closeAfter);
        }
    }

    @FXML
    private void onAddButtonClicked() {
        boolean closeAfter = false;
        if (advancedFilterRadioButton.isSelected()) {
            processAdvancedFilter(closeAfter);
        } else {
            processDefaultFilter(closeAfter);
        }

        resetUI();
    }

    @FXML
    private void onCancelClicked() {
        stage.close();
    }

}