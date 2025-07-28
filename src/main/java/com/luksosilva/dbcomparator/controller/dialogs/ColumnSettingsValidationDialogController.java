package com.luksosilva.dbcomparator.controller.dialogs;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ColumnSettingsValidationDialogController {

    @FXML
    private VBox messageContainer;

    private Stage stage;

    public void initializeDialog(List<ComparedTable> invalidTables) {
        for (ColumnSettingsValidationResultType type : ColumnSettingsValidationResultType.values()) {
            if (type == ColumnSettingsValidationResultType.VALID) continue;

            List<String> tableNames = invalidTables.stream()
                    .filter(table -> table.getColumnSettingsValidationResult() == type)
                    .map(ComparedTable::getTableName)
                    .toList();

            if (!tableNames.isEmpty()) {
                Label message = new Label(type.getMessage() + ":");
                message.setWrapText(true);
                message.getStyleClass().add("dialog-section-title");

                Label tables = new Label(String.join("\n", tableNames));
                tables.getStyleClass().add("dialog-table-names");

                Label tip = new Label(type.getTip());
                tip.setWrapText(true);
                tip.getStyleClass().add("dialog-tip");

                messageContainer.getChildren().addAll(message, tables, tip, new Separator());
            }
        }
    }

    @FXML
    private void onOkClicked() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}