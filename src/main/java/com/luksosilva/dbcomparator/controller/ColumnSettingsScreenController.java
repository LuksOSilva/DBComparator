package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.*;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FxLoadResult;
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

        public ComparedTableColumnSettings getViewModelColumnSetting() {
            return new ComparedTableColumnSettings(comparableProperty.get(), identifierProperty.get());
        }

        public String getPrimaryKeyCountText() {
            Map<ComparedSource, SourceTableColumn> map = comparedTableColumn.getPerSourceTableColumn();

            long pkCount = map.values().stream().filter(SourceTableColumn::isPk).count();
            int totalSources = map.size();

            if (pkCount == 0) return "";
            if (pkCount == totalSources) return "Y";

            return pkCount + "/" + totalSources;
        }
        public void setProperties() {
            this.identifierProperty.set(comparedTableColumn.getColumnSetting().isIdentifier());
            this.comparableProperty.set(comparedTableColumn.getColumnSetting().isComparable());
        }
        public void resetToDefault() {
            identifierProperty.set(defaultIdentifierProperty.get());
            comparableProperty.set(defaultComparableProperty.get());
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



    public void init() {
        constructAccordion();
        setupFilterControls();
    }


    /// User-called Methods

    public void onFilterButtonClicked(MouseEvent mouseEvent) {
        toggleFilters(filterToggleButton.isSelected());
    }

    public void onSaveSettingsForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String tableName = (String) clickedButton.getUserData();
        boolean saveAsDefault = true;

        saveSettingsForTable(tableName, saveAsDefault);
    }
    public void onResetSettingsToDefaultForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String tableName = (String) clickedButton.getUserData();
        boolean loadFromDb = true;

        resetSettingsForTable(tableName, loadFromDb);
    }

    public void onResetSettingsToOriginalForTableButtonClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String tableName = (String) clickedButton.getUserData();
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

    private void resetSettingsForTable(String tableName, boolean loadFromDb) {
        ComparedTable comparedTable = comparison.getComparedTables().stream()
                .filter(ct -> ct.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
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
        ComparedTable comparedTable = comparison.getComparedTables().stream()
                .filter(ct -> ct.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
        if (comparedTable == null) return;

        List<ComparedTableColumnViewModel> comparedTableColumnViewModelList =
                perTableComparedColumnViewModel.get(comparedTable.getTableName());
        if (comparedTableColumnViewModelList.isEmpty()) return;

        Map<ComparedTableColumn, ComparedTableColumnSettings> perComparedTableColumnSettings = new HashMap<>();
        for (ComparedTableColumnViewModel comparedTableColumnViewModel : comparedTableColumnViewModelList) {
            perComparedTableColumnSettings.put
                    (comparedTableColumnViewModel.comparedTableColumn,
                    comparedTableColumnViewModel.getViewModelColumnSetting());
        }

        //saves default
        ComparisonService.processColumnSettings(comparison, perComparedTableColumnSettings, saveAsDefault);

        //updates default values
        comparedTableColumnViewModelList.forEach(ComparedTableColumnViewModel::setDefault);
    }

    public List<ComparedTableColumnViewModel> getAlteredColumnSettings() {
        return perTableComparedColumnViewModel.values()
                .stream()          // Stream<List<ComparedTableColumnViewModel>>
                .flatMap(List::stream) // Flattens to Stream<ComparedTableColumnViewModel>
                .filter(ComparedTableColumnViewModel::isAltered) // Filter for altered columns
                .toList(); // Collect into a List
    }


    /// Constructor Methods

    private void setupFilterControls() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList("tabela", "coluna"));
        filterTypeComboBox.setValue("tabela");


        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showInAllSourcesOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showDiffRecordCountOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        showSelectedOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilter());

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

