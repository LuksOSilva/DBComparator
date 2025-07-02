package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.controller.dialogs.ColumnSettingsValidationDialogController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DialogUtils {


    public static void showWarning(String headerText, String contentText) {

        Alert alert = new Alert(Alert.AlertType.WARNING); // Or WARNING, or ERROR
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



    public static void showInvalidColumnSettingsDialog(Stage ownerStage, List<ComparedTable> invalidTables) {
        try {
            FxLoadResult<Stage, ColumnSettingsValidationDialogController> loadResult =
                    FxmlUtils.createNewStage(ownerStage, FxmlFiles.COLUMN_SETTINGS_VALIDATION_DIALOG, "Validação de Configurações");

            loadResult.controller.initializeDialog(invalidTables);
            loadResult.controller.setStage(loadResult.node);
            loadResult.node.setResizable(false);
            loadResult.node.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtils.showError("Erro", "Não foi possível abrir o diálogo de validação.");
        }
    }
}
