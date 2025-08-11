package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.exception.ColumnSettingsException;
import com.luksosilva.dbcomparator.exception.FilterException;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.comparison.customization.Filter;
import com.luksosilva.dbcomparator.model.comparison.customization.TableFilter;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.service.FilterService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.viewmodel.comparison.compared.ComparedTableViewModel;
import com.luksosilva.dbcomparator.viewmodel.comparison.customization.FilterViewModel;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.sqlite.FileException;

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
        setupFilterControls();
        constructAccordion();
    }

    public boolean needToProcess() {
        return comparison.getComparedTables().stream()
                .anyMatch(comparedTable -> comparedTable.hasFilter()
                        && comparedTable.getFilterValidationResult().getType().equals(FilterValidationResultType.NOT_VALIDATED));
    }

    /// USER-CALLED METHODS

    public void OnAddFilterButtonClicked(ActionEvent event) {
        List<Filter> addedFilters = DialogUtils.showAddFilterDialog(currentStage, comparison.getComparedTables());

        if (addedFilters == null || addedFilters.isEmpty()) return;

        addFilter(addedFilters);
    }

    public void onEditFilterButtonClicked(Filter filter) {

        if (filter instanceof TableFilter tableFilter) {

            Map<Filter, Filter> perNewFilterOldFilter =
                    DialogUtils.showEditAdvancedFilterDialog(currentStage, tableFilter.getComparedTable());

            if (perNewFilterOldFilter == null || perNewFilterOldFilter.isEmpty()) return;

            editFilter(perNewFilterOldFilter);

        } else if (filter instanceof ColumnFilter columnFilter) {

            Map<Filter, Filter> perNewFilterOldFilter =
                    DialogUtils.showEditDefaultFilterDialog(currentStage,
                            comparison.getComparedTables(), columnFilter);

            if (perNewFilterOldFilter == null || perNewFilterOldFilter.isEmpty()) return;

            editFilter(perNewFilterOldFilter);

        }

    }

    public void onDeleteFilterButtonClicked(Filter filter) {
        boolean confirmed = DialogUtils.askConfirmation("Apagar filtro?",
                "Deseja realmente apagar esse filtro? Essa ação não poderá ser desfeita.");

        if (!confirmed) return;

        deleteFilter(filter);
    }

    public void onCopyToClipBoardButtonClicked(TableFilter tableFilter) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(tableFilter.getUserWrittenFilter());
        clipboard.setContent(content);
    }

    /// HELPER METHODS

    private void addFilter(List<Filter> addedFilters) {

        FilterService.applyFilters(addedFilters);

        addedFilters.forEach((filter) -> {
            if (filter instanceof TableFilter tableFilter) {
                constructTitledPane(tableFilter.getComparedTable());
            } else if (filter instanceof ColumnFilter columnFilter) {
                constructTitledPane(columnFilter.getComparedTableColumn().getComparedTable());
            }
        });
    }

    private void editFilter(Map<Filter, Filter> perNewFilterOldFilter) {

        FilterService.editFilter(perNewFilterOldFilter);

        perNewFilterOldFilter.forEach((newFilter, oldFilter) -> {
            if (newFilter instanceof TableFilter tableFilter) {
                constructTitledPane(tableFilter.getComparedTable());
            } else if (newFilter instanceof ColumnFilter columnFilter) {
                constructTitledPane(columnFilter.getComparedTableColumn().getComparedTable());
            }
        });
    }

    private void deleteFilter(Filter filter) {

        FilterService.deleteFilter(filter);

        if (filter instanceof TableFilter tableFilter) {
            ComparedTable comparedTable = tableFilter.getComparedTable();

            if (comparedTable.getFilter() == null) {
                destructTitledPane(comparedTable);
                return;
            }

            constructTitledPane(comparedTable);
        }
        else if (filter instanceof ColumnFilter columnFilter){
            ComparedTable comparedTable = columnFilter.getComparedTableColumn().getComparedTable();

            if (comparedTable.getComparedTableColumns().stream().allMatch(tableColumn -> tableColumn.getColumnFilters().isEmpty())) {
                destructTitledPane(comparedTable);
                return;
            }

            constructTitledPane(comparedTable);
        }
    }



    private  void setupViewModels() {
        for (ComparedTable comparedTable : comparison.getComparedTables()) {

            comparedTableViewModels.add(new ComparedTableViewModel(comparedTable));

        }
    }

    private void setupFilterControls() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList("tabela", "coluna"));
        filterTypeComboBox.setValue("tabela");


        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());


        applyFilter();
    }

    private void applyFilter() {
        String filterText = searchTextField.getText().toLowerCase().trim();
        String filterType = filterTypeComboBox.getValue();

        filteredFilterPanes.setPredicate(pane -> {
            String tableName = pane.getText();
            String tableNameLowerCase = tableName.toLowerCase();

            // Filter by text
            if (!filterText.isEmpty()) {
                if ("tabela".equalsIgnoreCase(filterType)) {
                    if (!tableNameLowerCase.contains(filterText)) return false;
                } else if ("coluna".equalsIgnoreCase(filterType)) {

                    ComparedTable comparedTable = getComparedTableFromTableName(tableName);
                    if (comparedTable == null) return false;

                    boolean columnMatch = comparedTable.getComparedTableColumns().stream()
                            .anyMatch(comparedTableColumn -> comparedTableColumn.getColumnName().contains(filterText));

                    if (!columnMatch) return false;
                }
            }

            return true;
        });

        // Atualiza os panes
        tablesAccordion.getPanes().setAll(filteredFilterPanes);

        // Faz um fade-in suave no accordion
        fadeInAccordion();
    }

    private void fadeInAccordion() {
        tablesAccordion.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(250), tablesAccordion);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private ComparedTable getComparedTableFromTableName(String tableName) {
        return comparison.getComparedTables().stream()
                .filter(ct -> ct.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
    }

    /// CONSTRUCTOR METHODS

    private void refreshAccordion() {
        tablesAccordion.getPanes().setAll(filteredFilterPanes);
    }

    private void constructAccordion() {
        tablesAccordion.getPanes().clear(); // Clears Accordion
        allFiltersPanes.clear();             // Clears master list

        constructTitledPanes();

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


    private void constructTitledPanes() {
        List<ComparedTable> comparedTablesWithFilter = comparison.getComparedTables().stream()
                .filter(ComparedTable::hasFilter)
                .toList();

        if (comparedTablesWithFilter.isEmpty()) {
            return;
        }

        for (ComparedTable comparedTable : comparedTablesWithFilter) {
            constructTitledPane(comparedTable);
        }
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

            List<Node> content = new ArrayList<>();

            TableView<FilterViewModel> tableView = createFilterTableView(comparedTable);
            ObservableList<FilterViewModel> tableItems = getFilterItemsForTable(comparedTable);
            tableView.setItems(tableItems);

            content.add(tableView);

            if (comparedTable.hasTableFilter()) {

                Region spacer = new Region();
                spacer.setPrefHeight(5);
                content.add(spacer);
                content.add(createActionButtonsHBox(comparedTable.getFilter()));

                adjustTableHeight(tableView, 100);
            } else {
                adjustTableHeight(tableView, 35);
            }


            VBox container = new VBox();
            container.getChildren().setAll(content);


            container.setPadding(new Insets(10));
            container.setFillWidth(true);

            titledPane.setContent(container);
            titledPane.setUserData(true);

    }

    private TableView<FilterViewModel> createFilterTableView(ComparedTable comparedTable) {
        TableView<FilterViewModel> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<TableColumn<FilterViewModel, ?>> columns = new ArrayList<>();

        // 1. Column Name (only if no table filter)
        if (!comparedTable.hasTableFilter()) {
            columns.add(createColumnNameColumn());
            columns.add(createFilterTypeColumn());
        }

        // 2. Filter value column (always present)
        columns.add(createFilterValueColumn());

        // 3. Action buttons column (only if no table filter)
        if (!comparedTable.hasTableFilter()) {
            columns.add(createActionColumn(comparedTable));
        }

        // Add all columns in one shot, in order
        tableView.getColumns().addAll(columns);
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

        col.setCellFactory(tc -> {
            TableCell<FilterViewModel, String> cell = new TableCell<>() {
                private final Text text;

                {
                    text = new Text();
                    text.wrappingWidthProperty().bind(tc.widthProperty().subtract(10)); // adjust padding
                    setGraphic(text);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                    } else {
                        text.setText(item);
                    }
                }
            };
            return cell;
        });

        return col;
    }

    private TableColumn<FilterViewModel, Void> createActionColumn(ComparedTable comparedTable) {
        TableColumn<FilterViewModel, Void> actionsCol = new TableColumn<>();

        actionsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FilterViewModel vm = getTableView().getItems().get(getIndex());
                    setGraphic(createActionButtonsHBox(vm.getModel()));
                }
            }
        });

        return actionsCol;
    }

    private HBox createActionButtonsHBox(Filter filter) {
        List<Node> actionButtons = new ArrayList<>();

        if (filter instanceof TableFilter tableFilter) {
            Button copyToClipboardButton = new Button("copy");
            copyToClipboardButton.setOnAction(e -> onCopyToClipBoardButtonClicked(tableFilter));

            actionButtons.add(copyToClipboardButton);
        }

        Button editButton = new Button("edit");
        Button deleteButton = new Button("del");

        editButton.setOnAction(e -> onEditFilterButtonClicked(filter));
        deleteButton.setOnAction(e -> onDeleteFilterButtonClicked(filter));

        actionButtons.add(editButton);
        actionButtons.add(deleteButton);

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.TOP_RIGHT);
        hbox.getChildren().setAll(actionButtons);

        return hbox;
    }

    private ObservableList<FilterViewModel> getFilterItemsForTable(ComparedTable comparedTable) {
        ObservableList<FilterViewModel> items = FXCollections.observableArrayList();

        ComparedTableViewModel comparedTableViewModel = comparedTableViewModels.stream()
                .filter(vm -> vm.getModel().equals(comparedTable))
                .findFirst()
                .orElse(null);
        if (comparedTableViewModel == null) return items; //returns empty

        comparedTableViewModel.updateViewModel();

        if (comparedTable.hasTableFilter()) {

            items.add(comparedTableViewModel.getTableFilterViewModel());

        } else {
            comparedTableViewModel.getComparedTableColumnViewModels().stream()
                    .flatMap(colVM -> colVM.getColumnFilterViewModels().stream())
                    .forEach(items::add);
        }

        return items;
    }

    private void adjustTableHeight(TableView<FilterViewModel> tableView, int ROW_HEIGHT) {
        final int MAX_ROWS = 5;
        final int HEADER_HEIGHT = 30;


        // Set fixed row height
        tableView.setRowFactory(tv -> {
            TableRow<FilterViewModel> row = new TableRow<>();
            row.setPrefHeight(ROW_HEIGHT);
            return row;
        });

        tableView.prefHeightProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    int rowCount = tableView.getItems().size();

                    double prefHeight = (rowCount * ROW_HEIGHT) + HEADER_HEIGHT;
                    double maxHeight = (MAX_ROWS * ROW_HEIGHT) + HEADER_HEIGHT;

                    return Math.min(prefHeight, maxHeight);

                }, tableView.getItems())
        );
    }

    private boolean hasAnyInvalidFilter() {
        return comparison.getComparedTables().stream().anyMatch(comparedTable -> comparedTable.getFilterValidationResult().isInvalid());
    }

    private void showErrorInvalidFilter() {
        if (!hasAnyInvalidFilter()) return;

        List<ComparedTable> tablesWithInvalidFilters = comparison.getComparedTables().stream()
                .filter(comparedTable -> comparedTable.getFilterValidationResult().isInvalid())
                .toList();

        DialogUtils.showInvalidFiltersDialog(currentStage, tablesWithInvalidFilters);
    }


    /// NAVIGATION METHODS

    public void nextStep(MouseEvent mouseEvent) {

        Scene currentScene = currentStage.getScene();
        currentScene.setUserData(SetFiltersScreenController.this);


        if (!needToProcess() && nextScene != null) {
            currentStage.setScene(nextScene);
            return;
        }

        try {
            FxLoadResult<Parent, LoadingScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.LOADING_SCREEN);

            Parent root = screenData.node;
            LoadingScreenController controller = screenData.controller;

            controller.setMessage("Validando filtros, aguarde...");

            Scene scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            currentStage.setScene(scene);
            currentStage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela de carregamento: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        Task<Parent> processFiltersTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {


                FilterService.validateFilters(comparison.getComparedTables().stream()
                        .filter(comparedTable -> !comparedTable.getFilterValidationResult().isValid())
                        .toList());

                if (hasAnyInvalidFilter()) {
                    throw new FilterException(
                            comparison.getComparedTables().stream()
                                    .filter(comparedTable -> comparedTable.getFilterValidationResult().isInvalid())
                                    .toList());
                }

                ComparisonService.processFilters(comparison.getComparedTables());

                ComparisonService.compare(comparison);


                FxLoadResult<Parent, ComparisonResultScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.COMPARISON_RESULT_SCREEN);

                Parent nextScreenRoot = screenData.node;
                ComparisonResultScreenController controller = screenData.controller;

                controller.setCurrentStage(currentStage);
                controller.setComparison(comparison);
                controller.init();

                return nextScreenRoot;
            }
        };


        processFiltersTask.setOnSucceeded(event -> {
            try {

                Parent nextScreenRoot = processFiltersTask.getValue();

                Scene nextScreenScene = new Scene(nextScreenRoot, currentScene.getWidth(), currentScene.getHeight());

                currentStage.setScene(nextScreenScene);


            } catch (Exception e) {
                DialogUtils.showError("Erro de Transição", "Não foi possível exibir a próxima tela: " + e.getMessage());
                e.printStackTrace();
            }
        });


        processFiltersTask.setOnFailed(event -> {
            currentStage.setScene(currentScene);

            Throwable exception = processFiltersTask.getException();

            if (exception instanceof FilterException) {
                showErrorInvalidFilter();
                return;
            }

            DialogUtils.showError("Erro de Processamento",
                    "Ocorreu um erro inesperado: " + exception.getMessage());
            exception.printStackTrace();
        });


        new Thread(processFiltersTask).start();


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
