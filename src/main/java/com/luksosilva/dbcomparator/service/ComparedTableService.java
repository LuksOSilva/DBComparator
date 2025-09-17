package com.luksosilva.dbcomparator.service;


import com.luksosilva.dbcomparator.enums.ConfigKeys;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.persistence.temp.TempComparedTablesDAO;

import java.util.List;

public class ComparedTableService {

    public static void processTables(ConfigRegistry configRegistry,
                                     List<ComparedTable> selectedComparedTables) throws Exception {
        try {
            List<String> comparedTablesOnDb = getComparedTablesNames();
            List<String> selectedComparedTablesNames = selectedComparedTables.stream()
                    .map(ComparedTable::getTableName).toList();

            List<String> toAddTableNames = selectedComparedTablesNames.stream()
                    .filter(toAdd -> !comparedTablesOnDb.contains(toAdd))
                    .toList();
            List<String> toRemoveTableNames = comparedTablesOnDb.stream()
                    .filter(toRemove -> !selectedComparedTablesNames.contains(toRemove))
                    .toList();

            List<ComparedTable> toAddComparedTables = selectedComparedTables.stream()
                    .filter(comparedTable -> toAddTableNames.contains(comparedTable.getTableName()))
                    .toList();

            if (!toRemoveTableNames.isEmpty()) {
                TempComparedTablesDAO.deleteTempComparedTables(toRemoveTableNames);
            }
            if (!toAddComparedTables.isEmpty()) {
                //saves compared tables
                TempComparedTablesDAO.saveTempComparedTables(toAddComparedTables);

                //generates columns
                processComparedColumns(toAddComparedTables);
                //generates configs
                boolean prioritizeUserColumnSettings =
                        configRegistry.getConfigValueOf(ConfigKeys.DBC_PRIORITIZE_USER_COLUMN_SETTINGS);
                ColumnSettingsService.processComparedTableConfigs(prioritizeUserColumnSettings, toAddComparedTables);
            }

        } catch (Exception e) {
            throw new Exception("Não foi possível processar as tabelas selecionadas: " + e.getMessage());
        }
    }



    public static List<ComparedTable> getComparedTables() throws Exception {
        try {

            return TempComparedTablesDAO.selectComparedTables();

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas comparadas: " + e.getMessage());

        }
    }

    public static void getComparedColumnsOfTables(List<ComparedTable> comparedTables) throws Exception {
        try {

            List<ComparedTableColumn> comparedTableColumns = loadComparedColumnsOfTables(comparedTables);

            for (ComparedTable comparedTable : comparedTables) {
                List<ComparedTableColumn> columnsOfTable = comparedTableColumns.stream()
                        .filter(column -> column.getCodComparedTable() == comparedTable.getCodComparedTable())
                        .toList();

                comparedTable.setComparedTableColumns(columnsOfTable);
            }

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas comparadas: " + e.getMessage());

        }
    }

    public static List<ComparedTableColumn> loadComparedColumnsOfTables(List<ComparedTable> comparedTables) throws Exception {
        return TempComparedTablesDAO.selectComparedColumnsOfTables(comparedTables);
    }



    public static List<String> getComparedTablesNames() throws Exception {
        try {

            return TempComparedTablesDAO.selectComparedTablesNames();

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas selecionadas: " + e);

        }
    }

    public static List<ComparedTable> getComparedTablesFromSources() throws Exception {
        try {

            return TempComparedTablesDAO.selectComparedTableFromSources();

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas selecionadas: " + e);

        }
    }

    public static void processComparedColumns(List<ComparedTable> comparedTables) throws Exception {
        try {

            TempComparedTablesDAO.computeComparedColumns(comparedTables);


        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas selecionadas: " + e.getMessage());

        }
    }

    public static void clearTempTables() throws Exception {
        try {

            TempComparedTablesDAO.clearTables();

        } catch (Exception e) {
            throw new Exception("Não foi possível limpar as tabelas temporárias: " + e);
        }
    }

    /// HELPERS



}
