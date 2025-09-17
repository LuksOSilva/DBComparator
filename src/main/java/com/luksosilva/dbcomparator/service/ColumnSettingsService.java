package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import com.luksosilva.dbcomparator.exception.ColumnSettingsException;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnConfig;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.persistence.ColumnSettingsDAO;
import com.luksosilva.dbcomparator.persistence.ColumnSettingsValidator;
import com.luksosilva.dbcomparator.persistence.temp.TempColumnSettingsDAO;
import com.luksosilva.dbcomparator.persistence.temp.TempComparedTablesDAO;

import java.util.*;

public class ColumnSettingsService {

    public static void processColumnSettings(boolean validate, List<ComparedTable> comparedTableList) throws Exception {
        //saves only tables where user loaded the configs (others will have no changes)
        List<ComparedTable> touchedTables = comparedTableList.stream()
                .filter(comparedTable -> !comparedTable.getComparableColumns().isEmpty())
                .filter(comparedTable -> comparedTable.getComparedTableColumns().stream()
                    .noneMatch(comparedTableColumn -> comparedTableColumn.getColumnSetting() == null))
                .toList();

        if (!touchedTables.isEmpty()) {
            // Always save (so edits are not lost, even if invalid)
            saveTempColumnSettings(touchedTables);

            // Reset validation for tables user touched
            touchedTables.forEach(ComparedTable::clearColumnSettingValidation);
            saveColumnSettingValidationResult(touchedTables);
        }

        if (validate) {
            validateColumnSettings(comparedTableList);

            List<ComparedTable> invalidComparedTables = comparedTableList.stream()
                    .filter(ComparedTable::isColumnSettingsInvalid)
                    .toList();

            if (!invalidComparedTables.isEmpty()){
                throw new ColumnSettingsException(invalidComparedTables);
            }
        }
    }

    public static void processComparedTableConfigs(boolean prioritizeUserColumnSettings,
                                                   List<ComparedTable> comparedTables) throws Exception {
        try {

            TempComparedTablesDAO.computeTempComparedColumnConfigs(prioritizeUserColumnSettings, comparedTables);

        } catch (Exception e) {
            throw new Exception("Não foi possível processar as configurações: " + e);

        }
    }

    public static void saveColumnSettingValidationResult(List<ComparedTable> comparedTableList) throws Exception {
        try {

            TempComparedTablesDAO.updateTempComparedTableColumnValidation(comparedTableList);

        } catch (Exception e) {
            throw new Exception("Não foi possível processar as configurações: " + e);

        }
    }

    public static void getConfigOfComparedColumns(List<ComparedTableColumn> comparedTableColumnList) throws Exception {
        try {

            List<ColumnConfig> columnConfigList = loadConfigOfComparedColumns(comparedTableColumnList);

            for (ComparedTableColumn column : comparedTableColumnList) {
                ColumnConfig configOfColumn = columnConfigList.stream()
                        .filter(columnConfig -> columnConfig.getCodComparedColumn() == column.getCodComparedColumn())
                        .findFirst()
                        .orElse(new ColumnConfig(false, false));

                column.setColumnConfig(configOfColumn);
            }

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas comparadas: " + e.getMessage());

        }
    }

    public static List<ColumnConfig> loadConfigOfComparedColumns(List<ComparedTableColumn> comparedTableColumnList) throws Exception {
        return TempColumnSettingsDAO.selectConfigOfColumns(comparedTableColumnList);
    }

    public static void saveColumnSettingsAsDefault(ComparedTable comparedTable) throws Exception {
        try {

            ColumnSettingsDAO.saveTableColumnsSettings(comparedTable);

        } catch (Exception e) {
            throw new Exception("Não foi possível salvar as configurações como padrão: " + e.getMessage());

        }
    }

    public static void saveTempColumnSettings(List<ComparedTable> comparedTableList) throws Exception {
        try {

            TempColumnSettingsDAO.saveTableColumnsSettings(comparedTableList);

        } catch (Exception e) {
            throw new Exception("Não foi possível salvar as configurações como padrão: " + e.getMessage());

        }
    }

    public static void validateColumnSettings(List<ComparedTable> comparedTableList) throws Exception {
        for (ComparedTable comparedTable : comparedTableList) {
            if (comparedTable.isColumnSettingsValid()) continue;

            //for memory saving + only saving what needs later on
            ComparedTable tempComparedTable = new ComparedTable(comparedTable);

            if (tempComparedTable.getComparedTableColumns().isEmpty()) {
                ComparedTableService.getComparedColumnsOfTables(List.of(tempComparedTable));
            }

            if (tempComparedTable.getComparedTableColumns().stream().anyMatch(column -> column.getColumnSetting() == null)) {
                ColumnSettingsService.getConfigOfComparedColumns(tempComparedTable.getComparedTableColumns());
            }

            boolean hasIdentifier = tempComparedTable.getComparedTableColumns().stream()
                    .anyMatch(column -> column.getColumnSetting().isIdentifier());
            if (!hasIdentifier) {
                comparedTable.setColumnSettingsValidationResult(ColumnSettingsValidationResultType.NO_IDENTIFIER);
                continue;
            }

            boolean areIdentifiersValid = validateIdentifiers(tempComparedTable);
            if (!areIdentifiersValid) {
                comparedTable.setColumnSettingsValidationResult(ColumnSettingsValidationResultType.AMBIGUOUS_IDENTIFIER);
                continue;
            }

            comparedTable.setColumnSettingsValidationResult(ColumnSettingsValidationResultType.VALID);
        }

        saveColumnSettingValidationResult(comparedTableList);

    }

    public static boolean validateIdentifiers(ComparedTable comparedTable) throws Exception {

        List<Source> sources = SourceService.getSources();

        for (Source source : sources) {

            boolean isValid = ColumnSettingsValidator.selectValidateColumnSettings(source, comparedTable);

            if (!isValid) return false;
        }

        return true;
    }

    public static void clearTempTables() throws Exception {
        try {

            TempColumnSettingsDAO.clearTables();

        } catch (Exception e) {
            throw new Exception("Não foi possível limpar as tabelas temporárias: " + e);
        }
    }



}
