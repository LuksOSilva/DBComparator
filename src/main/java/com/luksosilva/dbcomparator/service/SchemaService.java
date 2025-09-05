package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.persistence.ColumnSettingsDAO;
import com.luksosilva.dbcomparator.persistence.SchemaLoader;
import com.luksosilva.dbcomparator.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchemaService {


    public static void mapComparedSources (List<ComparedSource> comparedSourceList, ConfigRegistry configRegistry) {
        for (ComparedSource comparedSource : comparedSourceList) {
            SchemaLoader.mapSourceTable(comparedSource, configRegistry);
        }
    }

    public static void loadColumnsSettings(List<ComparedTable> comparedTableList,
                                           List<ComparedSource> comparedSourceList,
                                           boolean loadFromDb) {

        Optional<Map<ComparedTable, Map<ComparedTableColumn, ColumnSettings>>> optionalPerComparedTableColumnSetting =
                loadFromDb ? ColumnSettingsDAO.loadTableColumnsSettingsFromDb(comparedTableList)
                        : Optional.empty();

        for (ComparedTable comparedTable : comparedTableList) {
            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {

                comparedTableColumn.setColumnSetting(ColumnSettingsService.getColumnSettings
                        (comparedTable, comparedTableColumn, comparedSourceList, optionalPerComparedTableColumnSetting));

            }
        }
    }

    public static void saveColumnSettings(List<ComparedTable> comparedTableList) {
        for (ComparedTable comparedTable : comparedTableList) {
            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {

                ColumnSettingsDAO.saveTableColumnsSettings(comparedTable, comparedTableColumn);

            }
        }
    }

    public static List<String> validateIdentifiers(ComparedTable comparedTable,
                                                   Map<ComparedTableColumn, ColumnSettings> perComparedTableColumnSettings,
                                                   List<ComparedSource> comparedSourceList) {


        List<String> identifiersComparedColumns = perComparedTableColumnSettings.keySet().stream()
                .filter(comparedTableColumn -> perComparedTableColumnSettings.get(comparedTableColumn).isIdentifier())
                .map(ComparedTableColumn::getColumnName)
                .toList();

        List<String> invalidInSources = new ArrayList<>();

        for (ComparedSource comparedSource : comparedSourceList) {

            invalidInSources.addAll(
                    ColumnSettingsDAO.selectValidateColumnSettings(comparedSource.getSourceId(),
                            FileUtils.getCanonicalPath(comparedSource.getSource().getPath()),
                            comparedTable.getTableName(), identifiersComparedColumns));

        }

        return invalidInSources;

    }

}
