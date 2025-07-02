package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.exception.ColumnSettingsException;
import com.luksosilva.dbcomparator.model.comparison.*;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnSettingsScreenController {

    private Scene previousScene;
    private Stage currentStage;
    private Scene nextScene;

    private class ComparedTableColumnViewModel {
        ComparedTableColumn comparedTableColumn;

        private final SimpleBooleanProperty identifierProperty = new SimpleBooleanProperty();
        private final SimpleBooleanProperty comparableProperty = new SimpleBooleanProperty();

        private final SimpleBooleanProperty defaultIdentifierProperty = new SimpleBooleanProperty();
        private final SimpleBooleanProperty defaultComparableProperty = new SimpleBooleanProperty();

        public ComparedTableColumnViewModel(ComparedTableColumn comparedTableColumn) {
            this.comparedTableColumn = comparedTableColumn;

            setProperties();
            setDefault();

            // Radio button-like behavior: selecting one disables the other
            identifierProperty.addListener((obs, oldVal, newVal) -> {
                if (newVal) comparableProperty.set(false);
            });

            comparableProperty.addListener((obs, oldVal, newVal) -> {
                if (newVal) identifierProperty.set(false);
            });
        }

        public boolean isAltered() {
            return (identifierProperty.get() != defaultIdentifierProperty.get())
                    || (comparableProperty.get() != defaultComparableProperty.get());
        }
        public boolean existsInAllSources() {
            return comparedTableColumn.getPerSourceTableColumn().size() == comparison.getComparedSources().size();
        }

        public ComparedTableColumnSettings getViewModelColumnSetting() {
            return new ComparedTableColumnSettings(comparableProperty.get(), identifierProperty.get());
        }

        public String getPrimaryKeyCountText() {
            Map<ComparedSource, SourceTableColumn> map = comparedTableColumn.getPerSourceTableColumn();

            long pkCount = map.values().stream().filter(SourceTableColumn::isPk).count();
            int totalSources = comparison.getComparedSources().size();

            if (pkCount == 0) return "";
            if (pkCount == totalSources) return "Y";

            return pkCount + "/" + totalSources;
        }

        public void setProperties() {
            this.identifierProperty.set(comparedTableColumn.getColumnSetting().isIdentifier());
            this.comparableProperty.set(comparedTableColumn.getColumnSetting().isComparable());
        }

        public void setDefault() {
            this.defaultIdentifierProperty.set(comparedTableColumn.getColumnSetting().isIdentifier());
            this.defaultComparableProperty.set(comparedTableColumn.getColumnSetting().isComparable());
        }
    }

    private Comparison comparison;
    private final ObservableList<TitledPane> allTablePanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTablePanes;

    Map<String, List<ComparedTableColumnViewModel>> perTableComparedColumnViewModel = new HashMap<>();

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    public void setPreviousScene(Scene previousScene) { this.previousScene = previousScene; }
    public void setCurrentStage(Stage currentStage) { this.currentStage = currentStage; }

    public void setNextScene(Scene nextScene) { this.nextScene = nextScene; }

    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    public ToggleButton filterToggleButton;
    @FXML
    public HBox filtersHBox;
    @FXML
    public CheckBox showOnlySchemaDiffersCheckBox;
    @FXML
    public CheckBox showOnlyInvalidColumnSettingsCheckBox;
    @FXML
    public CheckBox showOnlyAlteredCheckBox;
    @FXML
    public Accordion tablesAccordion;

    @FXML
    public Button nextStepBtn;
    @FXML
    public Button previousStepBtn;
    @FXML
    public Text cancelBtn;



    public void init() {
        setupViewModels();
        constructAccordion();
        setupFilterControls();

    }

    public boolean needToProcess() {
        return !getTableNamesWithAlteredColumns().isEmpty();
    }



    /// User-called Methods

    public void onFilterButtonClicked(MouseEvent mouseEvent) {
        toggleFilters(filterToggleButton.isSelected());
    }
    public void onSaveSettingsForAllAlteredTablesButtonClicked(ActionEvent event) {
        boolean confirm = DialogUtils.askConfirmation("Salvar Alterados?",
                "Todas as tabelas que foram alteradas serão salvas. Salvamentos prévios serão perdidos.");
        if (!confirm) {
            return;
        }

        boolean saveAsDefault = true;

        saveSettingsForAllAlteredTables(getTableNamesWithAlteredColumns(), saveAsDefault);

        if (hasAnyInvalidColumnSettings()){
            showErrorInvalidSettings();
        }
    }
    public void onResetSettingsToDefaultForAllTablesButtonClicked(ActionEvent event) {
        boolean confirm = DialogUtils.askConfirmation("Alterar todas para padrão?",
                "As configurações de todas as tabelas serão alteradas para o padrão. Configurações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean loadFromDb = true;

        resetSettingsForAllTables(loadFromDb);
    }
    public void onResetSettingsToOriginalForAllTablesButtonClicked(ActionEvent event) {
        boolean confirm = DialogUtils.askConfirmation("Alterar todas para padrão do sistema?",
                "As configurações de todas as tabelas serão alteradas para o padrão do sistema. Configurações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean loadFromDb = false;

        resetSettingsForAllTables(loadFromDb);
    }
    public void onSaveSettingsForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String tableName = (String) clickedButton.getUserData();

        boolean confirm = DialogUtils.askConfirmation("Salvar Configurações?",
                "As configurações da tabela " + tableName + " serão salvas. Se houver um salvamento prévio, será perdido.");
        if (!confirm) {
            return;
        }


        boolean saveAsDefault = true;

        saveSettingsForTable(tableName, saveAsDefault);

        if (hasInvalidColumnSettings(tableName)){
            showErrorInvalidSettings(tableName);
        }
    }
    public void onResetSettingsToDefaultForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String tableName = (String) clickedButton.getUserData();

        boolean confirm = DialogUtils.askConfirmation("Alterar para padrão?",
                "As configurações da tabela "+ tableName +" serão alteradas para o padrão. Alterações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean loadFromDb = true;

        resetSettingsForTable(tableName, loadFromDb);
    }
    public void onResetSettingsToOriginalForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String tableName = (String) clickedButton.getUserData();

        boolean confirm = DialogUtils.askConfirmation("Alterar para padrão?",
                "As configurações da tabela "+ tableName +" serão alteradas para o padrão do sistema. Alterações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean loadFromDb = false;

        resetSettingsForTable(tableName, loadFromDb);
    }

    /// Helper Methods

    private void displayNoTablesMessage() {
        tablesAccordion.getPanes().clear();
        tablesAccordion.getPanes().add(new TitledPane("No Tables Found", new Label("No table metadata available for comparison.")));
    }

    private void fadeInAccordion() {
        tablesAccordion.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(250), tablesAccordion);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void toggleFilters(boolean show) {
        if (show) {
            // Show with animation
            filtersHBox.setVisible(true);
            filtersHBox.setManaged(true);
            filtersHBox.setOpacity(0.0);
            filtersHBox.setTranslateY(-10);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), filtersHBox);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), filtersHBox);
            slideDown.setFromY(-10);
            slideDown.setToY(0);

            new ParallelTransition(fadeIn, slideDown).play();
        } else {
            // Hide with animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), filtersHBox);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            TranslateTransition slideUp = new TranslateTransition(Duration.millis(200), filtersHBox);
            slideUp.setFromY(0);
            slideUp.setToY(-10);

            ParallelTransition hideTransition = new ParallelTransition(fadeOut, slideUp);
            hideTransition.setOnFinished(event -> {
                filtersHBox.setVisible(false);
                filtersHBox.setManaged(false);
            });

            hideTransition.play();
        }
    }

    private void resetSettingsForAllTables(boolean loadFromDb) {

        List<String> tableNames = allTablePanes.stream().map(Labeled::getText).toList();

        for (String tableName : tableNames) {
            resetSettingsForTable(tableName, loadFromDb);
        }
    }

    private void saveSettingsForAllAlteredTables(List<String> tableNames, boolean saveAsDefault) {

        for (String tableName : tableNames) {

            saveSettingsForTable(tableName, saveAsDefault);
        }
    }

    private void resetSettingsForTable(String tableName, boolean loadFromDb) {
        ComparedTable comparedTable = getComparedTableFromTableName(tableName);
        if (comparedTable == null) return;

        List<ComparedTableColumnViewModel> comparedTableColumnViewModelList =
                perTableComparedColumnViewModel.get(tableName);
        if (comparedTableColumnViewModelList.isEmpty()) return;

        List<ComparedTable> comparedTableList = new ArrayList<>();
        comparedTableList.add(comparedTable);
        ComparisonService.setTableColumnsSettings(comparedTableList, loadFromDb);


        comparedTableColumnViewModelList.forEach(ComparedTableColumnViewModel::setProperties);
    }

    private void saveSettingsForTable(String tableName, boolean saveAsDefault) {
        ComparedTable comparedTable = getComparedTableFromTableName(tableName);
        if (comparedTable == null) return;

        List<ComparedTableColumnViewModel> comparedTableColumnViewModelList =
                perTableComparedColumnViewModel.get(comparedTable.getTableName());
        if (comparedTableColumnViewModelList.isEmpty()) return;

        Map<ComparedTableColumn, ComparedTableColumnSettings> perComparedTableColumnSettings =
                comparedTableColumnViewModelList.stream()
                        .collect(Collectors.toMap(
                                vm -> vm.comparedTableColumn,
                                ComparedTableColumnViewModel::getViewModelColumnSetting
                        ));

        //validate column setting
        ComparisonService.validateColumnSettings(comparedTable, perComparedTableColumnSettings);
        if (comparedTable.isColumnSettingsInvalid()) return;

        //saves default
        ComparisonService.processColumnSettings(comparedTable, perComparedTableColumnSettings, saveAsDefault);

        //updates default values
        comparedTableColumnViewModelList.forEach(ComparedTableColumnViewModel::setDefault);
    }

    private boolean hasAnyInvalidColumnSettings() {
        return comparison.getComparedTables().stream().anyMatch(ComparedTable::isColumnSettingsInvalid);
    }
    private boolean hasInvalidColumnSettings(ComparedTable comparedTable) {
        return comparedTable.isColumnSettingsInvalid();
    }
    private boolean hasInvalidColumnSettings(String tableName) {
        ComparedTable comparedTable = getComparedTableFromTableName(tableName);
        return comparedTable.isColumnSettingsInvalid();
    }

    private void showErrorInvalidSettings() {
        if (!hasAnyInvalidColumnSettings()) return;

        List<ComparedTable> tablesWithInvalidSettings = comparison.getComparedTables().stream()
                .filter(ComparedTable::isColumnSettingsInvalid).toList();

        DialogUtils.showInvalidColumnSettingsDialog(currentStage, tablesWithInvalidSettings);

    }
    private void showErrorInvalidSettings(String tableName) {
        ComparedTable comparedTable = getComparedTableFromTableName(tableName);
        if (comparedTable == null) return;
        if (!hasInvalidColumnSettings(comparedTable)) return;

        List<ComparedTable> comparedTableToList = new ArrayList<>();
        comparedTableToList.add(comparedTable);

        DialogUtils.showInvalidColumnSettingsDialog(currentStage, comparedTableToList);

    }

    private ComparedTable getComparedTableFromTableName(String tableName) {
        return comparison.getComparedTables().stream()
                .filter(ct -> ct.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
    }

    private List<String> getTableNamesWithAlteredColumns() {
        return perTableComparedColumnViewModel.entrySet()
                .stream()
                .filter(entry -> entry.getValue().stream().anyMatch(ComparedTableColumnViewModel::isAltered))
                .map(Map.Entry::getKey)
                .toList();
    }

    private void changeCursorTo(Cursor cursor) {
        currentStage.getScene().setCursor(cursor);
    }

    /// Constructor Methods

    private  void setupViewModels() {
        for (ComparedTable comparedTable : comparison.getComparedTables()) {
            List<ComparedTableColumnViewModel> comparedTableColumnViewModelList = comparedTable.getComparedTableColumns().stream()
                    .map(ComparedTableColumnViewModel::new)
                    .toList();

            perTableComparedColumnViewModel.put(comparedTable.getTableName(), comparedTableColumnViewModelList);
        }
    }

    private void setupFilterControls() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList("tabela", "coluna"));
        filterTypeComboBox.setValue("tabela");


        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showOnlyInvalidColumnSettingsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showOnlySchemaDiffersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showOnlyAlteredCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        applyFilter();
    }

    private void applyFilter() {
        String filterText = searchTextField.getText().toLowerCase().trim();
        String filterType = filterTypeComboBox.getValue();

        filteredTablePanes.setPredicate(pane -> {
            String tableName = pane.getText().toLowerCase();

            // Filter by text
            if (!filterText.isEmpty()) {
                if ("tabela".equalsIgnoreCase(filterType)) {
                    if (!tableName.contains(filterText)) return false;
                } else if ("coluna".equalsIgnoreCase(filterType)) {

                    ComparedTable comparedTable = comparison.getComparedTables().stream()
                            .filter(ct -> ct.getTableName().equals(pane.getText()))
                            .findFirst()
                            .orElse(null);

                    if (comparedTable == null) return false;

                    boolean columnMatch = comparedTable.getComparedTableColumns().stream()
                            .anyMatch(comparedTableColumn -> comparedTableColumn.getColumnName().contains(filterText));

                    if (!columnMatch) return false;
                }
            }

            //show only altered filter
            if (showOnlyAlteredCheckBox.isSelected()) {
                if (!getTableNamesWithAlteredColumns().contains(pane.getText())) return false;
            }

            //show only if schema differs filter
            if (showOnlySchemaDiffersCheckBox.isSelected()) {
                ComparedTable comparedTable = getComparedTableFromTableName(pane.getText());
                if (!comparedTable.hasSchemaDifference()) return false;
            }

            //show only invalid column settings
            if (showOnlyInvalidColumnSettingsCheckBox.isSelected()) {
                ComparedTable comparedTable = getComparedTableFromTableName(pane.getText());
                if (comparedTable.isColumnSettingsValid()) return false;
            }


            return true;
        });

        // Atualiza os panes
        tablesAccordion.getPanes().setAll(filteredTablePanes);

        // Faz um fade-in suave no accordion
        fadeInAccordion();
    }

    private void constructAccordion() {
        if (comparison.getComparedTables() == null ) {
            displayNoTablesMessage();
            return;
        }

        tablesAccordion.getPanes().clear(); // Clears Accordion
        allTablePanes.clear();             // Clears master list

        filterToggleButton.setSelected(false); //hides filters

        // Populate allTablePanes with all TitledPanes
        allTablePanes.addAll(constructTitledPanes());

        // Initialize FilteredList based on allTablePanes
        filteredTablePanes = new FilteredList<>(allTablePanes, pane -> true);

        // Set the Accordion's panes to the filtered list.
        tablesAccordion.getPanes().setAll(filteredTablePanes);
    }


    private List<TitledPane> constructTitledPanes() {
        List<TitledPane> titledPaneList = new ArrayList<>();

        List<String> tableNames = comparison.getComparedTables().stream()
                .map(ComparedTable::getTableName)
                .toList();

        for (String tableName : tableNames) {
            TitledPane tablePane = new TitledPane();
            tablePane.setText(tableName);


            tablePane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && tablePane.getUserData() == null) {
                    constructTitledPaneContent(tablePane);
                }
            });

            titledPaneList.add(tablePane);
        }
        return titledPaneList;
    }

    private void constructTitledPaneContent(TitledPane titledPane) {

        TableView<ComparedTableColumnViewModel> tableView = constructTitledPaneContentTableView(titledPane.getText());
        HBox buttonBox = constructTitledPaneContentButtonBox(titledPane.getText());

        // Combine the table and buttons in a VBox
        VBox contentContainer = new VBox(10, tableView, buttonBox);
        contentContainer.setPadding(new Insets(10));
        contentContainer.setFillWidth(true);

        titledPane.setContent(contentContainer);
        titledPane.setUserData(true);

    }

    private TableView<ComparedTableColumnViewModel> constructTitledPaneContentTableView(String tableName) {
        final double TABLE_ROW_HEIGHT = 28.0;
        final double TABLE_HEADER_HEIGHT = 30.0;

        List<ComparedTableColumnViewModel> comparedTableColumnViewModelList = perTableComparedColumnViewModel.get(tableName);

        TableView<ComparedTableColumnViewModel> tableView = new TableView<>();
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ComparedTableColumnViewModel item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                    setTooltip(null);
                } else if (!item.existsInAllSources()) {
                    setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.6;");
                    setTooltip(new Tooltip("Esta coluna não existe em todas as fontes."));
                    getTooltip().setShowDelay(Duration.millis(200));
                } else {
                    setStyle(""); // Reset style
                    setTooltip(null);
                }
            }
        });
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No data for this table from attached sources."));

        TableColumn<ComparedTableColumnViewModel, Number> rowIndexColumn = new TableColumn<>("#");
        rowIndexColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(tableView.getItems().indexOf(cellData.getValue()) + 1));
        rowIndexColumn.setSortable(false);

        TableColumn<ComparedTableColumnViewModel, String> columnNameColumn = new TableColumn<>("Coluna");
        columnNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().comparedTableColumn.getColumnName()));

        TableColumn<ComparedTableColumnViewModel, String> pkColumn = new TableColumn<>("PK");
        pkColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrimaryKeyCountText()));

        TableColumn<ComparedTableColumnViewModel, Boolean> isIdentifierColumn = new TableColumn<>("Identificador");
        isIdentifierColumn.setCellValueFactory(cellData -> cellData.getValue().identifierProperty);
        isIdentifierColumn.setCellFactory(col -> new CheckBoxTableCell<>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    ComparedTableColumnViewModel viewModel = getTableView().getItems().get(getIndex());
                    setDisable(!viewModel.existsInAllSources());
                }
            }
        });


        TableColumn<ComparedTableColumnViewModel, Boolean> isComparableColumn = new TableColumn<>("Comparável");
        isComparableColumn.setCellValueFactory(cellData -> cellData.getValue().comparableProperty);
        isComparableColumn.setCellFactory(col -> new CheckBoxTableCell<>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    ComparedTableColumnViewModel viewModel = getTableView().getItems().get(getIndex());
                    setDisable(!viewModel.existsInAllSources());
                }
            }
        });


        tableView.getColumns().addAll(rowIndexColumn, columnNameColumn, pkColumn, isIdentifierColumn, isComparableColumn);


        ObservableList<ComparedTableColumnViewModel> tableItems = FXCollections.observableArrayList();

        tableItems.addAll(comparedTableColumnViewModelList);

        tableView.setItems(tableItems);

        double calculatedPrefHeight = (tableItems.size() * TABLE_ROW_HEIGHT) + TABLE_HEADER_HEIGHT;
        tableView.setPrefHeight(Math.max(calculatedPrefHeight, TABLE_HEADER_HEIGHT));

        tableView.setEditable(true);
        isIdentifierColumn.setEditable(true);
        isComparableColumn.setEditable(true);


        return tableView;
    }

    private HBox constructTitledPaneContentButtonBox(String tableName) {
        // Create the buttons
        Button resetToOriginal = new Button("Alterar para padrão do sistema");
        Button resetToDefaultBtn = new Button("Alterar para padrão");
        Button saveAsDefaultBtn = new Button("Salvar como padrão");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Put them in an HBox
        HBox buttonBox = new HBox(10, resetToOriginal, spacer, resetToDefaultBtn, saveAsDefaultBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT); // Right-aligned
        buttonBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding

        // Add logic for the buttons
        resetToOriginal.setOnAction(this::onResetSettingsToOriginalForTableButtonClicked);
        resetToDefaultBtn.setOnAction(this::onResetSettingsToDefaultForTableButtonClicked);
        saveAsDefaultBtn.setOnAction(this::onSaveSettingsForTableButtonClicked);

        // Add user data
        resetToOriginal.setUserData(tableName);
        resetToDefaultBtn.setUserData(tableName);
        saveAsDefaultBtn.setUserData(tableName);

        return buttonBox;
    }



    /// Navigation Methods

    public void nextStep(MouseEvent mouseEvent) {

        Scene currentScene = currentStage.getScene();
        currentScene.setUserData(ColumnSettingsScreenController.this);


        if (!needToProcess() && nextScene != null) {
            currentStage.setScene(nextScene);
            return;
        }

        try {
            FxLoadResult<Parent, LoadingScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.LOADING_SCREEN);

            Parent root = screenData.node;
            LoadingScreenController controller = screenData.controller;

            controller.setMessage("Validando configurações, aguarde...");

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela de carregamento: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        Task<Parent> processColumnSettingsTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {


                boolean saveAsDefault = false;

                saveSettingsForAllAlteredTables(perTableComparedColumnViewModel.keySet().stream().toList(), saveAsDefault);

                if (hasAnyInvalidColumnSettings()) {
                    throw new ColumnSettingsException(
                            comparison.getComparedTables().stream()
                                    .filter(ComparedTable::isColumnSettingsInvalid).toList());
                }

                FxLoadResult<Parent, SetFiltersScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.SET_FILTERS_SCREEN);

                Parent nextScreenRoot = screenData.node;
                SetFiltersScreenController controller = screenData.controller;

                controller.setCurrentStage(currentStage);
                controller.setPreviousScene(currentScene);
                controller.setComparison(comparison);
                controller.init();

                return nextScreenRoot;
            }
        };


        processColumnSettingsTask.setOnSucceeded(event -> {
            try {

                Parent nextScreenRoot = processColumnSettingsTask.getValue();

                Scene nextScreenScene = new Scene(nextScreenRoot);

                currentStage.setScene(nextScreenScene);

            } catch (Exception e) {
                DialogUtils.showError("Erro de Transição", "Não foi possível exibir a próxima tela: " + e.getMessage());
                e.printStackTrace();
            }
        });


        processColumnSettingsTask.setOnFailed(event -> {
            currentStage.setScene(currentScene);

            Throwable exception = processColumnSettingsTask.getException();

            if (exception instanceof ColumnSettingsException) {
                showErrorInvalidSettings();
                return;
            }

            DialogUtils.showError("Erro de Processamento",
                    "Ocorreu um erro inesperado: " + exception.getMessage());
            exception.printStackTrace();
        });


        new Thread(processColumnSettingsTask).start();

    }

    public void previousStep(MouseEvent mouseEvent) {

        SelectTablesScreenController selectTablesScreenController = (SelectTablesScreenController) previousScene.getUserData();
        selectTablesScreenController.setNextScene(currentStage.getScene());

        currentStage.setScene(previousScene);

    }

    public void cancelComparison(MouseEvent mouseEvent) {
        boolean confirmCancel = DialogUtils.askConfirmation("Cancelar comparação",
                "Deseja realmente cancelar essa comparação? Nenhuma informação será salva");
        if (!confirmCancel) {
            return;
        }

        try {
            FxLoadResult<Parent, AttachSourcesScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.HOME_SCREEN);

            Parent root = screenData.node;

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela inicial: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
