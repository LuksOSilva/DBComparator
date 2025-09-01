package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.controller.dialogs.AddFilterDialogController;
import com.luksosilva.dbcomparator.controller.dialogs.ColumnSettingsValidationDialogController;
import com.luksosilva.dbcomparator.controller.comparisonScreens.TableComparisonResultScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.customization.Filter;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.viewmodel.comparison.result.TableComparisonResultViewModel;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DialogUtils {

    public static void showInfo(String headerText, String contentText) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Atenção");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();

    }

    public static void showWarning(String headerText, String contentText) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();

    }

    public static void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static boolean askConfirmation(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        ButtonType buttonTypeYes = new ButtonType("Sim");
        ButtonType buttonTypeNo = new ButtonType("Não");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == buttonTypeYes;
    }

    public static List<Filter> showAddFilterDialog(Stage ownerStage, List<ComparedTable> comparedTableList) {
        try {
            FxLoadResult<Stage, AddFilterDialogController> loadResult =
                    FxmlUtils.createNewStage(FxmlFiles.ADD_FILTER_DIALOG, Modality.APPLICATION_MODAL, ownerStage, "Adicionar Filtros");

            loadResult.controller.initializeAddDialog(comparedTableList);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.setResizable(false);
            loadResult.node.showAndWait();


            return loadResult.controller.getAddedFilters();


        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir o diálogo.");
            return null;
        }
    }
    public static Map<Filter, Filter> showEditDefaultFilterDialog(Stage ownerStage,
                                                                  List<ComparedTable> comparedTableList,
                                                                  ColumnFilter columnFilter) {
        try {
            FxLoadResult<Stage, AddFilterDialogController> loadResult =
                    FxmlUtils.createNewStage(FxmlFiles.ADD_FILTER_DIALOG, Modality.APPLICATION_MODAL, ownerStage, "Adicionar Filtros");


            loadResult.controller.initializeEditDefaultFilterDialog(comparedTableList, columnFilter);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.setResizable(false);
            loadResult.node.showAndWait();


            return loadResult.controller.getEditedFilters();


        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir o diálogo.");
            return null;
        }
    }

    public static Map<Filter, Filter> showEditAdvancedFilterDialog(Stage ownerStage,
                                                                  ComparedTable comparedTable) {
        try {
            FxLoadResult<Stage, AddFilterDialogController> loadResult =
                    FxmlUtils.createNewStage(FxmlFiles.ADD_FILTER_DIALOG, Modality.APPLICATION_MODAL, ownerStage, "Adicionar Filtros");


            loadResult.controller.initializeEditAdvancedFilterDialog(comparedTable);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.setResizable(false);
            loadResult.node.showAndWait();


            return loadResult.controller.getEditedFilters();


        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir o diálogo.");
            return null;
        }
    }

    public static void showInvalidColumnSettingsDialog(Stage ownerStage, List<ComparedTable> invalidTables) {
        try {
            FxLoadResult<Stage, ColumnSettingsValidationDialogController> loadResult =
                    FxmlUtils.createNewStage(FxmlFiles.COLUMN_SETTINGS_VALIDATION_DIALOG, Modality.APPLICATION_MODAL, ownerStage,  "Validação de Configurações");

            loadResult.controller.initializeColumnSettingsValidationDialog(invalidTables);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.setResizable(false);
            loadResult.node.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir o diálogo.");
        }
    }

    public static void showInvalidFiltersDialog(Stage ownerStage, List<ComparedTable> invalidTables) {
        try {
            FxLoadResult<Stage, ColumnSettingsValidationDialogController> loadResult =
                    FxmlUtils.createNewStage(FxmlFiles.COLUMN_SETTINGS_VALIDATION_DIALOG, Modality.APPLICATION_MODAL, ownerStage, "Validação de Filtros");

            loadResult.controller.initializeFiltersValidationDialog(invalidTables);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.setResizable(false);
            loadResult.node.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir o diálogo.");
        }
    }

    public static Stage showTableComparisonResultScreen(Stage ownerStage, TableComparisonResultViewModel tableComparisonResultViewModel) {
        try {
            FxLoadResult<Stage, TableComparisonResultScreenController> loadResult =
                    FxmlUtils.createNewStage(FxmlFiles.TABLE_COMPARISON_RESULT_SCREEN, Modality.NONE, ownerStage, "Resultado comparação");

            loadResult.controller.init(tableComparisonResultViewModel);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.show();

            return loadResult.node;

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir a tela.");
        }

        return null;
    }
}
