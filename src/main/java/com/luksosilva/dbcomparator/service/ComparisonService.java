package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.builder.ComparisonResultBuilder;
import com.luksosilva.dbcomparator.builder.SelectDifferencesBuilder;
import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import com.luksosilva.dbcomparator.model.comparison.*;
import com.luksosilva.dbcomparator.model.source.Source;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;
import org.apache.commons.io.FilenameUtils;

import java.util.*;


public class ComparisonService {

    //1
    public static void processSources(Comparison comparison, List<Source> sourceList) {
        setComparedSources(comparison, sourceList);
        SchemaService.mapComparedSources(comparison.getComparedSources());
    }

    //2
    public static void processTables(Comparison comparison, Map<String, Map<ComparedSource, SourceTable>> groupedTables) {
        setComparedTables(comparison, groupedTables);
        setComparedTableColumns(comparison.getComparedTables());


        //gets only tables with at least one column without column setting.
        List<ComparedTable> comparedTablesWithoutColumnSettings = comparison.getComparedTables().stream()
                .filter(comparedTable -> comparedTable.getComparedTableColumns()
                        .stream().anyMatch(comparedTableColumn -> !comparedTableColumn.hasColumnSetting())).toList();

        //removes any column setting in the tables gathered.
        comparedTablesWithoutColumnSettings.forEach(comparedTable -> {
            comparedTable.getComparedTableColumns().forEach(ComparedTableColumn::removeColumnSetting);
        });


        setTableColumnsSettings(comparedTablesWithoutColumnSettings, true); //TODO: create config to set default 'loadFromDb'
    }

    //3
    public static void validateColumnSettings(ComparedTable comparedTable,
                                                                        Map<ComparedTableColumn, ComparedTableColumnSettings> perComparedTableColumnSettings) {

        //checks if table was validated before.
        if (comparedTable.isColumnSettingsValid()) {

            for (Map.Entry<ComparedTableColumn, ComparedTableColumnSettings> entry : perComparedTableColumnSettings.entrySet()) {
                ComparedTableColumn comparedTableColumn = entry.getKey();
                ComparedTableColumnSettings newcomparedTableColumnSettings = entry.getValue();
                ComparedTableColumnSettings currentComparedTableColumnSettings = comparedTableColumn.getColumnSetting();

                //checks if anything changed when compared to the last validation.
                if (!currentComparedTableColumnSettings.equals(newcomparedTableColumnSettings)){
                    comparedTable.clearColumnSettingValidation();
                    break;
                }
            }
            //if nothing changed
            if (comparedTable.isColumnSettingsValid()) {
                comparedTable.setColumnSettingsValidationResultType(ColumnSettingsValidationResultType.VALID);
                return;
            }
        }


        boolean hasIdentifier = perComparedTableColumnSettings.values().stream()
                .anyMatch(ComparedTableColumnSettings::isIdentifier);

        if (!hasIdentifier) {
            comparedTable.setColumnSettingsValidationResultType(ColumnSettingsValidationResultType.NO_IDENTIFIER);
            return;
        }


        List<String> invalidInSources = SchemaService.validateIdentifiers(comparedTable, perComparedTableColumnSettings);

        if (!invalidInSources.isEmpty()) {
            comparedTable.setColumnSettingsValidationResultType(ColumnSettingsValidationResultType.AMBIGUOUS_IDENTIFIER);
            return;
        }


        comparedTable.setColumnSettingsValidationResultType(ColumnSettingsValidationResultType.VALID);
    }

    //4
    public static void processColumnSettings(ComparedTable comparedTable,
                                             Map<ComparedTableColumn, ComparedTableColumnSettings> perComparedTableColumnSettings,
                                             boolean saveSettingsAsDefault) {

        perComparedTableColumnSettings.forEach((comparedTableColumn, comparedTableColumnSettings) -> {

            comparedTableColumn.getColumnSetting().changeIsComparableTo(comparedTableColumnSettings.isComparable());
            comparedTableColumn.getColumnSetting().changeIsIdentifierTo(comparedTableColumnSettings.isIdentifier());

        });


        if (saveSettingsAsDefault) {

            List<ComparedTable> tablesToSave = new ArrayList<>();
            tablesToSave.add(comparedTable);

            SchemaService.saveColumnSettings(tablesToSave);
        }
    }


    //5
    public static void processFilters(Map<ComparedTableColumn, List<String>> perComparedTableColumnFilter) {

        perComparedTableColumnFilter.forEach((comparedTableColumn, filter) -> {

            comparedTableColumn.getColumnFilter().addAll(filter);

        });

    }

    //6
    public static void compare(Comparison comparison) {

        buildSelectDifferences(comparison.getComparedTables());

        comparison.setComparisonResult(ComparisonResultBuilder.build(comparison));

//        try {
//
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.enable(SerializationFeature.INDENT_OUTPUT);
//            String json = mapper.writeValueAsString(comparison.getComparisonResult());
//
//            System.out.println(json);
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }


    }


    /// HELPERS

    public static void setTableColumnsSettings(List<ComparedTable> comparedTableList, boolean loadFromDb) {
        SchemaService.loadColumnsSettings(comparedTableList, loadFromDb);
    }


    /// privates

    private static void setComparedSources(Comparison comparison, List<Source> sourceList) {
        for (int i = 0; i < sourceList.size(); i++){

            String sourceName = sourceList.get(i).getPath().getName();
            String sourceId = FilenameUtils.removeExtension(sourceName);

            comparison.getComparedSources().add(new ComparedSource(sourceId, i, sourceList.get(i)));

        }
    }

    private static void setComparedTables(Comparison comparison, Map<String, Map<ComparedSource, SourceTable>> groupedTables) {

        for (Map<ComparedSource, SourceTable> perSourceTable : groupedTables.values()) {
            ComparedTable comparedTable = new ComparedTable(perSourceTable);
            comparison.getComparedTables().add(comparedTable);
        }

    }

    private static void setComparedTableColumns(List<ComparedTable> comparedTableList) {
        for (ComparedTable comparedTable : comparedTableList) {

            if (!comparedTable.getComparedTableColumns().isEmpty()) continue;


            Map<String, Map<ComparedSource, SourceTableColumn>> groupedColumns = new HashMap<>();

            for (Map.Entry<ComparedSource, SourceTable> entry : comparedTable.getPerSourceTable().entrySet()) {
                ComparedSource comparedSource = entry.getKey();
                SourceTable sourceTable = entry.getValue();

                for (SourceTableColumn sourceTableColumn : sourceTable.getSourceTableColumns()) {
                    String columnName = sourceTableColumn.getColumnName();

                    groupedColumns
                            .computeIfAbsent(columnName, k -> new HashMap<>())
                            .put(comparedSource, sourceTableColumn);
                }
            }

            for (Map<ComparedSource, SourceTableColumn> perSourceTableColumn : groupedColumns.values()) {
                ComparedTableColumn comparedTableColumn = new ComparedTableColumn(perSourceTableColumn);
                comparedTable.getComparedTableColumns().add(comparedTableColumn);
            }
        }
    }

    private static void buildSelectDifferences(List<ComparedTable> comparedTableList) {

        for (ComparedTable comparedTable : comparedTableList) {

            String sql = SelectDifferencesBuilder.build(comparedTable);

            comparedTable.setQueryDifferences(sql);

        }

    }

}
