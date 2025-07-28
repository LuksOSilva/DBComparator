package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ColumnSettingsService {

    public static void validateColumnSettings(ComparedTable comparedTable,
                                              Map<ComparedTableColumn, ColumnSettings> perComparedTableColumnSettings) {

        //checks if table was validated before.
        if (comparedTable.isColumnSettingsValid()) {

            for (Map.Entry<ComparedTableColumn, ColumnSettings> entry : perComparedTableColumnSettings.entrySet()) {
                ComparedTableColumn comparedTableColumn = entry.getKey();
                ColumnSettings newcomparedTableColumnSettings = entry.getValue();
                ColumnSettings currentColumnSettings = comparedTableColumn.getColumnSetting();

                //checks if anything changed when compared to the last validation.
                if (!currentColumnSettings.equals(newcomparedTableColumnSettings)){
                    comparedTable.clearColumnSettingValidation();
                    break;
                }
            }
            //no changes since last validation.
            if (comparedTable.isColumnSettingsValid()) {
                return;
            }
        }


        boolean hasIdentifier = perComparedTableColumnSettings.values().stream()
                .anyMatch(ColumnSettings::isIdentifier);

        if (!hasIdentifier) {
            comparedTable.setColumnSettingsValidationResult(ColumnSettingsValidationResultType.NO_IDENTIFIER);
            return;
        }


        List<String> invalidInSources = SchemaService.validateIdentifiers(comparedTable, perComparedTableColumnSettings);

        if (!invalidInSources.isEmpty()) {
            comparedTable.setColumnSettingsValidationResult(ColumnSettingsValidationResultType.AMBIGUOUS_IDENTIFIER);
            return;
        }


        comparedTable.setColumnSettingsValidationResult(ColumnSettingsValidationResultType.VALID);
    }

    public static ColumnSettings getColumnSettings
            (ComparedTable comparedTable,
             ComparedTableColumn comparedTableColumn,
             Optional<Map<ComparedTable, Map<ComparedTableColumn, ColumnSettings>>> optionalPerComparedTableColumnSetting) {


        List<ComparedSource> comparedSourceList = new ArrayList<>();
        comparedTable.getPerSourceTable().forEach((comparedSource, sourceTable) ->
                comparedSourceList.add(comparedSource));

        boolean existsInAllSources = getExistsInAllSources(comparedTableColumn, comparedSourceList);
        //1. If column doesn't exist in all sources, it is neither identifier nor comparable.
        if (!existsInAllSources) {
            return new ColumnSettings(false, false);
        }

        boolean tableHasPrimaryKey = comparedTable.getPerSourceTable().values().stream()
                .flatMap(sourceTable -> sourceTable.getSourceTableColumns().stream())
                .anyMatch(SourceTableColumn::isPk);

        boolean isPkInAnySource = getIsPkInAnySource(comparedTableColumn);
        boolean isPkInAllSources = getIsPkInAllSources(comparedTableColumn);



        return optionalPerComparedTableColumnSetting
                .map(map -> map.getOrDefault(comparedTable, Map.of()).get(comparedTableColumn))
                .orElseGet(() -> getDefaultColumnSettings(
                        tableHasPrimaryKey, isPkInAnySource, isPkInAllSources
                ));
    }


    //

    private static ColumnSettings getDefaultColumnSettings(boolean tableHasPrimaryKey, boolean isPkInAnySource, boolean isPkInAllSources) {
        boolean isIdentifier;
        boolean isComparable;

        //2. If table doesn't have any PK in any sources, all columns are identifiers.
        if (!tableHasPrimaryKey) {
            isIdentifier = true;
            isComparable = false;
        }
        //3. If column is PK in at least 1 source and not in the others, it is an identifier.
        else if (isPkInAnySource && !isPkInAllSources) {
            isIdentifier = true;
            isComparable = false;
        }
        //4. If column exists in all sources and table has primary keys, identifier if its PK else comparable.
        else {
            isIdentifier = isPkInAnySource;
            isComparable = !isPkInAnySource;
        }


        return new ColumnSettings(isComparable, isIdentifier);
    }



    private static boolean getExistsInAllSources(ComparedTableColumn comparedTableColumn, List<ComparedSource> comparedSourceList) {
        return comparedTableColumn.getPerSourceTableColumn().size() == comparedSourceList.size();
    }

    private static boolean getIsPkInAnySource(ComparedTableColumn comparedTableColumn) {
        List<SourceTableColumn> sourceTableColumnList =
                comparedTableColumn.getPerSourceTableColumn().values().stream().toList();

        return sourceTableColumnList.stream().anyMatch(SourceTableColumn::isPk);
    }

    private static boolean getIsPkInAllSources(ComparedTableColumn comparedTableColumn) {
        List<SourceTableColumn> sourceTableColumnList =
                comparedTableColumn.getPerSourceTableColumn().values().stream().toList();

        return sourceTableColumnList.stream().allMatch(SourceTableColumn::isPk);
    }


}
