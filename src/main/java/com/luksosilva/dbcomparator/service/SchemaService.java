package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnConfig;
import com.luksosilva.dbcomparator.persistence.ColumnSettingsDAO;
import com.luksosilva.dbcomparator.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchemaService {


//    public static void loadColumnsSettings(List<ComparedTable> comparedTableList,
//                                           List<ComparedSource> comparedSourceList,
//                                           boolean loadFromDb) {
//
//        Optional<Map<ComparedTable, Map<ComparedTableColumn, ColumnConfig>>> optionalPerComparedTableColumnSetting =
//                loadFromDb ? ColumnSettingsDAO.loadTableColumnsSettingsFromDb(comparedTableList)
//                        : Optional.empty();
//
//        for (ComparedTable comparedTable : comparedTableList) {
//            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {
//
//                comparedTableColumn.setColumnConfig(ColumnSettingsService.getColumnSettings
//                        (comparedTable, comparedTableColumn, comparedSourceList, optionalPerComparedTableColumnSetting));
//
//            }
//        }
//    }

//    public static void saveColumnSettings(List<ComparedTable> comparedTableList) {
//        for (ComparedTable comparedTable : comparedTableList) {
//            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {
//
//                ColumnSettingsDAO.saveTableColumnsSettings(comparedTable, comparedTableColumn);
//
//            }
//        }
//    }



}
