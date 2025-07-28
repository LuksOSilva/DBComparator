package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.comparison.customization.Filter;
import com.luksosilva.dbcomparator.model.comparison.customization.TableFilter;
import com.luksosilva.dbcomparator.service.FilterService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.viewmodel.comparison.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    private FilteredList<TitledPane> filteredFilterPanes = new FilteredList<>(allFiltersPanes, s -> true);

    private final List<ComparedTableViewModel> comparedTableViewModels = new ArrayList<>();


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
        List<Filter> addedFilters = DialogUtils.showAddFilterDialog(currentStage, comparison.getComparedTables());

        if (addedFilters == null || addedFilters.isEmpty()) return;

        addFilter(addedFilters);
    }

    public void onEditDefaultFilterButtonClicked(ComparedTable comparedTable,
                                          ComparedTableColumn comparedTableColumn,
                                          Filter filter) {

        Map<Filter, Filter> perNewFilterOldFilter =
                DialogUtils.showEditDefaultFilterDialog(currentStage,
                        comparison.getComparedTables(), comparedTable,
                        comparedTableColumn, (ColumnFilter) filter);

        if (perNewFilterOldFilter == null || perNewFilterOldFilter.isEmpty()) return;

        editFilter(perNewFilterOldFilter);
    }

    public void onEditAdvancedFilterButtonClicked(ComparedTable comparedTable) {
        Map<Filter, Filter> perNewFilterOldFilter =
                DialogUtils.showEditAdvancedFilterDialog(currentStage,comparedTable);

        if (perNewFilterOldFilter == null || perNewFilterOldFilter.isEmpty()) return;

        editFilter(perNewFilterOldFilter);
    }

    public void onDeleteFilterButtonClicked(Filter filter) {
        boolean confirmed = DialogUtils.askConfirmation("Apagar filtro?",
                "Deseja realmente apagar esse filtro? Essa ação não poderá ser desfeita.");

        if (!confirmed) return;

        deleteFilter(filter);
    }

    /// HELPER METHODS

    private void addFilter(List<Filter> addedFilters) {

        FilterService.applyFilters(addedFilters);

        addedFilters.forEach((filter) -> {
            if (filter instanceof TableFilter) {
                constructTitledPane(((TableFilter) filter).getComparedTable());
            } else if (filter instanceof ColumnFilter) {
                constructTitledPane(((ColumnFilter) filter).getComparedTableColumn().getComparedTable());
            }
        });
    }

    private void editFilter(Map<Filter, Filter> perNewFilterOldFilter) {

        FilterService.editFilter(perNewFilterOldFilter);

        perNewFilterOldFilter.forEach((newFilter, oldFilter) -> {
            if (newFilter instanceof TableFilter) {
                constructTitledPane(((TableFilter) newFilter).getComparedTable());
            } else if (newFilter instanceof ColumnFilter) {
                constructTitledPane(((ColumnFilter) newFilter).getComparedTableColumn().getComparedTable());
            }
        });
    }

    private void deleteFilter(Filter filter) {

        FilterService.deleteFilter(filter);

        if (filter instanceof TableFilter) {
            ComparedTable comparedTable = ((TableFilter) filter).getComparedTable();

            if (comparedTable.getFilter() == null) {
                destructTitledPane(comparedTable);
                return;
            }

            constructTitledPane(comparedTable);
        }
        else {
            ComparedTableColumn comparedTableColumn = ((ColumnFilter) filter).getComparedTableColumn();
            ComparedTable comparedTable = comparedTableColumn.getComparedTable();

            if (comparedTable.getComparedTableColumns().stream().allMatch(tableColumn -> tableColumn.getColumnFilters().isEmpty())) {
                destructTitledPane(comparedTableColumn.getComparedTable());
                return;
            }

            constructTitledPane(comparedTableColumn.getComparedTable());
        }
    }

    private void applyFilter() {

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


    private void destructTitledPane(ComparedTable comparedTable) {
        Optional<TitledPane> existingPaneOpt = allFiltersPanes.stream()
                .filter(tp -> tp.getText().equals(comparedTable.getTableName()))
                .findFirst();
        if (existingPaneOpt.isEmpty()) return;

        TitledPane existingPane = existingPaneOpt.get();
        allFiltersPanes.remove(existingPane);

        refreshAccordion();
    }


    private void constructTitledPane(ComparedTable comparedTable) {

        Optional<TitledPane> existingPaneOpt = allFiltersPanes.stream()
                .filter(tp -> tp.getText().equals(comparedTable.getTableName()))
                .findFirst();

        if (existingPaneOpt.isPresent()) {
            TitledPane existingPane = existingPaneOpt.get();
            existingPane.setExpanded(false);
            existingPane.setUserData(null);
        }
        else {
            TitledPane titledPane = new TitledPane();
            titledPane.setText(comparedTable.getTableName());

            titledPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && titledPane.getUserData() == null) {
                    constructTitledPaneContent(titledPane, comparedTable);
                }
            });

            allFiltersPanes.add(titledPane);
        }

        refreshAccordion();
    }


    private void constructTitledPaneContent(TitledPane titledPane, ComparedTable comparedTable) {

            TableView<FilterViewModel> tableView = createFilterTableView(comparedTable);
            ObservableList<FilterViewModel> tableItems = getFilterItemsForTable(comparedTable);

            tableView.setItems(tableItems);
            adjustTableHeight(tableView, tableItems.size());

            VBox container = new VBox(tableView);
            container.setPadding(new Insets(10));
            container.setFillWidth(true);

            titledPane.setContent(container);
            titledPane.setUserData(true);

    }

    private TableView<FilterViewModel> createFilterTableView(ComparedTable comparedTable) {
        TableView<FilterViewModel> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        if (comparedTable.getFilter() == null) {
            tableView.getColumns().addAll(createColumnNameColumn(), createFilterTypeColumn());
        }

        tableView.getColumns().addAll(createFilterValueColumn(), createActionColumn(comparedTable));

        return tableView;
    }

    private TableColumn<FilterViewModel, String> createColumnNameColumn() {
        TableColumn<FilterViewModel, String> col = new TableColumn<>("Coluna");
        col.setCellValueFactory(data -> data.getValue().columnNameProperty()
                .orElse(new SimpleStringProperty("")));
        return col;
    }

    private TableColumn<FilterViewModel, String> createFilterTypeColumn() {
        TableColumn<FilterViewModel, String> col = new TableColumn<>("Operação");
        col.setCellValueFactory(data -> data.getValue().filterTypeDescriptionProperty()
                .orElse(new SimpleStringProperty("")));
        return col;
    }

    private TableColumn<FilterViewModel, String> createFilterValueColumn() {
        TableColumn<FilterViewModel, String> col = new TableColumn<>("Filtro");
        col.setCellValueFactory(data -> data.getValue().displayValueProperty());
        return col;
    }

    private TableColumn<FilterViewModel, Void> createActionColumn(ComparedTable comparedTable) {
        TableColumn<FilterViewModel, Void> actionsCol = new TableColumn<>();

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("edit");
            private final Button deleteButton = new Button("del");
            private final HBox hbox = new HBox(10, editButton, deleteButton);

            {
                hbox.setAlignment(Pos.CENTER);
                editButton.setOnAction(e -> handleEdit(comparedTable));
                deleteButton.setOnAction(e -> handleDelete(comparedTable));
            }

            private void handleEdit(ComparedTable table) {
                FilterViewModel item = getTableView().getItems().get(getIndex());

                if (table.getFilter() != null) {
                    onEditAdvancedFilterButtonClicked(table);
                    return;
                }
                onEditDefaultFilterButtonClicked(table,
                        ((ColumnFilterViewModel) item).getModel().getComparedTableColumn(),
                        (((ColumnFilterViewModel) item).getModel()));
            }

            private void handleDelete(ComparedTable table) {
                FilterViewModel item = getTableView().getItems().get(getIndex());

                onDeleteFilterButtonClicked(item.getModel());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        return actionsCol;
    }

    private ObservableList<FilterViewModel> getFilterItemsForTable(ComparedTable comparedTable) {
        ObservableList<FilterViewModel> items = FXCollections.observableArrayList();

        ComparedTableViewModel comparedTableViewModel = comparedTableViewModels.stream()
                .filter(vm -> vm.getModel().equals(comparedTable))
                .findFirst()
                .orElse(null);
        if (comparedTableViewModel == null) return items; //returns empty

        comparedTableViewModel.updateViewModel();

        if (comparedTable.getFilter() != null) {

            items.add(comparedTableViewModel.getTableFilterViewModel());
        } else {
            comparedTableViewModel.getComparedTableColumnViewModels().stream()
                    .flatMap(colVM -> colVM.getColumnFilterViewModels().stream())
                    .forEach(items::add);
        }

        return items;
    }

    private void adjustTableHeight(TableView<?> tableView, int itemCount) {
        final double TABLE_ROW_HEIGHT = 35.0;
        final double TABLE_HEADER_HEIGHT = 30.0;
        double prefHeight = (itemCount * TABLE_ROW_HEIGHT) + TABLE_HEADER_HEIGHT;
        tableView.setPrefHeight(Math.max(prefHeight, TABLE_HEADER_HEIGHT));
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
