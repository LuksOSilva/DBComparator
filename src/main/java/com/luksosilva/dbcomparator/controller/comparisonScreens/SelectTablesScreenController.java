package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.controller.HomeScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.navigator.ComparisonStepsNavigator;
import com.luksosilva.dbcomparator.service.ComparedTableService;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.service.SourceService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.viewmodel.live.source.SourceTableViewModel;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
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

public class SelectTablesScreenController implements BaseController {

    private ComparisonStepsNavigator navigator;

    private Stage currentStage;

    private final Comparison comparison = new Comparison();

    List<ComparedTable> comparedTableList = new ArrayList<>();

    private ObservableList<TitledPane> allTablePanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTablePanes;

    private List<String> selectedTableNames = new ArrayList<>();
    private List<Stage> compareSchemaOpenedStages = new ArrayList<>();

    @FXML
    public Text titleLabel;

    @FXML
    private CheckBox selectAllCheckBox,
            showDiffRecordCountOnlyCheckBox,
            showOnlySchemaDiffersCheckBox,
            showSelectedOnlyCheckBox;

    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    public ToggleButton filterToggleButton;
    @FXML
    public HBox filtersHBox;
    @FXML
    public Accordion tablesAccordion;
    @FXML
    public Button nextStepBtn,
            previousStepBtn;
    @FXML
    public Text cancelBtn;


    @FXML
    public void onFilterButtonClicked(MouseEvent mouseEvent) {
        toggleFilters(filterToggleButton.isSelected());
    }

    @Override
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    @Override
    public void init(ConfigRegistry configRegistry, ComparisonStepsNavigator navigator) {
        this.comparison.setConfigRegistry(configRegistry);
        this.navigator = navigator;
        this.currentStage = navigator.getStage();

        computeComparedTables();
        prepareAccordionInfo();
        setupFilterControls();
    }


