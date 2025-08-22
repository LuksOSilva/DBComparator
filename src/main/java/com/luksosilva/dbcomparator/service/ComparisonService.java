package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.builder.ComparisonResultBuilder;
import com.luksosilva.dbcomparator.builder.FilterSqlBuilder;
import com.luksosilva.dbcomparator.builder.SelectDifferencesBuilder;
import com.luksosilva.dbcomparator.controller.comparisonScreens.ComparisonResultScreenController;
import com.luksosilva.dbcomparator.model.comparison.*;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.model.comparison.result.TableComparisonResult;
import com.luksosilva.dbcomparator.model.source.Source;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;
import javafx.concurrent.Task;
import org.apache.commons.io.FilenameUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ComparisonService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

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

    //4
    public static void processColumnSettings(ComparedTable comparedTable,
                                             Map<ComparedTableColumn, ColumnSettings> perComparedTableColumnSettings,
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
    public static void processFilters(List<ComparedTable> comparedTableList) {

        for (ComparedTable comparedTable : comparedTableList) {
            String filterSql = FilterSqlBuilder.build(comparedTable);

            comparedTable.setSqlUserFilter(filterSql);
        }

    }

    //6
    public static void compare(ComparedTable comparedTable, ComparisonResultScreenController controller) {

        Task<TableComparisonResult> compareTableTask = new Task<>() {
            @Override
            protected TableComparisonResult call() throws Exception {
                comparedTable.setSqlSelectDifferences(SelectDifferencesBuilder.build(comparedTable));
                return ComparisonResultBuilder.buildTableComparisonResult(comparedTable);
            }
        };

        compareTableTask.setOnSucceeded(event -> {
            try {
                TableComparisonResult tableComparisonResult = compareTableTask.getValue();

                comparedTable.setComparisonFailed(false);
                controller.getComparisonResult().addTableComparisonResult(tableComparisonResult);

                // updates UI
                controller.setupViewModel(tableComparisonResult);
                controller.constructComparedTableAccordion();


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        compareTableTask.setOnFailed(event -> {
            Throwable ex = compareTableTask.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
            comparedTable.setComparisonFailed(true);
        });

        executor.submit(compareTableTask);
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
                ComparedTableColumn comparedTableColumn = new ComparedTableColumn(comparedTable, perSourceTableColumn);
                comparedTable.getComparedTableColumns().add(comparedTableColumn);
            }
        }
    }

    private static void buildSelectDifferences(List<ComparedTable> comparedTableList) {

        for (ComparedTable comparedTable : comparedTableList) {

            String sql = SelectDifferencesBuilder.build(comparedTable);
            comparedTable.setSqlSelectDifferences(sql);

        }

    }

}