//            // show only selected filter
//            if (showSelectedOnlyCheckBox.isSelected()) {
//                if (!selectedTableNames.contains(pane.getText())) return false;
//            }
//
//            // show only different record count filter
//            if (showDiffRecordCountOnlyCheckBox.isSelected()) {
//                Map<ComparedSource, SourceTable> perSource = groupedTables.get(pane.getText());
//                if (perSource == null) return false;
//
//                Set<Integer> rowCounts = new HashSet<>();
//                for (SourceTable st : perSource.values()) {
//                    rowCounts.add(st.getRecordCount());
//                }
//                if (rowCounts.size() <= 1) return false;  // Só exibe se tiver diferenças
//            }
//
//            // show only available in all sources filter
//            if (showInAllSourcesOnlyCheckBox.isSelected()) {
//                int totalSources = comparison.getComparedSources().size();
//                Map<ComparedSource, SourceTable> perSource = groupedTables.get(pane.getText());
//                if (perSource == null || perSource.size() < totalSources) return false;
//            }

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

        //TableView<ComparedTableColumn> tableView = constructTitledPaneContentTableView(titledPane.getText());
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

        ComparedTable comparedTable = comparison.getComparedTables().stream()
                .filter(ct -> ct.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
        if (comparedTable == null) return null;

        List<ComparedTableColumnViewModel> comparedTableColumnViewModelList = comparedTable.getComparedTableColumns().stream()
                .map(comparedTableColumn -> new ComparedTableColumnViewModel(comparedTableColumn))
                .collect(Collectors.toList());

        TableView<ComparedTableColumnViewModel> tableView = new TableView<>();
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
        isIdentifierColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

        TableColumn<ComparedTableColumnViewModel, Boolean> isComparableColumn = new TableColumn<>("Comparável");
        isComparableColumn.setCellValueFactory(cellData -> cellData.getValue().comparableProperty);
        isComparableColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

        tableView.getColumns().addAll(rowIndexColumn, columnNameColumn, pkColumn, isIdentifierColumn, isComparableColumn);


        ObservableList<ComparedTableColumnViewModel> tableItems = FXCollections.observableArrayList();

        tableItems.addAll(comparedTableColumnViewModelList);

        tableView.setItems(tableItems);

        double calculatedPrefHeight = (tableItems.size() * TABLE_ROW_HEIGHT) + TABLE_HEADER_HEIGHT;
        tableView.setPrefHeight(Math.max(calculatedPrefHeight, TABLE_HEADER_HEIGHT));

        tableView.setEditable(true);
        isIdentifierColumn.setEditable(true);
        isComparableColumn.setEditable(true);


        perTableComparedColumnViewModel.put(tableName, comparedTableColumnViewModelList);



        return tableView;
    }

    private HBox constructTitledPaneContentButtonBox(String tableName) {
        // Create the buttons
        Button resetToOriginal = new Button("Alterar para padrão do sistema");
        Button saveAsDefaultBtn = new Button("Salvar como padrão");
        Button resetToDefaultBtn = new Button("Alterar para padrão");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Put them in an HBox
        HBox buttonBox = new HBox(10, resetToOriginal, spacer, saveAsDefaultBtn, resetToDefaultBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT); // Right-aligned
        buttonBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding

        // Add logic for the buttons
        resetToOriginal.setOnAction(this::onResetSettingsToOriginalForTableButtonClicked);
        saveAsDefaultBtn.setOnAction(this::onSaveSettingsForTableButtonClicked);
        resetToDefaultBtn.setOnAction(this::onResetSettingsToDefaultForTableButtonClicked);

        // Add user data
        resetToOriginal.setUserData(tableName);
        saveAsDefaultBtn.setUserData(tableName);
        resetToDefaultBtn.setUserData(tableName);

        return buttonBox;
    }



    /// Navigation Methods

    public void nextStep(MouseEvent mouseEvent) {

        Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

        try {
            FxLoadResult<Parent, LoadingScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.LOADING_SCREEN);

            Parent root = screenData.node;
            LoadingScreenController controller = screenData.controller;

            controller.setMessage("Processando customizações, aguarde...");

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

                for (String tableName : perTableComparedColumnViewModel.keySet()) {
                    saveSettingsForTable(tableName, saveAsDefault);
                }

//                List<ComparedTableColumnViewModel> alteredColumns = getAlteredColumnSettings();
//
//                Map<ComparedTableColumn, ComparedTableColumnSettings> perComparedTableColumnSettings = new HashMap<>();
//                for (ComparedTableColumnViewModel comparedTableColumnViewModel : alteredColumns) {
//                    perComparedTableColumnSettings.put
//                            (comparedTableColumnViewModel.comparedTableColumn,
//                                    comparedTableColumnViewModel.getViewModelColumnSetting());
//                }
//
//                ComparisonService.processColumnSettings(comparison, perComparedTableColumnSettings, false);


                FxLoadResult<Parent, SetFiltersScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.SET_FILTERS_SCREEN);

                Parent nextScreenRoot = screenData.node;
                SetFiltersScreenController controller = screenData.controller;

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
            DialogUtils.showError("Erro de Processamento", "Ocorreu um erro durante o processamento: " + processColumnSettingsTask.getException().getMessage());
            processColumnSettingsTask.getException().printStackTrace();

            try {
                FxLoadResult<Parent, ColumnSettingsScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.COLUMN_SETTINGS_SCREEN);

                Parent root = screenData.node;

                Scene currentScreenScene = new Scene(root);
                currentStage.setScene(currentScreenScene);

            } catch (IOException e) {
                DialogUtils.showError("Erro de Recuperação", "Não foi possível recarregar a tela anterior: " + e.getMessage());
                e.printStackTrace();
            }
        });


        new Thread(processColumnSettingsTask).start();

    }

    public void previousStep(ActionEvent event) {
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
