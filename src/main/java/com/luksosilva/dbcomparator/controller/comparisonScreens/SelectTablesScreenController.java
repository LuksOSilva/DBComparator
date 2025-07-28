package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SelectTablesScreenController {

    private Scene previousScene;
    private Stage currentStage;
    private Scene nextScene;


    private Comparison comparison;
    private Map<String, Map<ComparedSource, SourceTable>> groupedTables;
    private List<String> selectedTableNames = new ArrayList<>();

    private ObservableList<TitledPane> allTablePanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTablePanes;

    private class TableSourceStats {
        SimpleStringProperty sourceName;
        SimpleIntegerProperty rowCount;
        SimpleIntegerProperty columnCount;

        public TableSourceStats(String sourceName, int rowCount, int columnCount) {
            this.sourceName = new SimpleStringProperty(sourceName);
            this.rowCount = new SimpleIntegerProperty(rowCount);
            this.columnCount = new SimpleIntegerProperty(columnCount);
        }

        // Getters for properties
        public SimpleStringProperty sourceNameProperty() {
            return sourceName;
        }
        public SimpleIntegerProperty rowCountProperty() {
            return rowCount;
        }
        public SimpleIntegerProperty columnCountProperty() {
            return columnCount;
        }
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    public void setPreviousScene(Scene previousScene) { this.previousScene = previousScene; }
    public void setCurrentStage(Stage currentStage) { this.currentStage = currentStage; }
    public void setNextScene(Scene nextScene) {this.nextScene = nextScene; }

    @FXML
    private CheckBox selectAllCheckBox;
    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    public ToggleButton filterToggleButton;
    @FXML
    public HBox filtersHBox;
    @FXML
    public CheckBox showDiffRecordCountOnlyCheckBox;
    @FXML
    public CheckBox showOnlySchemaDiffersCheckBox;
    @FXML
    public CheckBox showSelectedOnlyCheckBox;
    @FXML
    public Accordion tablesAccordion;

    @FXML
    public Button nextStepBtn;
    @FXML
    public Button previousStepBtn;
    @FXML
    public Text cancelBtn;


    @FXML
    public void onFilterButtonClicked(MouseEvent mouseEvent) {
        toggleFilters(filterToggleButton.isSelected());
    }




    public void init() {
        groupedTables = getGroupedTables();
        prepareAccordionInfo();
        setupFilterControls();

    }

    public boolean needToProcess() {
        List<ComparedTable> comparedTables = comparison.getComparedTables();
        List<String> currentTables = selectedTableNames;

        if (comparedTables.size() != currentTables.size()) {
            return true;
        }


        for (String tableName : currentTables) {
            boolean found = comparedTables.stream()
                    .anyMatch(cs -> cs.getTableName().equals(tableName));
            if (!found) {
                return true;
            }
        }

        return false;
    }

    public void onCompareSchemasButtonClicked(ActionEvent actionEvent) {
        Button clickedButton = (Button) actionEvent.getSource();
        String tableName = (String) clickedButton.getUserData();

        System.out.println(tableName);
    }





    private void prepareAccordionInfo() {
        if (comparison == null || groupedTables.isEmpty()) {
            displayNoTablesMessage();
            return;
        }


        tablesAccordion.getPanes().clear(); // Clears Accordion
        allTablePanes.clear();             // Clears master list

        filterToggleButton.setSelected(false); //hides filters

        // Populate allTablePanes (your master list) with all TitledPanes
        allTablePanes.addAll(buildAllTableTitledPanes());

        // Initialize FilteredList based on allTablePanes
        filteredTablePanes = new FilteredList<>(allTablePanes, pane -> true);

        // Set the Accordion's panes to the filtered list. This is done ONCE.
        tablesAccordion.getPanes().setAll(filteredTablePanes);
    }

    private void setupFilterControls() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList("tabela", "coluna"));
        filterTypeComboBox.setValue("tabela");

        selectAllCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (selectAllCheckBox.isIndeterminate()) return;

            for (TitledPane pane : tablesAccordion.getPanes()) {
                Node graphicNode = pane.getGraphic();
                if (graphicNode instanceof HBox hBox) {
                    for (Node node : hBox.getChildren()) {
                        if (node instanceof CheckBox cb) {
                            cb.setSelected(newVal);
                        }
                    }
                }
            }
        });

        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showOnlySchemaDiffersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showDiffRecordCountOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showSelectedOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        applyFilter(); // Apply initial filter (which is "show all" by default)
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
                    Map<ComparedSource, SourceTable> sourceTableMap = groupedTables.get(pane.getText());
                    if (sourceTableMap == null) return false;

                    boolean columnMatch = sourceTableMap.values().stream()
                            .flatMap(st -> st.getSourceTableColumns().stream())
                            .anyMatch(col -> col.getColumnName().toLowerCase().contains(filterText));

                    if (!columnMatch) return false;
                }
            }

            // show only selected filter
            if (showSelectedOnlyCheckBox.isSelected()) {
                if (!selectedTableNames.contains(pane.getText())) return false;
            }

            // show only different record count filter
            if (showDiffRecordCountOnlyCheckBox.isSelected()) {
                Map<ComparedSource, SourceTable> perSource = groupedTables.get(pane.getText());
                if (perSource == null) return false;

                Set<Integer> rowCounts = new HashSet<>();
                for (SourceTable st : perSource.values()) {
                    rowCounts.add(st.getRecordCount());
                }
                if (rowCounts.size() <= 1) return false;
            }

            // show only different schema
            if (showOnlySchemaDiffersCheckBox.isSelected()) {
                int totalSources = comparison.getComparedSources().size();
                Map<ComparedSource, SourceTable> perSource = groupedTables.get(pane.getText());
                if (perSource == null) return false;

                // only checks for schema difference if table exists in all sources.
                // this makes it so that the filter 'show only when schema differs' also shows tables that don't exist in all sources.
                if (totalSources == perSource.size()) {
                    Collection<SourceTable> sourceTables = perSource.values();
                    if (sourceTables.isEmpty()) return false;

                    SourceTable first = sourceTables.iterator().next();
                    boolean allMatch = sourceTables.stream().allMatch(first::equalSchema);

                    if (allMatch) {
                        return false;
                    }
                }

            }
            return true;
        });

        // Atualiza os panes
        tablesAccordion.getPanes().setAll(filteredTablePanes);
        updateSelectAllCheckBoxState();

        // Faz um fade-in suave no accordion
        fadeInAccordion();
    }

    public void toggleFilters(boolean show) {
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


    // --- Helper Methods ---
    private void fadeInAccordion() {
        tablesAccordion.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(250), tablesAccordion);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void displayNoTablesMessage() {
        tablesAccordion.getPanes().clear();
        tablesAccordion.getPanes().add(new TitledPane("No Tables Found", new Label("No table metadata available for comparison.")));
    }


    private List<TitledPane> buildAllTableTitledPanes() {
        List<TitledPane> titledPaneList = new ArrayList<>();

        List<String> tableNames = groupedTables.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        for (String tableName : tableNames) {
            TitledPane tablePane = new TitledPane();
            tablePane.setText(tableName);

            tablePane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && tablePane.getUserData() == null) {
                    TableView<TableSourceStats> tableView = buildTableMetadata(tableName);
                    HBox buttonBox = buildButtonBox(tableName);
                    VBox contentContainer = new VBox(tableView, buttonBox);
                    tablePane.setContent(contentContainer);
                    tablePane.setUserData(true);
                }
            });

            HBox graphicBox = new HBox();
            graphicBox.setAlignment(Pos.CENTER_RIGHT);
            graphicBox.setMaxWidth(Double.MAX_VALUE);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            boolean tableExistsInAllSources = groupedTables.get(tableName).size() == comparison.getComparedSources().size();
            boolean tableHasRecordsInAllSources = groupedTables.get(tableName).values().stream().noneMatch(sourceTable -> sourceTable.getRecordCount() == 0);



            CheckBox selectCheckBox = new CheckBox();
            selectCheckBox.setDisable(!tableExistsInAllSources || !tableHasRecordsInAllSources);
            selectCheckBox.setSelected(selectedTableNames.contains(tableName));
            selectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (selectCheckBox.isDisable()) return;
                if (newVal) selectedTableNames.add(tableName);
                else selectedTableNames.remove(tableName);
                updateSelectAllCheckBoxState();
            });
            if (!tableExistsInAllSources) {
                Tooltip tooltip = new Tooltip("Esta tabela não está presente em todas as fontes.");
                tooltip.setShowDelay(Duration.millis(200));
                Tooltip.install(tablePane, tooltip);
            } else if (!tableHasRecordsInAllSources) {
                Tooltip tooltip = new Tooltip("Esta tabela não possui registro em todas as fontes");
                tooltip.setShowDelay(Duration.millis(200));
                Tooltip.install(tablePane, tooltip);
            }

            graphicBox.getChildren().addAll(spacer, selectCheckBox);
            tablePane.setGraphic(graphicBox);

            titledPaneList.add(tablePane);
        }
        return titledPaneList;
    }


    private TableView<TableSourceStats> buildTableMetadata(String tableName) {
        final double TABLE_ROW_HEIGHT = 28.0;
        final double TABLE_HEADER_HEIGHT = 30.0;

        Map<ComparedSource, SourceTable> perSourceTable = groupedTables.get(tableName);

        TableView<TableSourceStats> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No data for this table from attached sources."));

        TableColumn<TableSourceStats, String> sourceColumn = new TableColumn<>("Fonte");
        sourceColumn.setCellValueFactory(cellData -> cellData.getValue().sourceNameProperty());

        TableColumn<TableSourceStats, Integer> rowCountColumn = new TableColumn<>("Nº de Registros");
        rowCountColumn.setCellValueFactory(cellData -> cellData.getValue().rowCountProperty().asObject());

        TableColumn<TableSourceStats, Integer> columnCountColumn = new TableColumn<>("Nº de Colunas");
        columnCountColumn.setCellValueFactory(cellData -> cellData.getValue().columnCountProperty().asObject());

        tableView.getColumns().addAll(sourceColumn, rowCountColumn, columnCountColumn);

        ObservableList<TableSourceStats> tableStats = FXCollections.observableArrayList();

        if (perSourceTable != null) {
            for (Map.Entry<ComparedSource, SourceTable> entry : perSourceTable.entrySet()) {
                ComparedSource comparedSource = entry.getKey();
                SourceTable sourceTable = entry.getValue();

                String sourceName = comparedSource.getSourceId();
                int rowCount = sourceTable.getRecordCount();
                int columnCount = sourceTable.getSourceTableColumns().size();

                tableStats.add(new TableSourceStats(sourceName, rowCount, columnCount));
            }
        }
        tableView.setItems(tableStats);

        double calculatedPrefHeight = (tableStats.size() * TABLE_ROW_HEIGHT) + TABLE_HEADER_HEIGHT;
        tableView.setPrefHeight(Math.max(calculatedPrefHeight, TABLE_HEADER_HEIGHT));

        return tableView;
    }

    private HBox buildButtonBox(String tableName) {
        // Create the button
        Button compareSchemas = new Button("Comparar Schemas");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Put them in an HBox
        HBox buttonBox = new HBox(10, spacer, compareSchemas);
        buttonBox.setAlignment(Pos.CENTER_RIGHT); // Right-aligned
        buttonBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding

        // Add logic for the buttons
        compareSchemas.setOnAction(this::onCompareSchemasButtonClicked);

        // Add user data
        compareSchemas.setUserData(tableName);

        return buttonBox;
    }



    private Map<String, Map<ComparedSource, SourceTable>> getGroupedTables () {
        Map<String, Map<ComparedSource, SourceTable>> groupedTables = new HashMap<>();

        if (comparison != null && comparison.getComparedSources() != null) {
            for (ComparedSource comparedSource : comparison.getComparedSources()) {
                if (comparedSource.getSource() != null && comparedSource.getSource().getSourceTables() != null) {
                    for (SourceTable sourceTable : comparedSource.getSource().getSourceTables()) {
                        String tableName = sourceTable.getTableName();

                        groupedTables
                                .computeIfAbsent(tableName, k -> new HashMap<>())
                                .put(comparedSource, sourceTable);
                    }
                }
            }
        }
        return groupedTables;
    }

    private void updateSelectAllCheckBoxState() {
        boolean allSelected = true;
        boolean noneSelected = true;

        for (TitledPane pane : filteredTablePanes) { // Só os visíveis!
            Node graphicNode = pane.getGraphic();
            if (graphicNode instanceof HBox hBox) {
                for (Node node : hBox.getChildren()) {
                    if (node instanceof CheckBox cb) {
                        if (cb.isSelected()) {
                            noneSelected = false;
                        } else {
                            allSelected = false;
                        }
                    }
                }
            }
        }

        if (allSelected) {
            selectAllCheckBox.setSelected(true);
            selectAllCheckBox.setIndeterminate(false);
        } else if (noneSelected) {
            selectAllCheckBox.setSelected(false);
            selectAllCheckBox.setIndeterminate(false);
        } else {
            selectAllCheckBox.setIndeterminate(true);
        }
    }

    // --- Navigation Steps ---

    public void nextStep(MouseEvent mouseEvent) {


        if (selectedTableNames.isEmpty()) {
            DialogUtils.showWarning("Nenhuma tabela selecionada.", "Selecione ao menos uma tabela para prosseguir com a comparação.");
            return;
        }

        Scene currentScene = currentStage.getScene();
        currentScene.setUserData(SelectTablesScreenController.this);

        if (!needToProcess() && nextScene != null) {
            currentStage.setScene(nextScene);
            return;
        }

        try {
            FxLoadResult<Parent, LoadingScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.LOADING_SCREEN);

            Parent root = screenData.node;
            LoadingScreenController controller = screenData.controller;

            controller.setMessage("Processando tabelas, aguarde...");

            Scene scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            currentStage.setScene(scene);
            currentStage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela de carregamento: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        Task<Parent> processTablesTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {



                comparison.getComparedTables()
                        .removeIf(comparedTable -> !selectedTableNames.contains(comparedTable.getTableName()));

                Map<String, Map<ComparedSource, SourceTable>> notProcessedSelectedGroupedTables =
                        groupedTables.entrySet().stream()
                                .filter(groupedTable -> comparison.getComparedTables().stream()
                                        .noneMatch(comparedTable -> comparedTable.getTableName().equals(groupedTable.getKey())))
                                .filter(entry -> selectedTableNames.contains(entry.getKey()))
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                ));


                ComparisonService.processTables(comparison, notProcessedSelectedGroupedTables);


                FxLoadResult<Parent, ColumnSettingsScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.COLUMN_SETTINGS_SCREEN);

                Parent nextScreenRoot = screenData.node;
                ColumnSettingsScreenController controller = screenData.controller;

                controller.setCurrentStage(currentStage);
                controller.setPreviousScene(currentScene);
                controller.setComparison(comparison);
                controller.init();

                return nextScreenRoot;
            }
        };


        processTablesTask.setOnSucceeded(event -> {
            try {

                Parent nextScreenRoot = processTablesTask.getValue();

                Scene nextScreenScene = new Scene(nextScreenRoot, currentScene.getWidth(), currentScene.getHeight());

                currentStage.setScene(nextScreenScene);

            } catch (Exception e) {
                DialogUtils.showError("Erro de Transição", "Não foi possível exibir a próxima tela: " + e.getMessage());
                e.printStackTrace();
            }
        });


        processTablesTask.setOnFailed(event -> {
            DialogUtils.showError("Erro de Processamento", "Ocorreu um erro durante o processamento: " + processTablesTask.getException().getMessage());
            processTablesTask.getException().printStackTrace();

            try {
                FxLoadResult<Parent, SelectTablesScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.SELECT_TABLES_SCREEN);

                Parent root = screenData.node;

                Scene currentScreenScene = new Scene(root);
                currentStage.setScene(currentScreenScene);

            } catch (IOException e) {
                DialogUtils.showError("Erro de Recuperação", "Não foi possível recarregar a tela anterior: " + e.getMessage());
                e.printStackTrace();
            }
        });


        new Thread(processTablesTask).start();

    }

    public void previousStep(MouseEvent mouseEvent) {

        double width = currentStage.getWidth();
        double height = currentStage.getHeight();

        AttachSourcesScreenController attachSourcesScreenController = (AttachSourcesScreenController) previousScene.getUserData();
        attachSourcesScreenController.setNextScene(currentStage.getScene());

        currentStage.setScene(previousScene);

        currentStage.setWidth(width);
        currentStage.setHeight(height);

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
            Scene scene = new Scene(root, currentStage.getWidth(), currentStage.getHeight());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela inicial: " + e.getMessage());
            e.printStackTrace();
        }
    }
}