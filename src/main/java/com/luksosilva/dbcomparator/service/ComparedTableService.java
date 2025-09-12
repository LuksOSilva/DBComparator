package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.persistence.temp.TempComparedTablesDAO;
import com.luksosilva.dbcomparator.persistence.temp.TempSourcesDAO;

import java.util.List;

public class ComparedTableService {

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

    public static void clearTempTables() throws Exception {
        try {

            TempComparedTablesDAO.clearTables();

        } catch (Exception e) {
            throw new Exception("Não foi possível limpar as tabelas temporárias: " + e);
        }
    }


}
