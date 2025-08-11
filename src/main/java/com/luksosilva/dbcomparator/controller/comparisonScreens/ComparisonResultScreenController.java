package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.viewmodel.comparison.result.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResultScreenController {


    private Stage currentStage;
    public void setCurrentStage(Stage currentStage) { this.currentStage = currentStage; }

    private Comparison comparison;
    private ComparisonResult comparisonResult;
    private ComparisonResultViewModel comparisonResultViewModel;
    public void setComparison(Comparison comparison) { this.comparison = comparison; }

    private final ObservableList<TitledPane> tableComparisonResultPanes = FXCollections.observableArrayList();
    private FilteredList<TitledPane> filteredTableComparisonResultPanes;

    private List<Stage> openedStages = new ArrayList<>();


    @FXML
    public Accordion tablesAccordion;

    public void init() {
        comparisonResult = comparison.getComparisonResult();
        setupViewModel();
        setupComparedTablesAccordion();
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

    private void setupViewModel() {
        comparisonResultViewModel = new ComparisonResultViewModel(comparisonResult);
    }

    private void setupComparedTablesAccordion() {
        tablesAccordion.getPanes().clear(); // Clears Accordion
        tableComparisonResultPanes.clear();             // Clears master list

        // Populate allTablePanes with all TitledPanes
        tableComparisonResultPanes.addAll(constructComparedTableAccordion());

        // Initialize FilteredList based on allTablePanes
        filteredTableComparisonResultPanes = new FilteredList<>(tableComparisonResultPanes, pane -> true);

        // Set the Accordion's panes to the filtered list.
        tablesAccordion.getPanes().setAll(filteredTableComparisonResultPanes);
    }



    /// Constructors

    private List<TitledPane> constructComparedTableAccordion() {
        List<TitledPane> titledPaneList = new ArrayList<>();

        List<TableComparisonResultViewModel> tableComparisonResultViewModels =
                comparisonResultViewModel.getTableComparisonResultViewModels();

        for (TableComparisonResultViewModel tableComparisonResultViewModel : tableComparisonResultViewModels) {
            TitledPane titledPane = new TitledPane();

            String tableName = tableComparisonResultViewModel.getTableName();
            titledPane.setText(tableName);


            titledPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && titledPane.getUserData() == null) {
                    constructComparedTableAccordionContent(titledPane, tableComparisonResultViewModel);

                }
            });

            titledPaneList.add(titledPane);
        }
        return titledPaneList;
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

        contentButtons.add(showComparisonResultButton);
        contentButtons.add(showSchemaComparisonButton);

        return contentButtons;
    }

}
