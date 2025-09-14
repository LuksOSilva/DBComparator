package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.persistence.temp.TempComparedTablesDAO;

import java.util.List;

public class ComparedTableService {

    public static void processTables(List<ComparedTable> selectedComparedTables) throws Exception {
        try {
            List<String> comparedTablesOnDb = getComparedTablesNames();
            List<String> selectedComparedTablesNames = selectedComparedTables.stream()
                    .map(ComparedTable::getTableName).toList();

            List<String> toAddTableNames = selectedComparedTablesNames.stream()
                    .filter(toAdd -> !comparedTablesOnDb.contains(toAdd))
                    .toList();
            List<String> toRemoveTableNames = comparedTablesOnDb.stream()
                    .filter(toAdd -> !selectedComparedTablesNames.contains(toAdd))
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
                //generates compared table columns
                getComparedColumnsFromSources(toAddComparedTables);
                //saves compared table columns
                TempComparedTablesDAO.saveTempComparedColumns(toAddComparedTables);
            }


        } catch (Exception e) {
            throw new Exception("Não foi possível processar as tabelas selecionadas: " + e.getMessage());
        }
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

    public static void getComparedColumnsFromSources(List<ComparedTable> comparedTables) throws Exception {
        try {

            List<ComparedTableColumn> comparedTableColumns =
                    TempComparedTablesDAO.selectComparedColumnsFromSources();

            for (ComparedTable comparedTable : comparedTables) {
                List<ComparedTableColumn> columnsOfTable = comparedTableColumns.stream()
                        .filter(column -> column.getCodComparedTable() == comparedTable.getCodComparedTable())
                        .toList();

                comparedTable.getComparedTableColumns().addAll(columnsOfTable);

            }

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas selecionadas: " + e);

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