    private void computeComparedTables() {
        try {

            comparedTableList = ComparedTableService.getComparedTablesFromSources();

        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao carregar tabelas",
                    e.getMessage());
        }
    }

    /// USER-ACTIONS

    public void onCompareSchemasButtonClicked(ActionEvent actionEvent) {
//        Stage previouslyOpenedStage = compareSchemaOpenedStages.stream()
//                .filter(openedStage -> perSourceTable.equals(openedStage.getUserData()))
//                .findFirst()
//                .orElse(null);
//
//        if (previouslyOpenedStage != null) {
//            DialogUtils.showInCenter(currentStage, previouslyOpenedStage);
//            previouslyOpenedStage.toFront();
//            previouslyOpenedStage.requestFocus();
//
//            return;
//        }
//
//
//        Stage schemaComparisonStage = DialogUtils.showSchemaComparisonScreen(currentStage, perSourceTable);
//        if (schemaComparisonStage == null) return;
//
//        schemaComparisonStage.setUserData(perSourceTable);
//
//        compareSchemaOpenedStages.add(schemaComparisonStage);
    }





    private void prepareAccordionInfo() {
        if (comparedTableList.isEmpty()) {
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
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals("coluna")) {
                setupFilterByColumnName();
            }
            applyFilter();
        });



        showOnlySchemaDiffersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showDiffRecordCountOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showSelectedOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        applyFilter();
    }

    private void setupFilterByColumnName() {
        List<SourceTable> allSourceTables = comparedTableList.stream()
                .flatMap(comparedTable -> comparedTable.getSourceTables().stream())
                .filter(sourceTable -> sourceTable.getSourceTableColumns().isEmpty())
                .toList();

        if (allSourceTables.isEmpty()) return;

        try {

            SourceService.getColumnsOfTables(allSourceTables);

        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Algo deu errado ao carregar as colunas",
                    e.getMessage());
        }
    }

    private void applyFilter() {
        String filterText = searchTextField.getText().toLowerCase().trim();
        String filterType = filterTypeComboBox.getValue();

        filteredTablePanes.setPredicate(pane -> {
            String tableName = pane.getText().toLowerCase();

            ComparedTable comparedTable = comparedTableList.stream()
                    .filter(ct -> ct.getTableName().equalsIgnoreCase(tableName))
                    .findFirst().orElse(null);
            if (comparedTable == null) return true;

            // Filter by text
            if (!filterText.isEmpty()) {
                if ("tabela".equalsIgnoreCase(filterType)) {
                    if (!tableName.contains(filterText)) return false;

                } else if ("coluna".equalsIgnoreCase(filterType)) {

                    List<SourceTable> sourceTables = comparedTable.getSourceTables();
                    if (sourceTables.isEmpty()) return false;

                    boolean columnMatch = sourceTables.stream()
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
                Map<String, SourceTable> perSource = comparedTable.getPerSourceTable();
                if (perSource == null) return false;

                Set<Integer> rowCounts = new HashSet<>();
                for (SourceTable st : perSource.values()) {
                    rowCounts.add(st.getRecordCount());
                }
                if (rowCounts.size() <= 1) return false;
            }

            // show only different schema
            if (showOnlySchemaDiffersCheckBox.isSelected()) {
                int totalSources = comparison.getSources().size();
                Map<String, SourceTable> perSource = comparedTable.getPerSourceTable();
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
        tablesAccordion.getPanes().add(new TitledPane("Erro ao carregar tabelas", new Label("Erro ao carregar tabelas")));
    }



    private List<TitledPane> constructTitledPanes() {
        List<TitledPane> titledPaneList = new ArrayList<>();

        for (ComparedTable comparedTable : comparedTableList) {
            String tableName = comparedTable.getTableName();


            TitledPane tablePane = new TitledPane();
            tablePane.setText(tableName);



            tablePane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && tablePane.getUserData() == null) {

                    TableView<SourceTableViewModel> tableView = constructTitledPaneContent(comparedTable);

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


            boolean tableExistsInAllSources = comparedTable.getSourceTables().size() == 2;
            boolean tableHasRecordsInAllSources = comparedTable.getSourceTables().stream()
                    .noneMatch(table -> table.getRecordCount() == 0);


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
                Tooltip.install(tablePane, tooltip);
            } else if (!tableHasRecordsInAllSources) {
                Tooltip tooltip = new Tooltip("Esta tabela não possui registro em todas as fontes");
                Tooltip.install(tablePane, tooltip);
            }

            graphicBox.getChildren().addAll(spacer, selectCheckBox);
            tablePane.setGraphic(graphicBox);

            titledPaneList.add(tablePane);
        }
        return titledPaneList;
    }


    private TableView<SourceTableViewModel> constructTitledPaneContent(ComparedTable comparedTable) {
        final double TABLE_ROW_HEIGHT = 28.0;
        final double TABLE_HEADER_HEIGHT = 30.0;

        TableView<SourceTableViewModel> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("Nenhuma informação encontrada."));

        TableColumn<SourceTableViewModel, String> sourceColumn = new TableColumn<>("Fonte");
        sourceColumn.setCellValueFactory(cellData -> cellData.getValue().sourceIdProperty());

        TableColumn<SourceTableViewModel, Number> rowCountColumn = new TableColumn<>("Nº de Registros");
        rowCountColumn.setCellValueFactory(cellData -> cellData.getValue().columnCountProperty());


        tableView.getColumns().addAll(sourceColumn, rowCountColumn);

        ObservableList<SourceTableViewModel> tableStats = FXCollections.observableArrayList();


        for (SourceTable sourceTable : comparedTable.getSourceTables()) {

            tableStats.add(new SourceTableViewModel(sourceTable));
        }

        tableView.setItems(tableStats);

        double calculatedPrefHeight = (tableStats.size() * TABLE_ROW_HEIGHT) + TABLE_HEADER_HEIGHT;
        tableView.setPrefHeight(Math.max(calculatedPrefHeight, TABLE_HEADER_HEIGHT));

        return tableView;
    }

    private HBox buildButtonBox(String tableName) {
        Button compareSchemas = new Button("Comparar Schemas");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(10, spacer, compareSchemas);
        buttonBox.setAlignment(Pos.CENTER_RIGHT); // Right-aligned
        buttonBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding

        compareSchemas.setOnAction(this::onCompareSchemasButtonClicked);

        compareSchemas.setUserData(tableName);

        compareSchemas.getStyleClass().add("btn-action");

        return buttonBox;
    }





//    private Map<String, Map<String, SourceTable>> getGroupedTables () {
//        // tableName, sourceId, sourceTable
//        Map<String, Map<String, SourceTable>> groupedTables = new HashMap<>();
//
//        if (comparison != null && comparison.getSources() != null) {
//            for (ComparedSource comparedSource : comparison.getComparedSources()) {
//                if (comparedSource.getSource() != null && comparedSource.getSource().getSourceTables() != null) {
//                    for (SourceTable sourceTable : comparedSource.getSource().getSourceTables()) {
//                        String tableName = sourceTable.getTableName();
//
//                        groupedTables
//                                .computeIfAbsent(tableName, k -> new HashMap<>())
//                                .put(comparedSource.getSourceId(), sourceTable);
//                    }
//                }
//            }
//        }
//        return groupedTables;
//    }

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

    public void nextStep() {
        if (selectedTableNames.isEmpty()) {
            DialogUtils.showWarning(currentStage,
                    "Nenhuma tabela selecionada.",
                    "Selecione ao menos uma tabela para prosseguir com a comparação.");
            return;
        }

        navigator.goTo(FxmlFiles.LOADING_SCREEN, ctrl -> {
            ctrl.setTitle("Processando tabelas, aguarde...");
        });

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ComparisonService.processTables(selectedTableNames);
                return null;
            }
        };

        navigator.runTask(task, () -> {
            navigator.goTo(FxmlFiles.COLUMN_SETTINGS_SCREEN, ctrl -> {
                ctrl.setTitle("selecione as tabelas a serem comparadas");
                ctrl.init(comparison.getConfigRegistry(), navigator);
            });
        });
    }

    public void previousStep(MouseEvent mouseEvent) {
        navigator.goTo(FxmlFiles.LOADING_SCREEN, ctrl -> {
            ctrl.setTitle("Processando fontes, aguarde...");
        });

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ComparisonService.processTables(selectedTableNames);
                return null;
            }
        };

        navigator.runTask(task, () -> {
            navigator.goTo(FxmlFiles.ATTACH_SOURCES_SCREEN, ctrl -> {
                ctrl.setTitle("selecione os bancos para comparação");
                ctrl.init(comparison.getConfigRegistry(), navigator);
            });
        });
    }

    public void cancelComparison(MouseEvent mouseEvent) {
        boolean confirmCancel = DialogUtils.askConfirmation(currentStage,
                "Cancelar comparação",
                "Deseja realmente cancelar essa comparação? Nenhuma informação será salva");;
        if (!confirmCancel) {
            return;
        }

        navigator.goTo(FxmlFiles.LOADING_SCREEN, ctrl -> {
            ctrl.setTitle("cancelando comparação, aguarde...");
        });

        navigator.cancelComparison();
    }
}