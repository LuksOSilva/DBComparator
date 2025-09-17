package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.exception.ColumnSettingsException;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.navigator.ComparisonStepsNavigator;
import com.luksosilva.dbcomparator.service.ColumnSettingsService;
import com.luksosilva.dbcomparator.service.ComparedTableService;
import com.luksosilva.dbcomparator.service.SourceService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.compared.ComparedTableColumnViewModel;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;


public class ColumnSettingsScreenController implements BaseController {

    private ComparisonStepsNavigator navigator;

    private Stage currentStage;

    private final Comparison comparison = new Comparison();

    private ObservableList<TitledPane> tableTitledPanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTableTitledPanes;

    //pagination
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 20;

    @FXML
    public Text titleLabel;
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
    public Accordion tablesAccordion;

    @FXML
    public ScrollPane scrollPane;
    @FXML
    public HBox paginationHBox;
    @FXML
    public Button prevBtn;
    @FXML
    public Label pageLabel;
    @FXML
    public Button nextBtn;

    @FXML
    public Button nextStepBtn;
    @FXML
    public Button previousStepBtn;
    @FXML
    public Text cancelBtn;



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
        constructAccordion();
        setupFilterControls();
    }

    private void computeComparedTables() {
        try {

            comparison.getComparedTables().addAll(ComparedTableService.getComparedTables());


        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao carregar tabelas",
                    e.getMessage());
        }
    }


    /// User-called Methods

    public void onFilterButtonClicked(MouseEvent mouseEvent) {
        toggleFilters(filterToggleButton.isSelected());
    }

    public void onResetSettingsToDefaultForAllTablesButtonClicked(ActionEvent event) {
        boolean confirm = DialogUtils.askConfirmation(currentStage,
                "Alterar todas para padrão?",
                "As configurações de todas as tabelas serão alteradas para o padrão. Configurações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean useUserDefault = true;

        resetSettingsForAllTables(useUserDefault);
    }
    public void onResetSettingsToOriginalForAllTablesButtonClicked(ActionEvent event) {
        boolean confirm = DialogUtils.askConfirmation(currentStage,
                "Alterar todas para padrão do sistema?",
                "As configurações de todas as tabelas serão alteradas para o padrão do sistema. Configurações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean useUserDefault = false;

        resetSettingsForAllTables(useUserDefault);
    }
    public void onSaveSettingsForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        ComparedTable comparedTable = (ComparedTable) clickedButton.getUserData();

        boolean confirm = DialogUtils.askConfirmation(currentStage,
                "Salvar Configurações?",
                "As configurações da tabela " + comparedTable.getTableName() + " serão salvas. Se houver um salvamento prévio, será perdido.");
        if (!confirm) {
            return;
        }

        saveSettingsForTable(comparedTable);
    }

    public void onResetSettingsToDefaultForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        ComparedTable comparedTable = (ComparedTable) clickedButton.getUserData();

        boolean confirm = DialogUtils.askConfirmation(currentStage,
                "Alterar para padrão?",
                "As configurações da tabela "+ comparedTable.getTableName() +" serão alteradas para o padrão. Alterações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean useUserDefault = true;

        resetSettingsForTable(comparedTable, useUserDefault);
    }

    public void onResetSettingsToOriginalForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        ComparedTable comparedTable = (ComparedTable) clickedButton.getUserData();

        boolean confirm = DialogUtils.askConfirmation(currentStage,
                "Alterar para padrão?",
                "As configurações da tabela "+ comparedTable.getTableName() +" serão alteradas para o padrão do sistema. Alterações não salvas serão perdidas.");
        if (!confirm) {
            return;
        }

        boolean useUserDefault = false;

        resetSettingsForTable(comparedTable, useUserDefault);
    }

    /// Helper Methods

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

    private void resetSettingsForAllTables(boolean useUserDefault) {
        try {

            ColumnSettingsService.processComparedTableConfigs(useUserDefault, comparison.getComparedTables());


            for (ComparedTable comparedTable : comparison.getComparedTables()) {
                for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {
                    comparedTableColumn.setColumnConfig(null);
                }
            }
            tablesAccordion.setExpandedPane(null);

        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao alterar configurações",
                    e.getMessage());
        }
    }


    private void resetSettingsForTable(ComparedTable comparedTable, boolean useUserDefault) {
        try {

            ColumnSettingsService.processComparedTableConfigs(useUserDefault, List.of(comparedTable));

            //clears configs so it'll be loaded again when user expands the pane.
            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {
                comparedTableColumn.setColumnConfig(null);
            }

            //ColumnSettingsService.loadConfigOfComparedColumns(comparedTable.getComparedTableColumns());
            tablesAccordion.setExpandedPane(null);


        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao alterar configurações",
                    e.getMessage());
        }
    }

    private void saveSettingsForTable(ComparedTable comparedTable) {
        try {

            ColumnSettingsService.saveColumnSettingsAsDefault(comparedTable);

        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao salvar configurações",
                    e.getMessage());
        }
    }

    private boolean hasAnyInvalidColumnSettings() {
        return comparison.getComparedTables().stream().anyMatch(ComparedTable::isColumnSettingsInvalid);
    }

    private void showErrorInvalidSettings() {
        if (!hasAnyInvalidColumnSettings()) return;

        List<ComparedTable> tablesWithInvalidSettings = comparison.getComparedTables().stream()
                .filter(ComparedTable::isColumnSettingsInvalid).toList();

        DialogUtils.showInvalidColumnSettingsDialog(currentStage, tablesWithInvalidSettings);

    }

    private void showErrorInvalidSettings(ComparedTable comparedTable) {
        if (comparedTable.isColumnSettingsValid()) return;

        List<ComparedTable> comparedTableToList = new ArrayList<>();
        comparedTableToList.add(comparedTable);

        DialogUtils.showInvalidColumnSettingsDialog(currentStage, comparedTableToList);
    }



    /// Constructor Methods

    private void setupFilterControls() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList("tabela", "coluna"));
        filterTypeComboBox.setValue("tabela");
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals("coluna")) {
                setupFilterByColumnName();
            }
            applyFilter();
        });


        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showOnlyInvalidColumnSettingsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showOnlySchemaDiffersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        applyFilter();
    }

    private void setupFilterByColumnName() {
        List<ComparedTable> tablesWithoutColumns = comparison.getComparedTables().stream()
                .filter(table -> table.getComparedTableColumns().isEmpty()).toList();
        if (tablesWithoutColumns.isEmpty()) return;


        try {

            ComparedTableService.getComparedColumnsOfTables(tablesWithoutColumns);

        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Algo deu errado ao carregar as colunas",
                    e.getMessage());
        }
    }

    private List<TitledPane> getFilteredTitledPanes() {
        String filterText = searchTextField.getText().toLowerCase().trim();
        String filterType = filterTypeComboBox.getValue();

        return tableTitledPanes.stream()
                .filter(pane -> {
                    String tableName = pane.getText().toLowerCase();

                    ComparedTable comparedTable = comparison.getComparedTables().stream()
                            .filter(ct -> ct.getTableName().equalsIgnoreCase(tableName))
                            .findFirst().orElse(null);
                    if (comparedTable == null) return false;

                    // Filter by text
                    if (!filterText.isEmpty()) {
                        if ("tabela".equalsIgnoreCase(filterType)) {
                            if (!tableName.contains(filterText)) return false;

                        } else if ("coluna".equalsIgnoreCase(filterType)) {

                        boolean columnMatch = comparedTable.getComparedTableColumns().stream()
                            .anyMatch(comparedTableColumn -> comparedTableColumn.getColumnName().contains(filterText));

                        if (!columnMatch) return false;
                        }
                    }

                    if (showOnlySchemaDiffersCheckBox.isSelected()
                        && !comparedTable.hasSchemaDifference()) return false;


                    if (showOnlyInvalidColumnSettingsCheckBox.isSelected()
                        && !comparedTable.isColumnSettingsInvalid()) return false;

                    return true;
                }).toList();
    }

    private void applyFilter() {
        String filterText = searchTextField.getText().toLowerCase().trim();

        List<TitledPane> filtered = getFilteredTitledPanes();

        if (filtered.isEmpty()) {
            tablesAccordion.getPanes().setAll();
            hidePagination("Nenhuma tabela encontrada para \"" + filterText + "\".");
            return;
        }

        // Apply pagination
        showPagination();
        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex;

        if (fromIndex >= filtered.size()) {
            currentPage = 0;
            fromIndex = 0;
            toIndex = Math.min(ITEMS_PER_PAGE, filtered.size());
        } else {
            toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filtered.size());
        }

        List<TitledPane> pageItems = filtered.subList(fromIndex, toIndex);

        tablesAccordion.getPanes().setAll(pageItems);

        /* */
        Accordion newAccordion = new Accordion();
        newAccordion.getPanes().setAll(tablesAccordion.getPanes());
        tablesAccordion = newAccordion;

        VBox content = new VBox(10, tablesAccordion, paginationHBox);
        scrollPane.setContent(content);
        /* */

        // fade-in style
        fadeInAccordion();

        int totalPages = (int) Math.ceil(filtered.size() / (double) ITEMS_PER_PAGE);
        if (totalPages == 1) {
            hidePagination("");
        }
        pageLabel.setText("Página " + (currentPage + 1) + " de " + totalPages);

        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(toIndex >= filtered.size());

        prevBtn.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                applyFilter();
            }
        });

        nextBtn.setOnAction(e -> {
            if (toIndex < filtered.size()) {
                currentPage++;
                applyFilter();
            }
        });
    }

    private void hidePagination(String text) {
        prevBtn.setVisible(false);
        nextBtn.setVisible(false);
        if (text.isBlank()) {
            pageLabel.setVisible(false);
            return;
        }
        pageLabel.setVisible(true); //could be hidden if there was 1 page
        pageLabel.setText(text);
    }
    private void showPagination() {
        prevBtn.setVisible(true);
        nextBtn.setVisible(true);
        pageLabel.setVisible(true);
    }

    private void constructAccordion() {
        if (comparison.getComparedTables().isEmpty()) {
            return;
        }

        tablesAccordion.getPanes().clear(); // Clears Accordion
        tableTitledPanes.clear();             // Clears master list

        filterToggleButton.setSelected(false); //hides filters

        // Populate tableTitledPanes with all TitledPanes
        tableTitledPanes.addAll(constructTitledPanes());

        // Initialize FilteredList based on tableTitledPanes
        filteredTableTitledPanes = new FilteredList<>(tableTitledPanes, pane -> true);

        // Set the Accordion's panes to the filtered list. This is done ONCE.
        tablesAccordion.getPanes().setAll(filteredTableTitledPanes);
    }


    private List<TitledPane> constructTitledPanes() {
        List<TitledPane> titledPaneList = new ArrayList<>();

        for (ComparedTable comparedTable : comparison.getComparedTables()) {
            String tableName = comparedTable.getTableName();

            TitledPane tablePane = new TitledPane();
            tablePane.setText(tableName);

            tablePane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {

                boolean columnsNeedLoading = comparedTable.getComparedTableColumns().isEmpty();

                boolean configsNeedLoading = columnsNeedLoading ||
                        comparedTable.getComparedTableColumns().stream().anyMatch(c -> c.getColumnSetting() == null);

                if (isNowExpanded && (columnsNeedLoading || configsNeedLoading)) {
                    try {

                        if (columnsNeedLoading) {
                            ComparedTableService.getComparedColumnsOfTables(List.of(comparedTable));
                        }

                        if (configsNeedLoading) {
                            ColumnSettingsService.getConfigOfComparedColumns(comparedTable.getComparedTableColumns());
                        }

                    } catch (Exception e) {
                        DialogUtils.showError(currentStage,
                                "Algo deu errado ao buscar dados da tabela",
                                e.getMessage());
                        return;
                    }

                    constructTitledPaneContent(tablePane, comparedTable);
                }
            });

            titledPaneList.add(tablePane);
        }

        return titledPaneList;
    }

    private void constructTitledPaneContent(TitledPane titledPane, ComparedTable comparedTable) {

        TableView<ComparedTableColumnViewModel> tableView = constructTitledPaneContentTableView(comparedTable);
        HBox buttonBox = constructTitledPaneContentButtonBox(comparedTable);

        VBox contentContainer = new VBox(10, tableView, buttonBox);
        contentContainer.setPadding(new Insets(10));
        contentContainer.setFillWidth(true);

        titledPane.setContent(contentContainer);
        titledPane.setUserData(true);
    }

    private TableView<ComparedTableColumnViewModel> constructTitledPaneContentTableView(ComparedTable comparedTable) {
        final double TABLE_ROW_HEIGHT = 28.0;
        final double TABLE_HEADER_HEIGHT = 30.0;

        List<ComparedTableColumnViewModel> comparedTableColumnViewModelList =
                comparedTable.getComparedTableColumns().stream()
                        .map(ComparedTableColumnViewModel::new)
                        .toList();

        TableView<ComparedTableColumnViewModel> tableView = new TableView<>();
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ComparedTableColumnViewModel item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                    setTooltip(null);
                } else if (!item.existsOnAllSources()) {
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
        tableView.setPlaceholder(new Label("Algo deu errado! Nenhuma informação encontrada"));

        TableColumn<ComparedTableColumnViewModel, Number> rowIndexColumn = new TableColumn<>("#");
        rowIndexColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(tableView.getItems().indexOf(cellData.getValue()) + 1));
        rowIndexColumn.setSortable(false);

        TableColumn<ComparedTableColumnViewModel, String> columnNameColumn = new TableColumn<>("Coluna");
        columnNameColumn.setCellValueFactory(cellData -> cellData.getValue().columnNameProperty());

        TableColumn<ComparedTableColumnViewModel, String> pkColumn = new TableColumn<>("PK");
        pkColumn.setCellValueFactory(cellData -> cellData.getValue().isPkAnySourceStringProperty());

        TableColumn<ComparedTableColumnViewModel, Boolean> isIdentifierColumn = new TableColumn<>("Identificador");
        isIdentifierColumn.setCellValueFactory(cellData -> cellData.getValue().isIdentifierProperty());
        isIdentifierColumn.setCellFactory(col -> new CheckBoxTableCell<>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    ComparedTableColumnViewModel viewModel = getTableView().getItems().get(getIndex());
                    setDisable(!viewModel.existsOnAllSources());
                }
            }
        });


        TableColumn<ComparedTableColumnViewModel, Boolean> isComparableColumn = new TableColumn<>("Comparável");
        isComparableColumn.setCellValueFactory(cellData -> cellData.getValue().isComparableProperty());
        isComparableColumn.setCellFactory(col -> new CheckBoxTableCell<>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    ComparedTableColumnViewModel viewModel = getTableView().getItems().get(getIndex());
                    setDisable(!viewModel.existsOnAllSources());
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

    private HBox constructTitledPaneContentButtonBox(ComparedTable comparedTable) {
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

        // style
        resetToOriginal.getStyleClass().add("btn-action");
        resetToDefaultBtn.getStyleClass().add("btn-action");
        saveAsDefaultBtn.getStyleClass().add("btn-action");

        // Add user data
        resetToOriginal.setUserData(comparedTable);
        resetToDefaultBtn.setUserData(comparedTable);
        saveAsDefaultBtn.setUserData(comparedTable);

        return buttonBox;
    }



    /// Navigation Methods

    public void nextStep(MouseEvent mouseEvent) {
        Scene currenteScene = currentStage.getScene();

        navigator.goTo(FxmlFiles.LOADING_SCREEN, ctrl -> {
            ctrl.setTitle("Validando configurações, aguarde...");
        });

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ColumnSettingsService.processColumnSettings(true, comparison.getComparedTables());
                return null;
            }
        };

        navigator.runTask(task,
                () -> {
                    navigator.goTo(FxmlFiles.SET_FILTERS_SCREEN, ctrl -> {
                    ctrl.setTitle("configure os filtros para comparação");
                    ctrl.init(comparison.getConfigRegistry(), navigator);
                    });
                },
                ex -> {
                    navigator.getStage().setScene(currenteScene);
                    if (ex instanceof ColumnSettingsException) {
                        showErrorInvalidSettings();
                        return;
                    }
                    DialogUtils.showError(currentStage,
                            "Ocorreu um erro inesperado ao validar as configurações:",
                            ex.getMessage());

                });
    }


    public void previousStep(MouseEvent mouseEvent) {
        Scene currenteScene = currentStage.getScene();

        navigator.goTo(FxmlFiles.LOADING_SCREEN, ctrl -> {
            ctrl.setTitle("Processando tabelas, aguarde...");
        });

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ColumnSettingsService.processColumnSettings(false, comparison.getComparedTables());
                return null;
            }
        };

        navigator.runTask(task,
                () -> {
                    navigator.goTo(FxmlFiles.SELECT_TABLES_SCREEN, ctrl -> {
                    ctrl.setTitle("selecione os bancos para comparação");
                    ctrl.init(comparison.getConfigRegistry(), navigator);
                    });
                },
                ex -> {
                    navigator.getStage().setScene(currenteScene);
                    if (ex instanceof ColumnSettingsException) {
                        showErrorInvalidSettings();
                        return;
                    }
                    DialogUtils.showError(currentStage,
                            "Ocorreu um erro inesperado ao validar as configurações:",
                            ex.getMessage());

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
