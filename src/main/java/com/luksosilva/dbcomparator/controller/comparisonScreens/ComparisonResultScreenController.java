package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.model.comparison.result.TableComparisonResult;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.viewmodel.comparison.compared.ComparedTableViewModel;
import com.luksosilva.dbcomparator.viewmodel.comparison.result.*;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class ComparisonResultScreenController {


    private Stage currentStage;
    public void setCurrentStage(Stage currentStage) { this.currentStage = currentStage; }
    private List<Stage> openedStages = new ArrayList<>();

    private Comparison comparison;
    private final ComparisonResult comparisonResult = new ComparisonResult();


    private final List<ComparedTableViewModel> comparedTableViewModels = new ArrayList<>();
    private final List<TableComparisonResultViewModel> tableComparisonResultViewModels = new ArrayList<>();

    private final Map<ComparedTable, TitledPane> perComparedTableResultPane = new HashMap<>();

    private final ObservableList<TitledPane> tableComparisonResultPanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTableComparisonResultPanes;


    public void setComparison(Comparison comparison) { this.comparison = comparison; }
    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }




    @FXML
    public Accordion tablesAccordion;

    public void init() {
        setupComparedTableViewModels();
        setupComparedTablesAccordion();
        constructComparedTableAccordion();
        startComparison();
    }

    private void startComparison() {

        List<ComparedTable> sortedComparedTableList = getSortedComparedTableList(comparison.getComparedTables());

        for (ComparedTable comparedTable : sortedComparedTableList) {
            ComparisonService.compare(comparedTable, this);
        }
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


            titledPane.setDisable(true);
            tableComparisonResultPanes.add(titledPane);
            perComparedTableResultPane.put(vm.getModel(), titledPane);
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

}
