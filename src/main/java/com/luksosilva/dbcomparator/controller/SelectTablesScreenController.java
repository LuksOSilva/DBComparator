package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class SelectTablesScreenController {

    private Comparison comparison;
    private Map<String, Map<ComparedSource, SourceTable>> groupedTables;
    private List<String> selectedTableNames = new ArrayList<>();

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

    public void init() {
        groupedTables = getGroupedTables();
        populateAccordion();
    }

    private void populateAccordion() {
        if (comparison == null || groupedTables.isEmpty()) {
            tablesAccordion.getPanes().clear();
            tablesAccordion.getPanes().add(new TitledPane("No Tables Found", new Label("No table metadata available for comparison.")));
            return;
        }
        tablesAccordion.getPanes().clear();

        tablesAccordion.getPanes().addAll(buildHeaders(groupedTables));
    }

    /// helper methods

    private List<TitledPane> buildHeaders(Map<String, Map<ComparedSource, SourceTable>> groupedTablesForListing) {

        List<TitledPane> titledPaneList = new ArrayList<>();

        // Get sorted table names.
        List<String> tableNames = groupedTables.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        for (String tableName : tableNames) {

            TitledPane tablePane = new TitledPane();
            tablePane.setText(tableName);

            VBox dummyContent = new VBox(new Label("Click to load data for " + tableName + "..."));
            dummyContent.setAlignment(Pos.CENTER);
            dummyContent.setPadding(new Insets(10));
            tablePane.setContent(dummyContent);

            tablePane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded) {
                    if (tablePane.getContent() instanceof VBox) {
                        TableView<TableSourceStats> tableView = buildTableMetadata(tablePane.getText());

                        VBox contentContainer = new VBox(tableView);

                        tablePane.setContent(contentContainer);
                    }
                }
            });



            HBox graphicBox = new HBox();

            CheckBox selectCheckBox = new CheckBox();
            selectCheckBox.setSelected(selectedTableNames.contains(tableName));
            selectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) selectedTableNames.add(tableName);
                else selectedTableNames.remove(tableName);
            });

            graphicBox.getChildren().add(selectCheckBox);
            tablePane.setGraphic(graphicBox);


            titledPaneList.add(tablePane);

        }
        return titledPaneList;
    }

    private TableView<TableSourceStats> buildTableMetadata(String tableName) {

        final double TABLE_ROW_HEIGHT = 27.0; // Typical default row height
        final double TABLE_HEADER_HEIGHT = 30.0; // Typical header height

        Map<ComparedSource, SourceTable> perSourceTable = groupedTables.get(tableName);

        TableView<TableSourceStats> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No data for this table from attached sources."));

        // Create Columns for the TableView
        TableColumn<TableSourceStats, String> sourceColumn = new TableColumn<>("Fonte");
        sourceColumn.setCellValueFactory(cellData -> cellData.getValue().sourceNameProperty());

        TableColumn<TableSourceStats, Integer> rowCountColumn = new TableColumn<>("Nº de Registros");
        rowCountColumn.setCellValueFactory(cellData -> cellData.getValue().rowCountProperty().asObject());

        TableColumn<TableSourceStats, Integer> columnCountColumn = new TableColumn<>("Nº de Colunas");
        columnCountColumn.setCellValueFactory(cellData -> cellData.getValue().columnCountProperty().asObject());

        tableView.getColumns().addAll(sourceColumn, rowCountColumn, columnCountColumn);

        ObservableList<TableSourceStats> tableStats = FXCollections.observableArrayList();

        // Iterate through the inner map to get data for each source for this table.
        for (Map.Entry<ComparedSource, SourceTable> entry : perSourceTable.entrySet()) {
            ComparedSource comparedSource = entry.getKey();
            SourceTable sourceTable = entry.getValue();

            String sourceName = comparedSource.getSourceId();
            int rowCount = sourceTable.getRecordCount();
            int columnCount = sourceTable.getSourceTableColumns().size();

            tableStats.add(new TableSourceStats(sourceName, rowCount, columnCount));
        }

        tableView.setItems(tableStats);

        double calculatedPrefHeight = (tableStats.size() * TABLE_ROW_HEIGHT) + TABLE_HEADER_HEIGHT;
        tableView.setPrefHeight(Math.max(calculatedPrefHeight, TABLE_HEADER_HEIGHT));

        //tablePane.setContent(tableView);
        return tableView;
    }

    private Map<String, Map<ComparedSource, SourceTable>> getGroupedTables () {

        Map<String, Map<ComparedSource, SourceTable>> groupedTables = new HashMap<>();

        for (ComparedSource comparedSource : comparison.getComparedSources()) {

            for (SourceTable sourceTable : comparedSource.getSource().getSourceTables()) {
                String tableName = sourceTable.getTableName();

                groupedTables
                        .computeIfAbsent(tableName, k -> new HashMap<>())
                        .put(comparedSource, sourceTable);


            }

        }
        return groupedTables;
    }

    /// steps

    public void nextStep(MouseEvent mouseEvent) {
    }

    public void previousStep(MouseEvent mouseEvent) {
    }

    public void cancelComparison(MouseEvent mouseEvent) {

        boolean confirmCancel = DialogUtils.askConfirmation("Cancelar comparação",
                "Deseja realmente cancelar essa comparação? Nenhuma informação será salva");;
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
