package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
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

public class SelectTablesScreenController {



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
        public String getSourceName() {
            return sourceName.get();
        }

        public SimpleStringProperty sourceNameProperty() {
            return sourceName;
        }

        public int getRowCount() {
            return rowCount.get();
        }

        public SimpleIntegerProperty rowCountProperty() {
            return rowCount;
        }

        public int getColumnCount() {
            return columnCount.get();
        }

        public SimpleIntegerProperty columnCountProperty() {
            return columnCount;
        }

    }

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
    public CheckBox showInAllSourcesOnlyCheckBox;
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

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    @FXML
    public void onFilterButtonClicked(MouseEvent mouseEvent) {
        toggleFilters(filterToggleButton.isSelected());
    }




    public void init() {
        groupedTables = getGroupedTables();
        prepareAccordionInfo();
        setupFilterControls();
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
        showInAllSourcesOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
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
                if (rowCounts.size() <= 1) return false;  // Só exibe se tiver diferenças
            }

            // show only available in all sources filter
            if (showInAllSourcesOnlyCheckBox.isSelected()) {
                int totalSources = comparison.getComparedSources().size();
                Map<ComparedSource, SourceTable> perSource = groupedTables.get(pane.getText());
                if (perSource == null || perSource.size() < totalSources) return false;
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
                    TableView<TableSourceStats> tableView = buildTableMetadata(tablePane.getText());
                    VBox contentContainer = new VBox(tableView);
                    tablePane.setContent(contentContainer);
                    tablePane.setUserData(true);
                }
            });

            HBox graphicBox = new HBox();
            graphicBox.setAlignment(Pos.CENTER_RIGHT);
            graphicBox.setMaxWidth(Double.MAX_VALUE);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            CheckBox selectCheckBox = new CheckBox();
            selectCheckBox.setSelected(selectedTableNames.contains(tableName));
            selectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) selectedTableNames.add(tableName);
                else selectedTableNames.remove(tableName);
                updateSelectAllCheckBoxState();
            });

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
    }

    public void previousStep(MouseEvent mouseEvent) {
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