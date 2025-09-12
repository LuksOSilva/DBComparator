package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.persistence.temp.TempComparedTablesDAO;
import com.luksosilva.dbcomparator.persistence.temp.TempTableComparisonResultDAO;

public class TableComparisonResultService {


    public static void clearTempTables() throws Exception {
        try {

            TempTableComparisonResultDAO.clearTables();

        } catch (Exception e) {
            throw new Exception("Não foi possível limpar as tabelas temporárias: " + e);
        }
    }
}
