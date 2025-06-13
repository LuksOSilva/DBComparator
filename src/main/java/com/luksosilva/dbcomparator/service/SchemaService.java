package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumnSettings;
import com.luksosilva.dbcomparator.repository.SchemaRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchemaService {


    public static void mapComparedSources (List<ComparedSource> comparedSourceList) {
        for (ComparedSource comparedSource : comparedSourceList) {
            SchemaRepository.mapSourceTable(comparedSource);
        }
    }

    public static void loadColumnsSettings(List<ComparedTable> comparedTableList, List<ComparedSource> comparedSourceList) {

        Optional<Map<ComparedTable, Map<ComparedTableColumn, ComparedTableColumnSettings>>> optionalPerComparedTableColumnSetting =
                SchemaRepository.loadTableColumnsSettingsFromDb(comparedTableList);

        for (ComparedTable comparedTable : comparedTableList) {

            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {

                comparedTableColumn.setColumnSetting(ColumnSettingsService.getColumnSettings
                        (comparedTable, comparedTableColumn,
                         comparedSourceList, optionalPerComparedTableColumnSetting));

            }
        }
    }

    public static void saveColumnSettings(List<ComparedTable> comparedTableList) {
        for (ComparedTable comparedTable : comparedTableList) {
            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {

                SchemaRepository.saveTableColumnsSettings(comparedTable, comparedTableColumn);

            }
        }
    }

}
