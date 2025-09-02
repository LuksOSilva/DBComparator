package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.controller.dialogs.AddFilterDialogController;
import com.luksosilva.dbcomparator.controller.dialogs.ColumnSettingsValidationDialogController;
import com.luksosilva.dbcomparator.controller.comparisonScreens.TableComparisonResultScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.customization.Filter;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.result.TableComparisonResultViewModel;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DialogUtils {

    public static void showInfo(Stage ownerStage, String headerText, String contentText) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Atenção");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        showInCenter(ownerStage, alert);

    }

    public static void showWarning(Stage ownerStage, String headerText, String contentText) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        showInCenter(ownerStage, alert);

    }

    public static void showError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static void showError(Stage ownerStage, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        showInCenter(ownerStage, alert);
    }

    public static boolean askConfirmation(Stage ownerStage, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(ownerStage);
        alert.setTitle("Confirmar");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        ButtonType buttonYes = new ButtonType("Sim");
        ButtonType buttonNo = new ButtonType("Não");
        alert.getButtonTypes().setAll(buttonYes, buttonNo);


        alert.setOnShown(ev -> {
            Window window = alert.getDialogPane().getScene().getWindow();
            window.setX(ownerStage.getX() + (ownerStage.getWidth() - window.getWidth()) / 2);
            window.setY(ownerStage.getY() + (ownerStage.getHeight() - window.getHeight()) / 2);
        });


        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == buttonYes;
    }

    public static List<Filter> showAddFilterDialog(Stage ownerStage, List<ComparedTable> comparedTableList) {
        try {
            FxLoadResult<Stage, AddFilterDialogController> loadResult =
                    FxmlUtils.createNewStage(FxmlFiles.ADD_FILTER_DIALOG, Modality.APPLICATION_MODAL, ownerStage, "Adicionar Filtros");

            loadResult.controller.initializeAddDialog(comparedTableList);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.setResizable(false);
            showInCenter(ownerStage, loadResult.node);


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
            showInCenter(ownerStage, loadResult.node);


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
            showInCenter(ownerStage, loadResult.node);


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
            showInCenter(ownerStage, loadResult.node);

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

            loadResult.node.setMinHeight(600.0);
            loadResult.node.setMinWidth(850.0);
            loadResult.node.show();

            showInCenter(ownerStage, loadResult.node);

            return loadResult.node;

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir a tela.");
        }

        return null;
    }

    public static void showInCenter(Stage ownerStage, Stage newStage) {
        newStage.show();


        // Get caller's position and size
        double ownerX = ownerStage.getX();
        double ownerY = ownerStage.getY();
        double ownerWidth = ownerStage.getWidth();
        double ownerHeight = ownerStage.getHeight();

        // Get new stage size
        double stageWidth = newStage.getWidth();
        double stageHeight = newStage.getHeight();

        // Center new stage over owner
        newStage.setX(ownerX + (ownerWidth - stageWidth) / 2);
        newStage.setY(ownerY + (ownerHeight - stageHeight) / 2);
    }

    public static void showInCenter(Stage ownerStage, Alert alert) {
        alert.show();

        // Get caller's position and size
        double ownerX = ownerStage.getX();
        double ownerY = ownerStage.getY();
        double ownerWidth = ownerStage.getWidth();
        double ownerHeight = ownerStage.getHeight();

        // Get new stage size
        double alertWidth = alert.getWidth();
        double alertHeight = alert.getHeight();

        // Center new stage over owner
        alert.setX(ownerX + (ownerWidth - alertWidth) / 2);
        alert.setY(ownerY + (ownerHeight - alertHeight) / 2);
    }
}
