package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.model.comparison.result.TableComparisonResult;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.viewmodel.comparison.result.*;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ComparisonResultScreenController {


    private Stage currentStage;
    public void setCurrentStage(Stage currentStage) { this.currentStage = currentStage; }

    private Comparison comparison;
    private final ComparisonResult comparisonResult = new ComparisonResult();

    public void setComparison(Comparison comparison) { this.comparison = comparison; }

    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }

    private List<TableComparisonResultViewModel> tableComparisonResultViewModels = new ArrayList<>();

    private final ObservableList<TitledPane> tableComparisonResultPanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTableComparisonResultPanes;

    private List<Stage> openedStages = new ArrayList<>();


    @FXML
    public Accordion tablesAccordion;

    public void init() {
        setupComparedTablesAccordion();

        startComparison();
    }

    private void startComparison() {

        List<ComparedTable> sortedComparedTableList = new ArrayList<>(comparison.getComparedTables());

        sortedComparedTableList.sort(Comparator.comparingInt(ComparedTable::getTotalRecordCount));

        for (ComparedTable comparedTable : sortedComparedTableList) {
            ComparisonService.compare(comparedTable, this);
        }
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

    public void setupViewModel(TableComparisonResult tableComparisonResult) {
        tableComparisonResultViewModels.add(new TableComparisonResultViewModel(tableComparisonResult));
    }


    private void setupComparedTablesAccordion() {
        tablesAccordion.getPanes().clear();
        tableComparisonResultPanes.clear();

        filteredTableComparisonResultPanes = new FilteredList<>(tableComparisonResultPanes, pane -> true);


        Bindings.bindContent(tablesAccordion.getPanes(), filteredTableComparisonResultPanes);
    }

    /// tablesAccordion.getPanes().setAll(filteredFilterPanes);

    /// Constructors

    public void constructComparedTableAccordion() {
        Set<String> constructedNames = tableComparisonResultPanes.stream()
                .map(Labeled::getText)
                .collect(Collectors.toSet());

        List<TableComparisonResultViewModel> notConstructedTableComparisonResultVM =
                tableComparisonResultViewModels.stream()
                        .filter(vm -> !constructedNames.contains(vm.getTableName()))
                        .toList();

        if (notConstructedTableComparisonResultVM.isEmpty()) return;

        for (TableComparisonResultViewModel vm : notConstructedTableComparisonResultVM) {
            TitledPane titledPane = new TitledPane();
            titledPane.setText(vm.getTableName());

            titledPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && titledPane.getUserData() == null) {
                    constructComparedTableAccordionContent(titledPane, vm);
                }
            });


            tableComparisonResultPanes.add(titledPane);
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

}
