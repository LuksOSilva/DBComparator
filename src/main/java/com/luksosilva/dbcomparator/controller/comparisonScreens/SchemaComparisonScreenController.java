package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.viewmodel.live.source.SourceTableColumnViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class SchemaComparisonScreenController {

    static class ComparisonRow {
        private final SourceTableColumnViewModel left;
        private final SourceTableColumnViewModel right;

        public ComparisonRow(SourceTableColumnViewModel left, SourceTableColumnViewModel right) {
            this.left = left;
            this.right = right;
        }
        public SourceTableColumnViewModel getLeft() { return left; }
        public SourceTableColumnViewModel getRight() { return right; }
    }

    private static final int SOURCE_LIMIT = 2;



    private Stage currentStage;

    @FXML
    public Label titleLabel;

    @FXML
    public TextField searchTextField;
    @FXML
    public Button applyFilterButton;

    @FXML
    public HBox tablesHBox;

    private TableView<ComparisonRow> tableView = new TableView<>();
    private ObservableList<ComparisonRow> masterRows = FXCollections.observableArrayList();

    public void setStage(Stage stage) {
        this.currentStage = stage;

        stage.setOnCloseRequest(event -> {
            event.consume();
            onCloseButtonClicked();
        });
    }

    public void init(Map<String, SourceTable> perSourceTable) {
        titleLabel.setText(
                perSourceTable.values().stream()
                .map(SourceTable::getTableName).findFirst().orElse("")
        );

        constructComparisonTable(perSourceTable);

        applyFilterButton.setOnAction(event -> applyFilter());
    }

    private void applyFilter() {
        String filterText = searchTextField.getText().trim().toLowerCase();
        if (filterText.isEmpty()) {
            tableView.setItems(masterRows);
            sortTableView();
            return;
        }

        ObservableList<ComparisonRow> filteredRows = FXCollections.observableArrayList();

        for (ComparisonRow row : masterRows) {
            boolean matches = false;

            if (row.getLeft() != null) {
                matches |= row.getLeft().getColumnName().toLowerCase().contains(filterText);
                matches |= row.getLeft().getType().toLowerCase().contains(filterText);
            }
            if (row.getRight() != null) {
                matches |= row.getRight().getColumnName().toLowerCase().contains(filterText);
                matches |= row.getRight().getType().toLowerCase().contains(filterText);
            }

            if (matches) filteredRows.add(row);
        }

        tableView.setItems(filteredRows);

        sortTableView();
    }


    private void constructComparisonTable(Map<String, SourceTable> perSourceTable) {
        if (perSourceTable.size() != SOURCE_LIMIT) return;


        var iter = perSourceTable.entrySet().iterator();
        var entryA = iter.next();
        var entryB = iter.next();

        String sourceAName = entryA.getKey();
        String sourceBName = entryB.getKey();
        SourceTable sourceA = entryA.getValue();
        SourceTable sourceB = entryB.getValue();

        TableView<ComparisonRow> tableView = new TableView<>();

        // ---- Build Source A columns ----
        TableColumn<ComparisonRow, String> seqA = new TableColumn<>("#");
        seqA.setCellValueFactory(cd ->
                cd.getValue().getLeft() != null ? cd.getValue().getLeft().sequenceProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> nameA = new TableColumn<>("Coluna");
        nameA.setCellValueFactory(cd ->
                cd.getValue().getLeft() != null ? cd.getValue().getLeft().columnNameProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> typeA = new TableColumn<>("Tipo");
        typeA.setCellValueFactory(cd ->
                cd.getValue().getLeft() != null ? cd.getValue().getLeft().typeProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> pkA = new TableColumn<>("PK");
        pkA.setCellValueFactory(cd ->
                cd.getValue().getLeft() != null ? cd.getValue().getLeft().isPkProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> nullA = new TableColumn<>("Not Null");
        nullA.setCellValueFactory(cd ->
                cd.getValue().getLeft() != null ? cd.getValue().getLeft().notNullProperty() : new SimpleStringProperty("")
        );


        // ---- Build Source B columns ----
        TableColumn<ComparisonRow, String> seqB = new TableColumn<>("#");
        seqB.setCellValueFactory(cd ->
                cd.getValue().getRight() != null ? cd.getValue().getRight().sequenceProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> nameB = new TableColumn<>("Coluna");
        nameB.setCellValueFactory(cd ->
                cd.getValue().getRight() != null ? cd.getValue().getRight().columnNameProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> typeB = new TableColumn<>("Tipo");
        typeB.setCellValueFactory(cd ->
                cd.getValue().getRight() != null ? cd.getValue().getRight().typeProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> pkB = new TableColumn<>("PK");
        pkB.setCellValueFactory(cd ->
                cd.getValue().getRight() != null ? cd.getValue().getRight().isPkProperty() : new SimpleStringProperty("")
        );
        TableColumn<ComparisonRow, String> nullB = new TableColumn<>("Not Null");
        nullB.setCellValueFactory(cd ->
                cd.getValue().getRight() != null ? cd.getValue().getRight().notNullProperty() : new SimpleStringProperty("")
        );

        TableColumn<ComparisonRow, String> groupA = new TableColumn<>(sourceAName);
        groupA.getColumns().addAll(seqA, nameA, typeA, pkA, nullA);

        TableColumn<ComparisonRow, String> divider = new TableColumn<>("");
        divider.setCellValueFactory(cd -> new SimpleStringProperty(""));
        divider.setStyle("-fx-background-color: gray; -fx-pref-width: 2;");

        TableColumn<ComparisonRow, String> groupB = new TableColumn<>(sourceBName);
        groupB.getColumns().addAll(seqB, nameB, typeB, pkB, nullB);

        for (TableColumn<ComparisonRow, ?> tableColumn : groupA.getColumns()) {
            addStyle((TableColumn<ComparisonRow, String>) tableColumn);
        }
        for (TableColumn<ComparisonRow, ?> tableColumn : groupB.getColumns()) {
            addStyle((TableColumn<ComparisonRow, String>) tableColumn);
        }

        // Add everything to the table
        tableView.getColumns().addAll(groupA, divider, groupB);

        // ---- Build rows ----
        ObservableList<ComparisonRow> rows = FXCollections.observableArrayList();

        List<SourceTableColumnViewModel> listA = sourceA.getSourceTableColumns().stream().map(SourceTableColumnViewModel::new).toList();
        List<SourceTableColumnViewModel> listB = sourceB.getSourceTableColumns().stream().map(SourceTableColumnViewModel::new).toList();

        // Map column names to their SourceTableColumnViewModel for both sources
        Map<String, SourceTableColumnViewModel> mapA = listA.stream()
                .collect(Collectors.toMap(SourceTableColumnViewModel::getColumnName, c -> c));

        Map<String, SourceTableColumnViewModel> mapB = listB.stream()
                .collect(Collectors.toMap(SourceTableColumnViewModel::getColumnName, c -> c));

        // Get all column names that exist in either table
        Set<String> allColumnNames = new LinkedHashSet<>();
        allColumnNames.addAll(mapA.keySet());
        allColumnNames.addAll(mapB.keySet());

        // Create rows aligned by column name
        for (String columnName : allColumnNames) {
            SourceTableColumnViewModel left = mapA.get(columnName);
            SourceTableColumnViewModel right = mapB.get(columnName);
            rows.add(new ComparisonRow(left, right));
        }

        tableView.setItems(rows);

        masterRows.setAll(rows);
        this.tableView = tableView;

        // Styling and sizing
        HBox.setHgrow(tableView, Priority.ALWAYS);

        // Add to UI
        tablesHBox.getChildren().clear();
        tablesHBox.getChildren().add(tableView);

        sortTableView();
    }

    private void sortTableView() {
        tableView.getItems().sort((row1, row2) -> {
            int seq1Left = row1.getLeft() != null ? Integer.parseInt(row1.getLeft().getSequence()) : Integer.parseInt(row1.getRight().getSequence());
            int seq1Right = row1.getRight() != null ? Integer.parseInt(row1.getRight().getSequence()) : Integer.parseInt(row1.getLeft().getSequence());
            int seq2Left = row2.getLeft() != null ? Integer.parseInt(row2.getLeft().getSequence()) : Integer.parseInt(row2.getRight().getSequence());
            int seq2Right = row2.getRight() != null ? Integer.parseInt(row2.getRight().getSequence()) : Integer.parseInt(row2.getLeft().getSequence());

            int sum1 = seq1Left + seq1Right;
            int sum2 = seq2Left + seq2Right;

            return Integer.compare(sum1, sum2);
        });
    }


    private void addStyle(TableColumn<ComparisonRow, String> tableColumn) {
        tableColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Clear previous styles
                getStyleClass().removeAll("cell-different", "row-missing");

                setText(null);

                if (empty) return;

                setText(item); // default text

                ComparisonRow row = getTableView().getItems().get(getIndex());
                boolean leftMissing = row.getLeft() == null;
                boolean rightMissing = row.getRight() == null;

                // If row missing in any source
                if (leftMissing || rightMissing) {
                    getStyleClass().add("row-missing");
                    return;
                }

                // If any values differ
                SourceTableColumnViewModel left = row.getLeft();
                SourceTableColumnViewModel right = row.getRight();
                if (!left.equals(right)) {
                    getStyleClass().add("cell-different");
                }
            }
        });
    }


    @FXML
    public void onCloseButtonClicked() {
        if (currentStage != null) {
            currentStage.hide();
        }
    }
}
