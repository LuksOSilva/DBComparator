package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.controller.HomeScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.model.live.comparison.result.TableComparisonResult;
import com.luksosilva.dbcomparator.queue.ComparisonQueueManager;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.compared.ComparedTableViewModel;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.result.TableComparisonResultViewModel;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ComparisonResultScreenController {



    private Stage currentStage;
    public void setCurrentStage(Stage currentStage) { this.currentStage = currentStage; }
    private List<Stage> openedStages = new ArrayList<>();

    private Comparison comparison;
    private final ComparisonResult comparisonResult = new ComparisonResult();

    private boolean isComparisonSaved = false;
    private boolean isComparisonImported = false;

    private final List<ComparedTableViewModel> comparedTableViewModels = new ArrayList<>();
    private final List<TableComparisonResultViewModel> tableComparisonResultViewModels = new ArrayList<>();

    private final Map<ComparedTable, TitledPane> perComparedTableResultPane = new HashMap<>();

    private final ObservableList<TitledPane> tableComparisonResultPanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTableComparisonResultPanes = new FilteredList<>(tableComparisonResultPanes, s -> true);

    private ComparisonQueueManager comparisonQueueManager = new ComparisonQueueManager(); // or 1 for sequential


    public void setComparison(Comparison comparison) { this.comparison = comparison; }
    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }

    @FXML
    public TextField searchTextField;
    @FXML
    public ComboBox<String> filterTypeComboBox;

    @FXML
    public Accordion tablesAccordion;
    @FXML
    public Button saveBtn;
    @FXML
    public Button leaveBtn;

    public void init(Comparison comparison) {
        saveBtn.setVisible(false);
        saveBtn.setManaged(false);

        this.comparison = comparison;
        isComparisonImported = true;
        init();
    }

    public void init() {
        setupComparedTableViewModels();
        setupComparedTablesAccordion();
        constructComparedTableAccordion();
        setupFilterControls();

        startComparison();
    }

    private void startComparison() {

        if (isComparisonImported) {
            for (TableComparisonResult tableComparisonResult : comparison.getComparisonResult().getTableComparisonResults()) {
                addComparedTableResult(tableComparisonResult);
            }
            return;
        }

        List<ComparedTable> sortedComparedTableList = getSortedComparedTableList(comparison.getComparedTables());

        // add tables to the queue
        for (ComparedTable comparedTable : sortedComparedTableList) {
            comparisonQueueManager.add(comparedTable);
        }

        // start the queue processor
        comparisonQueueManager.start(comparison.getComparedSources(), this);

    }

    private boolean isComparisonFinished() {
        return comparison.getComparedTables().size() == comparisonResult.getTableComparisonResults().size();
    }

    public void addComparedTableResult(TableComparisonResult tableComparisonResult) {

        comparisonResult.addTableComparisonResult(tableComparisonResult);
        TableComparisonResultViewModel tableComparisonResultVM = setupTableResultViewModel(tableComparisonResult);

        ComparedTable comparedTable = tableComparisonResultVM.getModel().getComparedTable();
        TitledPane titledPane = perComparedTableResultPane.get(comparedTable);

        enableTitledPane(titledPane, tableComparisonResultVM);
    }

    public void enableTitledPane(TitledPane titledPane, TableComparisonResultViewModel tableComparisonResultVM) {

        constructComparedTableAccordionContent(titledPane, tableComparisonResultVM);

        titledPane.setDisable(false);
    }

    /// User actions

    public void onShowComparisonResultButtonClicked(TableComparisonResultViewModel tableComparisonResultViewModel) {
        Stage previouslyOpenedStage = openedStages.stream()
                .filter(openedStage -> tableComparisonResultViewModel.equals(openedStage.getUserData()))
                .findFirst()
                .orElse(null);

        if (previouslyOpenedStage != null) {
            DialogUtils.showInCenter(currentStage, previouslyOpenedStage);
            previouslyOpenedStage.toFront();
            previouslyOpenedStage.requestFocus();

            return;
        }

        Stage newStage = DialogUtils.showTableComparisonResultScreen(currentStage, tableComparisonResultViewModel);
        if (newStage == null) {
            return; /// todo: show warning
        }

        newStage.setUserData(tableComparisonResultViewModel);

        openedStages.add(newStage);
    }

    public void onShowSchemaDifferencesButtonClicked(TableComparisonResultViewModel tableComparisonResultViewModel) {

    }

    private void applyFilter() {
        String filterText = searchTextField.getText().toLowerCase().trim();
        String filterType = filterTypeComboBox.getValue();

        filteredTableComparisonResultPanes.setPredicate(pane -> {
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


        tablesAccordion.getPanes().setAll(filteredTableComparisonResultPanes);


        fadeInAccordion();
    }

    private void fadeInAccordion() {
        tablesAccordion.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(250), tablesAccordion);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /// setups

    private void setupFilterControls() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList("tabela", "coluna"));
        filterTypeComboBox.setValue("tabela");


        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());


        applyFilter();
    }

    public void setupComparedTableViewModels() {
        for (ComparedTable comparedTable : getSortedComparedTableList(comparison.getComparedTables())) {
            comparedTableViewModels.add(new ComparedTableViewModel(comparedTable));
        }
    }

    public TableComparisonResultViewModel setupTableResultViewModel(TableComparisonResult tableComparisonResult) {
        TableComparisonResultViewModel vm = new TableComparisonResultViewModel(tableComparisonResult);
        tableComparisonResultViewModels.add(vm);

        return vm;
    }



    private void setupComparedTablesAccordion() {
        tablesAccordion.getPanes().clear();
        tableComparisonResultPanes.clear();
        perComparedTableResultPane.clear();

        filteredTableComparisonResultPanes = new FilteredList<>(tableComparisonResultPanes, pane -> true);


        Bindings.bindContent(tablesAccordion.getPanes(), filteredTableComparisonResultPanes);
    }


    /// Constructors


    public void constructComparedTableAccordion() {

        for (ComparedTableViewModel vm : comparedTableViewModels) {
            TitledPane titledPane = new TitledPane();
            titledPane.setText(vm.getTableName());


            tableComparisonResultPanes.add(titledPane);
            perComparedTableResultPane.put(vm.getModel(), titledPane);

            titledPane.setDisable(true);
        }
    }

    private void constructComparedTableAccordionContent(TitledPane titledPane, TableComparisonResultViewModel vm) {

        // --- LABELS ---
        VBox labelsBox = getLabelsVBox(vm);

        // --- BUTTONS ---
        VBox buttonsBox = getActionButtonsVBox(vm);

        // --- MAIN CONTAINER ---
        HBox contentContainer = new HBox(20);
        contentContainer.setPadding(new Insets(10));
        contentContainer.getChildren().addAll(labelsBox, buttonsBox);
        HBox.setHgrow(labelsBox, Priority.ALWAYS);

        titledPane.setContent(contentContainer);
        titledPane.setUserData(true);
    }


    /// Constructor helpers

    private VBox getLabelsVBox(TableComparisonResultViewModel vm) {
        VBox labelsBox = new VBox(6);
        labelsBox.setAlignment(Pos.TOP_LEFT);

        Label recordCountLabel = new Label("Número de registros: \n" + vm.getPerSourceRecordCount());
        Label diffRecordCountLabel = new Label("Registros com diferença: " + vm.getDiffRecordCount());
        Label diffSchemaLabel = new Label("Diferença no schema: " + vm.hasSchemaDifference());

        recordCountLabel.setStyle("-fx-font-weight: bold;");
        labelsBox.getChildren().addAll(recordCountLabel, diffRecordCountLabel, diffSchemaLabel);

        return labelsBox;
    }

    private VBox getActionButtonsVBox(TableComparisonResultViewModel vm) {
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button compareRecordsButton = new Button("Comparar registros");
        compareRecordsButton.getStyleClass().add("btn-action");
        compareRecordsButton.setOnAction(e -> onShowComparisonResultButtonClicked(vm));

        Button compareSchemaButton = new Button("Comparar schemas");
        compareSchemaButton.getStyleClass().add("btn-action");
        compareSchemaButton.setOnAction(e -> onShowSchemaDifferencesButtonClicked(vm));

        buttonsBox.getChildren().addAll(compareRecordsButton, compareSchemaButton);

        return buttonsBox;
    }

    /// HELPERS

    private ComparedTable getComparedTableFromTableName(String tableName) {
        return comparison.getComparedTables().stream()
                .filter(ct -> ct.getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
    }

    private List<ComparedTable> getSortedComparedTableList(List<ComparedTable> unsortedComparedTableList) {

        List<ComparedTable> sortedComparedTableList = new ArrayList<>(unsortedComparedTableList);
        sortedComparedTableList.sort(Comparator.comparingInt(ComparedTable::getTotalRecordCount));

        return sortedComparedTableList;
    }

    public void saveComparisonBtnClicked(MouseEvent mouseEvent) {
        if (!isComparisonFinished()) {
            DialogUtils.showWarning(currentStage,
                    "Comparação ainda não finalizada",
                    "Você deve aguardar todas as tabelas serem comparadas para salvar"
            );
            return;
        }

        comparison.setComparisonResult(comparisonResult);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Comparação");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DBC Files", "*.dbc", "*.json"));

            File file = fileChooser.showSaveDialog(currentStage);
            if (file == null) return;

            ComparisonService.saveComparison(comparison, file);
        }
        catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao salvar comparação",
                    e.getMessage()
            );
            return;
        }

        DialogUtils.showInfo(currentStage, "Sucesso!", "Comparação salva.");
        isComparisonSaved = true;
    }

    public void leaveComparisonBtnClicked(MouseEvent mouseEvent) {
        String message = "Deseja realmente sair dessa comparação? "
                + ((isComparisonImported || isComparisonSaved) ? "" : "Nenhuma informação será salva.");

        boolean confirmCancel = DialogUtils.askConfirmation(currentStage,
                "Sair", message);
        if (!confirmCancel) {
            return;
        }

        comparisonQueueManager.stop();

        try {
            FxLoadResult<Parent, HomeScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.HOME_SCREEN);

            Parent root = screenData.node;
            HomeScreenController controller = screenData.controller;

            controller.setCurrentStage(currentStage);
            controller.init();

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, currentStage.getScene().getWidth(), currentStage.getScene().getHeight());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            DialogUtils.showError(currentStage,
                    "Erro de Carregamento",
                    "Não foi possível carregar a tela inicial: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
