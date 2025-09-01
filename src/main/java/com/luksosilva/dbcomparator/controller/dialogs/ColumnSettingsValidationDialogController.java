package com.luksosilva.dbcomparator.controller.dialogs;

import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ColumnSettingsValidationDialogController {

    @FXML
    public Label title;
    @FXML
    private VBox messageContainer;

    private Stage stage;

    public void initializeColumnSettingsValidationDialog(List<ComparedTable> invalidTables) {

        title.setText("Validação de Configurações");

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

    public void initializeFiltersValidationDialog(List<ComparedTable> invalidTables) {

        title.setText("Validação de Filtros");

        for (FilterValidationResultType filterValidationResultType : FilterValidationResultType.values()) {
            if (filterValidationResultType == FilterValidationResultType.VALID) continue;

            List<String> tableNames = invalidTables.stream()
                    .filter(table -> table.getFilterValidationResult().getType() == filterValidationResultType)
                    .map(ComparedTable::getTableName)
                    .toList();

            if (!tableNames.isEmpty()) {
                Label message = new Label(filterValidationResultType.getMessage() + ":");
                message.setWrapText(true);
                message.getStyleClass().add("dialog-section-title");

                Label tables = new Label(String.join("\n", tableNames));
                tables.getStyleClass().add("dialog-table-names");

                Label tip = new Label(filterValidationResultType.getTip());
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