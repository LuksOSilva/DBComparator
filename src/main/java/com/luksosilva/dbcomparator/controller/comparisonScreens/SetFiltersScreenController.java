package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.viewmodel.comparison.ColumnFilterViewModel;
import com.luksosilva.dbcomparator.viewmodel.comparison.ComparedTableColumnViewModel;
import com.luksosilva.dbcomparator.viewmodel.comparison.ComparedTableViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

public class SetFiltersScreenController {

    private Scene previousScene;
    private Stage currentStage;
    private Scene nextScene;
    public void setPreviousScene(Scene previousScene) { this.previousScene = previousScene; }
    public void setCurrentStage(Stage currentStage) { this.currentStage = currentStage; }
    public void setNextScene(Scene nextScene) { this.nextScene = nextScene; }

    /// FXML Objects
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public Accordion tablesAccordion;
    @FXML
    public ComboBox<String> filterTypeComboBox;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button addFilterBtn;

    @FXML
    public Button nextStepBtn;
    @FXML
    public Button previousStepBtn;
    @FXML
    public Text cancelBtn;


    private Comparison comparison;
    private final ObservableList<TitledPane> allFiltersPanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredFilterPanes;

    private List<ComparedTableViewModel> comparedTableViewModels = new ArrayList<>();


    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }



    public void init() {
        setupViewModels();
        constructAccordion();
    }

    public boolean needToProcess() {
        return true;
    }

    /// USER-CALLED METHODS

    public void OnAddFilterButtonClicked(ActionEvent event) {
        Map<ComparedTableColumn, List<ColumnFilter>> perComparedTableColumnFilter = DialogUtils.showAddFilterDialog(currentStage, comparison.getComparedTables());

        if (perComparedTableColumnFilter == null || perComparedTableColumnFilter.isEmpty()) return;

        addFilter(perComparedTableColumnFilter);
    }

    /// HELPER METHODS

    private void addFilter(Map<ComparedTableColumn, List<ColumnFilter>> perComparedTableColumnFilter) {

        perComparedTableColumnFilter.forEach((comparedTableColumn, filter) -> {

            // 1. Apply the filter to the model
            comparedTableColumn.addColumnFilter(filter);

            // 2. Find and update the corresponding view model
            comparedTableViewModels.stream()
                    .flatMap(vm -> vm.getComparedTableColumnViewModels().stream())
                    .filter(columnVM -> columnVM.getComparedTableColumn().equals(comparedTableColumn))
                    .findFirst()
                    .ifPresent(ComparedTableColumnViewModel::setProperties);
        });

        constructTitledPanes(perComparedTableColumnFilter);

    }

    private void applyFilter() {

    }

    private ComparedTable getComparedTableOfComparedColumn(ComparedTableColumn comparedTableColumn) {
        return comparison.getComparedTables().stream()
                .filter(ct -> ct.getComparedTableColumns().stream()
                        .anyMatch(ctc -> ctc.equals(comparedTableColumn)))
                .findFirst()
                .orElse(null);
    }

    private ComparedTable getComparedTableFromTableName(String tableName) {
        return comparison.getComparedTables().stream()
                .filter(ct -> ct.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
    }

    private  void setupViewModels() {
        for (ComparedTable comparedTable : comparison.getComparedTables()) {

            comparedTableViewModels.add(new ComparedTableViewModel(comparedTable));

        }
    }

    /// CONSTRUCTOR METHODS

    private void refreshAccordion() {
        tablesAccordion.getPanes().setAll(filteredFilterPanes);
    }

    private void constructAccordion() {
        tablesAccordion.getPanes().clear(); // Clears Accordion
        allFiltersPanes.clear();             // Clears master list

        // Initialize FilteredList based on allTablePanes
        filteredFilterPanes = new FilteredList<>(allFiltersPanes, pane -> true);

        // Set the Accordion's panes to the filtered list.
        refreshAccordion();
    }

    private void constructTitledPanes(Map<ComparedTableColumn, List<ColumnFilter>> perComparedTableColumnFilter) {
        List<TitledPane> titledPaneList = new ArrayList<>();

        perComparedTableColumnFilter.forEach((comparedTableColumn, filter) -> {

            ComparedTable comparedTable = getComparedTableOfComparedColumn(comparedTableColumn);
            if (comparedTable == null) return;

            Optional<TitledPane> existingPaneOpt = allFiltersPanes.stream()
                    .filter(tp -> tp.getText().equals(comparedTable.getTableName()))
                    .findFirst();

            if (existingPaneOpt.isPresent()) {
                TitledPane existingPane = existingPaneOpt.get();
                if (existingPane.getUserData() != null) {
                    constructTitledPaneContent(existingPane); //refresh
                }
            } else {
                TitledPane titledPane = new TitledPane();
                titledPane.setText(comparedTable.getTableName());

                titledPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                    if (isNowExpanded && titledPane.getUserData() == null) {
                        constructTitledPaneContent(titledPane);
                    }
                });

                titledPaneList.add(titledPane);
            }

        });

        allFiltersPanes.addAll(titledPaneList);
        refreshAccordion();
    }

    private void constructTitledPaneContent(TitledPane titledPane) {
        final double TABLE_ROW_HEIGHT = 28.0;
        final double TABLE_HEADER_HEIGHT = 30.0;

        String tableName = titledPane.getText();

        ComparedTable comparedTable = getComparedTableFromTableName(tableName);
        if (comparedTable == null) return;

        TableView<ColumnFilterViewModel> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ColumnFilterViewModel, String> columnNameCol = new TableColumn<>("Coluna");
        columnNameCol.setCellValueFactory(data -> data.getValue().getColumnNameProperty());

        TableColumn<ColumnFilterViewModel, String> filterTypeCol = new TableColumn<>("Operação");
        filterTypeCol.setCellValueFactory(data -> data.getValue().getFilterTypeProperty());

        TableColumn<ColumnFilterViewModel, String> filterCol = new TableColumn<>("Filtro");
        filterCol.setCellValueFactory(data -> data.getValue().getFilterValueProperty());

        tableView.getColumns().addAll(columnNameCol, filterTypeCol, filterCol);

        // Generate one row per filter
        ObservableList<ColumnFilterViewModel> tableItems = FXCollections.observableArrayList();

        for (ComparedTableColumnViewModel columnViewModel : comparedTableViewModels.stream()
                .filter(vm -> vm.getTableName().equals(tableName))
                .flatMap(vm -> vm.getComparedTableColumnViewModels().stream())
                .toList()) {

            List<ColumnFilter> filters = columnViewModel.getComparedTableColumn().getColumnFilter();
            for (ColumnFilter filter : filters) {
                tableItems.add(new ColumnFilterViewModel(columnViewModel.getComparedTableColumn(), filter));
            }
        }

        tableView.setItems(tableItems);

        double calculatedPrefHeight = (tableItems.size() * TABLE_ROW_HEIGHT) + TABLE_HEADER_HEIGHT;
        tableView.setPrefHeight(Math.max(calculatedPrefHeight, TABLE_HEADER_HEIGHT));

        VBox container = new VBox(tableView);
        container.setPadding(new Insets(10));
        container.setFillWidth(true);

        titledPane.setContent(container);
        titledPane.setUserData(true);
    }

    /// NAVIGATION METHODS

    public void nextStep(MouseEvent mouseEvent) {
    }

    public void previousStep(MouseEvent mouseEvent) {

        ColumnSettingsScreenController columnSettingsScreenController = (ColumnSettingsScreenController) previousScene.getUserData();
        columnSettingsScreenController.setNextScene(currentStage.getScene());

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
