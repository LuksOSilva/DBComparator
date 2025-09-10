package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.util.CsvExporter;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.result.ComparableColumnViewModel;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.result.IdentifierColumnViewModel;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.result.RowDifferenceViewModel;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.result.TableComparisonResultViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class TableComparisonResultScreenController {


    private Stage currentStage;
    private Stage columnSelectorStage;

    private TableComparisonResultViewModel tableComparisonResultViewModel;

    private final ObservableList<RowDifferenceViewModel> diffRowViewModels = FXCollections.observableArrayList();
    private FilteredList<RowDifferenceViewModel> filteredDiffRowViewModels;


    @FXML
    public Label titleLabel;

    @FXML
    public MenuButton visibleColumnsMenuButton;
    @FXML
    public ComboBox<String> columnsComboBox;
    @FXML
    public ComboBox<String> sourcesComboBox;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button applyFilterButton;
    @FXML
    public CheckBox showExclusiveRecordsCheckBox;
    @FXML
    public CheckBox showDivergingRecordsCheckBox;

    @FXML
    public TableView<RowDifferenceViewModel> differencesTableView;



    public void setStage(Stage stage) {
        this.currentStage = stage;

        stage.setOnCloseRequest(event -> {
            event.consume(); // Stop the default close behavior
            onCloseButtonClicked();
        });
    }

    public void init(TableComparisonResultViewModel tableComparisonResultViewModel) {
        this.tableComparisonResultViewModel = tableComparisonResultViewModel;
        titleLabel.setText(tableComparisonResultViewModel.getTableName());

        constructDifferencesTableView();

        setupColumnsComboBox();
        setupSourcesComboBox();
        setupVisibleColumnsMenuButton();
    }

    /// SETUPS

    private void setupVisibleColumnsMenuButton() {
        if (differencesTableView.getColumns().isEmpty()) {
            visibleColumnsMenuButton.setVisible(false);
            return;
        }

        visibleColumnsMenuButton.getItems().addAll(constructVisibleColumnsMenuActions());

        visibleColumnsMenuButton.getItems().add(new SeparatorMenuItem());

        MenuItem openSelectorDialog = new MenuItem("Selecionar colunas...");
        openSelectorDialog.setOnAction(e -> {
            if (columnSelectorStage != null) {
                columnSelectorStage.toFront();
                columnSelectorStage.requestFocus();
                return;
            }
            onOpenColumnSelectorDialog();
        });
        visibleColumnsMenuButton.getItems().add(openSelectorDialog);

    }

    private void setupSourcesComboBox() {

        List<String> sourceIds = tableComparisonResultViewModel.getModel().getComparedTable().getPerSourceTable().keySet()
                .stream().toList();

        sourcesComboBox.getItems().add("Todas");
        sourcesComboBox.getItems().addAll(sourceIds);

        sourcesComboBox.setValue(sourcesComboBox.getItems().getFirst());
    }

    private void setupColumnsComboBox() {

        List<String> identifierColumns = tableComparisonResultViewModel.getModel().getComparedTable().getOrderedComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .map(ComparedTableColumn::getColumnName)
                .toList();

        List<String> comparableColumns = tableComparisonResultViewModel.getModel().getComparedTable().getOrderedComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable())
                .map(ComparedTableColumn::getColumnName)
                .toList();


        List<String> allColumns = new ArrayList<>();
        allColumns.addAll(identifierColumns);
        allColumns.addAll(comparableColumns);


        columnsComboBox.getItems().setAll(allColumns);
        if (!allColumns.isEmpty()) {
            columnsComboBox.setValue(allColumns.getFirst()); // Default selection
        }

        columnsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (identifierColumns.contains(newVal)) {
                sourcesComboBox.setManaged(false);
                sourcesComboBox.setVisible(false);
                return;
            }
            sourcesComboBox.setManaged(true);
            sourcesComboBox.setVisible(true);
        });
    }


    /// CONSTRUCTORS

    private List<MenuItem> constructVisibleColumnsMenuActions() {

        List<MenuItem> menuActions = new ArrayList<>();

        /// show all
        MenuItem showAll = new MenuItem("Mostrar todas");
        showAll.setOnAction(e -> {

            differencesTableView.getColumns().forEach(column -> column.setVisible(true));

        });
        menuActions.add(showAll);

        /// hide all
        MenuItem hideAll = new MenuItem("Esconder todas");
        hideAll.setOnAction(e -> {

            differencesTableView.getColumns().forEach(column -> column.setVisible(false));

        });
        menuActions.add(hideAll);


        return menuActions;
    }

    private void constructDifferencesTableView() {
        differencesTableView.setPlaceholder(new Label("Nenhum registro com diferença encontrado"));

        constructTableColumns();

        diffRowViewModels.setAll(tableComparisonResultViewModel.getRowDifferenceViewModels());

        filteredDiffRowViewModels = new FilteredList<>(diffRowViewModels, pane -> true);

        differencesTableView.getItems().setAll(filteredDiffRowViewModels);
    }

    private void constructTableColumns() {
        differencesTableView.getColumns().clear();

        if (tableComparisonResultViewModel.getRowDifferenceViewModels().isEmpty()) return;

        // Get the full column structure from the ComparedTable
        List<ComparedTableColumn> allTableColumns = tableComparisonResultViewModel
                .getModel()
                .getComparedTable()
                .getOrderedComparedTableColumns();

        boolean areAllColumnsIdentifiers = allTableColumns.stream()
                .noneMatch(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable());

        if (areAllColumnsIdentifiers) {

            TableColumn<RowDifferenceViewModel, String> sourceColumn = constructSourceColumn();

            differencesTableView.getColumns().add(sourceColumn);
        }

        // For each table column, check if it’s an identifier or data column
        for (ComparedTableColumn comparedTableColumn : allTableColumns) {

            if (comparedTableColumn.getColumnSetting().isIdentifier()) {

                TableColumn<RowDifferenceViewModel, String> identifierColumn = constructIdentifierColumn(comparedTableColumn);

                differencesTableView.getColumns().add(identifierColumn);

            }
            else if (comparedTableColumn.getColumnSetting().isComparable()) {

                TableColumn<RowDifferenceViewModel, String> comparableColumn = constructComparableColumn(comparedTableColumn);

                differencesTableView.getColumns().add(comparableColumn);
            }
        }

    }

    private TableColumn<RowDifferenceViewModel, String> constructSourceColumn() {
        TableColumn<RowDifferenceViewModel, String> sourceColumn = new TableColumn<>("Fonte");

        sourceColumn.setCellValueFactory(data -> {

            String sources = data.getValue().getExistsOn();

            return new SimpleStringProperty(sources);
        });

        return sourceColumn;
    }

    private TableColumn<RowDifferenceViewModel, String> constructIdentifierColumn(ComparedTableColumn comparedTableColumn) {

        TableColumn<RowDifferenceViewModel, String> identifierColumn = new TableColumn<>(comparedTableColumn.getColumnName());
        identifierColumn.setCellValueFactory(data -> {
            return new SimpleStringProperty(
                    data.getValue()
                            .getIdentifierColumnViewModels()
                            .stream()
                            .filter(vm -> vm.getModel().getComparedTableColumn().equals(comparedTableColumn))
                            .map(IdentifierColumnViewModel::getValue)
                            .findFirst()
                            .orElse("")
            );
        });

        identifierColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Clear styles and text every time
                getStyleClass().removeAll("cell-null", "cell-default", "cell-different", "row-missing");
                setText(null);

                if (empty) {
                    return;
                }

                //default
                setText(item);

                // Italic + centered for NULL values
                if (item == null || item.isEmpty() || item.equals("NULL")) {
                    getStyleClass().add("cell-null");
                }

                // style for row:
                RowDifferenceViewModel rowVM = getTableView().getItems().get(getIndex());

                // If row missing in any source
                if (rowVM.isMissingInAnySource()) {
                    getStyleClass().add("row-missing");
                    return;
                }

                if (getStyleClass().isEmpty() && getTableRow().getStyleClass().isEmpty()) {
                    getStyleClass().add("cell-default");
                }
            }
        });

        return identifierColumn;
    }

    private TableColumn<RowDifferenceViewModel, String> constructComparableColumn(ComparedTableColumn comparedTableColumn) {

        TableColumn<RowDifferenceViewModel, String> comparableColumn = new TableColumn<>(comparedTableColumn.getColumnName());

        // Get all source IDs for this table
        Set<String> sourceIds = new LinkedHashSet<>(tableComparisonResultViewModel
                .getModel()
                .getComparedTable()
                .getPerSourceTable()
                .keySet());

        for (String sourceId : sourceIds) {

            TableColumn<RowDifferenceViewModel, String> sourceValueColumn =
                    constructSourceValueColumn(comparedTableColumn, sourceId);

            comparableColumn.getColumns().add(sourceValueColumn);
        }

        return comparableColumn;
    }

    private TableColumn<RowDifferenceViewModel, String> constructSourceValueColumn(ComparedTableColumn comparedTableColumn, String sourceId) {

        TableColumn<RowDifferenceViewModel, String> sourceValueColumn = new TableColumn<>(sourceId);

        sourceValueColumn.setCellValueFactory(data -> {
            // Find the differing column view model for this column
            Optional<ComparableColumnViewModel> comparableVmOpt = data.getValue()
                    .getComparableColumnViewModels()
                    .stream()
                    .filter(vm -> vm.getModel().getComparedTableColumn().equals(comparedTableColumn))
                    .findFirst();


            String value = comparableVmOpt
                        .map(vm -> {
                            Map<String, String> map = vm.getPerSourceValue();
                            if (!map.containsKey(sourceId)) {
                                return "";
                            }
                            String v = map.get(sourceId);
                            return v != null ? v : "";
                        })
                        .orElse("");


            return new SimpleStringProperty(value);
        });

        // Style cells
        sourceValueColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Clear styles and text every time
                //getTableRow().getStyleClass().removeAll("row-missing");
                getStyleClass().removeAll("cell-null", "cell-default", "cell-different", "row-missing");
                setText(null);

                if (empty) {
                    return;
                }

                //default
                setText(item);

                // Italic + centered for NULL values
                if (item == null || item.isEmpty() || item.equals("NULL")) {
                    getStyleClass().add("cell-null");
                }

                // style for row:
                RowDifferenceViewModel rowVM = getTableView().getItems().get(getIndex());

                // If row missing in any source → apply row style
                if (rowVM.isMissingInAnySource()) {
                    getStyleClass().add("row-missing");
                    return;
                }

                // style for column:
                rowVM.getComparableColumnViewModels().stream()
                        .filter(vm -> vm.getModel().getComparedTableColumn().equals(comparedTableColumn))
                        .findFirst()
                        .ifPresent(vm -> {
                            if (!vm.allValuesAreEqual()) {
                                getStyleClass().add("cell-different");
                            }
                        });


                if (getStyleClass().isEmpty() && getTableRow().getStyleClass().isEmpty()) {
                    getStyleClass().add("cell-default");
                }
            }
        });

        return sourceValueColumn;
    }


    /// USER ACTIONS

    @FXML
    private void onOpenColumnSelectorDialog() {
        Stage dialogStage = new Stage();
        columnSelectorStage = dialogStage;

        ListView<CheckBox> listView = new ListView<>();
        listView.setPrefSize(300, 400);  // Fix height so scrollbar appears if needed

        for (TableColumn<RowDifferenceViewModel, ?> col : differencesTableView.getColumns()) {
            CheckBox cb = new CheckBox(col.getText());
            cb.setSelected(col.isVisible());
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> col.setVisible(newVal));
            col.visibleProperty().addListener((obs, oldVal, newVal) -> cb.setSelected(newVal));
            listView.getItems().add(cb);
        }

        VBox root = new VBox(listView);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root);

        dialogStage.setTitle("Selecionar colunas visíveis");
        dialogStage.setScene(scene);
        dialogStage.initOwner(currentStage);
        dialogStage.setResizable(false);

        dialogStage.setOnCloseRequest(e -> {
            columnSelectorStage = null;
        });

        DialogUtils.showInCenter(currentStage, dialogStage);
    }

    @FXML
    public void onSearchKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            applyFilter();
        }
    }

    @FXML
    private void applyFilter() {
        String filterText = searchTextField.getText().toLowerCase().trim();
        String selectedColumnName = columnsComboBox.getValue();
        String selectedSource = sourcesComboBox.getValue();

        boolean isIdentifierFieldSearch = tableComparisonResultViewModel.getModel().getComparedTable().getComparedTableColumns()
                .stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .anyMatch(comparedTableColumn -> comparedTableColumn.getColumnName().equals(selectedColumnName));

        boolean showDivergingRecords = showDivergingRecordsCheckBox.isSelected();
        boolean showExclusiveRecords = showExclusiveRecordsCheckBox.isSelected();

        filteredDiffRowViewModels.setPredicate(row -> {
            //if nothing searched and both checkboxes marked, show all
            if ((selectedColumnName == null || filterText.isBlank()) && showDivergingRecords && showExclusiveRecords) return true;


            if (isIdentifierFieldSearch) {
                boolean matchesIdentifierSearch = row.getIdentifierColumnViewModels().stream()
                        .anyMatch(identifierColumnViewModel ->
                                identifierColumnViewModel.getColumnName().equals(selectedColumnName)
                                        && identifierColumnViewModel.getValue().toLowerCase().contains(filterText));
                if (!matchesIdentifierSearch) return false;

            } else {
                boolean matchesComparableSearch;
                if (selectedSource.equals("Todas")) {
                    matchesComparableSearch = row.getComparableColumnViewModels().stream()
                            .anyMatch(comparableColumnViewModel ->
                                    comparableColumnViewModel.getColumnName().equals(selectedColumnName)
                                            && comparableColumnViewModel.getPerSourceValue().values().stream()
                                            .anyMatch(value -> value.toLowerCase().contains(filterText)));
                } else {
                    matchesComparableSearch = row.getComparableColumnViewModels().stream()
                            .anyMatch(comparableColumnViewModel -> {
                                if (!comparableColumnViewModel.getColumnName().equals(selectedColumnName)) {
                                    return false;
                                }
                                String value = comparableColumnViewModel.getPerSourceValue().get(selectedSource);
                                return value != null && value.toLowerCase().contains(filterText);
                            });
                }
                if (!matchesComparableSearch) return false;
            }



            if (!showExclusiveRecords && row.isMissingInAnySource()) return false;

            //if its not missing from any source, it must have a difference.
            if (!showDivergingRecords && !row.isMissingInAnySource()) return false;

            return true;
        });

        differencesTableView.getItems().setAll(filteredDiffRowViewModels);
    }


    @FXML
    private void onCopyQueryButtonClicked() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(tableComparisonResultViewModel.getModel().getComparedTable().getSqlSelectDifferences());
        clipboard.setContent(content);
    }

    @FXML
    private void onExportCsvButtonClicked() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Salvar CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Arquivo CSV", "*.csv"));
        java.io.File file = fileChooser.showSaveDialog(currentStage);
        if (file == null) return;

        List<String> headerColumns = new ArrayList<>();
        
        // Add identifier columns (names)
        List<String> identifierNames = tableComparisonResultViewModel.getModel()
                .getComparedTable()
                .getOrderedComparedTableColumns()
                .stream()
                .filter(c -> c.getColumnSetting().isIdentifier())
                .map(ComparedTableColumn::getColumnName)
                .toList();

        headerColumns.addAll(identifierNames);

        // Add comparable columns with sourceIds
        List<ComparedTableColumn> comparableColumns = tableComparisonResultViewModel.getModel()
                .getComparedTable()
                .getOrderedComparedTableColumns()
                .stream()
                .filter(c -> c.getColumnSetting().isComparable())
                .toList();

        // Get sourceIds in order
        List<String> sourceIds = new ArrayList<>(tableComparisonResultViewModel.getModel().getComparedTable().getPerSourceTable().keySet())
                .stream()
                .toList();

        List<String> headers = new ArrayList<>();
        headers.addAll(identifierNames);
        for (ComparedTableColumn col : comparableColumns) {
            for (String sourceId : sourceIds) {
                headers.add(col.getColumnName() + "[" + sourceId + "]");
            }
        }

        // Use CsvExporter with RowDifferenceViewModel data and a lambda to extract each row's values
        CsvExporter<RowDifferenceViewModel> exporter = new CsvExporter<>(
                headers,
                new ArrayList<>(diffRowViewModels),
                (rowVm) -> {
                    List<String> rowValues = new ArrayList<>();
                    for (String idName : identifierNames) {
                        String val = rowVm.getIdentifierColumnViewModels().stream()
                                .filter(vm -> vm.getColumnName().equals(idName))
                                .map(IdentifierColumnViewModel::getValue)
                                .findFirst()
                                .orElse("");
                        rowValues.add(val);
                    }
                    for (ComparedTableColumn col : comparableColumns) {
                        Map<String, String> perSourceValues = rowVm.getComparableColumnViewModels().stream()
                                .filter(vm -> vm.getModel().getComparedTableColumn().equals(col))
                                .map(ComparableColumnViewModel::getPerSourceValue)
                                .findFirst()
                                .orElse(Collections.emptyMap());
                        for (String sourceId : sourceIds) {
                            rowValues.add(perSourceValues.getOrDefault(sourceId, ""));
                        }
                    }
                    return rowValues;
                }
        );

        try {
            exporter.exportToFile(file);
        } catch (IOException e) {
            DialogUtils.showWarning(currentStage,
                    "Erro ao exportar CSV", e.getMessage());
        }
    }

    @FXML
    private void onCloseButtonClicked() {
        if (currentStage != null) {
            currentStage.hide();
        }
    }


}
