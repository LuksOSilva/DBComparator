package com.luksosilva.dbcomparator.controller.comparisonScreens;

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
import com.luksosilva.dbcomparator.viewmodel.comparison.compared.ComparedTableViewModel;
import com.luksosilva.dbcomparator.viewmodel.comparison.result.*;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
    private FilteredList<TitledPane> filteredTableComparisonResultPanes;

    private ComparisonQueueManager comparisonQueueManager = new ComparisonQueueManager(); // or 1 for sequential


    public void setComparison(Comparison comparison) { this.comparison = comparison; }
    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }




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
            previouslyOpenedStage.show();
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


    /// setups

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



    private void constructComparedTableAccordionContent(TitledPane titledPane, TableComparisonResultViewModel tableComparisonResultViewModel) {

        List<Node> contentLabels = getContentLabels(tableComparisonResultViewModel);
        List<Node> contentButtons = getContentActionButtons(tableComparisonResultViewModel);

        List<Node> content = new ArrayList<>();
        content.addAll(contentLabels);
        content.addAll(contentButtons);

        // Combine the table and buttons in a VBox
        VBox contentContainer = new VBox(10);
        contentContainer.getChildren().addAll(content);

        contentContainer.setPadding(new Insets(10));
        contentContainer.setFillWidth(true);

        titledPane.setContent(contentContainer);
        titledPane.setUserData(true);
    }



    /// Constructor helpers

    private List<Node> getContentLabels(TableComparisonResultViewModel tableComparisonResultViewModel) {
        List<Node> contentLabels = new ArrayList<>();

        Label recordCountLabel = new Label();
        Label diffRecordCountLabel = new Label();
        Label diffSchemaLabel = new Label();

        recordCountLabel.setText("Número de registros:\n" + tableComparisonResultViewModel.getPerSourceRecordCount());
        diffRecordCountLabel.setText("Registros com diferença:\n" + tableComparisonResultViewModel.getDiffRecordCount());
        diffSchemaLabel.setText("Possui diferença no schema:\n" + tableComparisonResultViewModel.hasSchemaDifference());

        contentLabels.add(recordCountLabel);
        contentLabels.add(diffRecordCountLabel);
        contentLabels.add(diffSchemaLabel);

        return contentLabels;
    }

    private List<Node> getContentActionButtons(TableComparisonResultViewModel tableComparisonResultViewModel) {
        List<Node> contentButtons = new ArrayList<>();

        Button showComparisonResultButton = new Button("comparar registros");
        showComparisonResultButton.setOnAction(e -> onShowComparisonResultButtonClicked(tableComparisonResultViewModel));

        Button showSchemaComparisonButton = new Button("comparar schemas");
        showSchemaComparisonButton.setOnAction(e -> onShowSchemaDifferencesButtonClicked(tableComparisonResultViewModel));

        showComparisonResultButton.getStyleClass().add("btn-action");
        showSchemaComparisonButton.getStyleClass().add("btn-action");

        contentButtons.add(showComparisonResultButton);
        contentButtons.add(showSchemaComparisonButton);

        return contentButtons;
    }

    /// HELPERS


    private List<ComparedTable> getSortedComparedTableList(List<ComparedTable> unsortedComparedTableList) {

        List<ComparedTable> sortedComparedTableList = new ArrayList<>(unsortedComparedTableList);
        sortedComparedTableList.sort(Comparator.comparingInt(ComparedTable::getTotalRecordCount));

        return sortedComparedTableList;
    }

    public void saveComparisonBtnClicked(MouseEvent mouseEvent) {
        if (!isComparisonFinished()) {
            DialogUtils.showWarning(
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
            DialogUtils.showError(
                    "Erro ao salvar comparação",
                    e.getMessage()
            );
            return;
        }

        DialogUtils.showInfo("Sucesso!", "Comparação salva.");
        isComparisonSaved = true;
    }

    public void leaveComparisonBtnClicked(MouseEvent mouseEvent) {
        String message = "Deseja realmente sair dessa comparação? "
                + ((isComparisonImported || isComparisonSaved) ? "" : "Nenhuma informação será salva.");

        boolean confirmCancel = DialogUtils.askConfirmation("Sair", message);
        if (!confirmCancel) {
            return;
        }

        comparisonQueueManager.stop();

        try {
            FxLoadResult<Parent, AttachSourcesScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.HOME_SCREEN);

            Parent root = screenData.node;

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, currentStage.getScene().getWidth(), currentStage.getScene().getHeight());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela inicial: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
