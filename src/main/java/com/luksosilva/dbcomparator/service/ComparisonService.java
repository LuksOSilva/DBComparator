package com.luksosilva.dbcomparator.service;


import com.luksosilva.dbcomparator.enums.ConfigKeys;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import com.luksosilva.dbcomparator.model.persistence.SavedComparison;
import com.luksosilva.dbcomparator.persistence.ComparisonDAO;
import com.luksosilva.dbcomparator.persistence.temp.TempSourcesDAO;
import com.luksosilva.dbcomparator.util.JsonUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ComparisonService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(1);


    //1
    public static void processSources(Comparison comparison, List<File> fileList) throws Exception {
        comparison.getSources().addAll(getSourcesFromFiles(fileList));

        SchemaService.mapComparedSources(comparison.getSources(), comparison.getConfigRegistry());

        TempSourcesDAO.saveTempSources(comparison.getSources());
    }

    //2
    public static void processTables(Comparison comparison,
                                     ConfigRegistry configRegistry,
                                     Map<String, Map<String, SourceTable>> groupedTables) {

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


        boolean prioritizeUserColumnSettings =
                configRegistry.getConfigValueOf(ConfigKeys.DBC_PRIORITIZE_USER_COLUMN_SETTINGS);

        setTableColumnsSettings(comparedTablesWithoutColumnSettings,
               comparison.getComparedSources(), prioritizeUserColumnSettings);

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


    public static void saveComparison(Comparison comparison, File file) throws Exception {
        try {

            JsonUtils.saveComparisonAsJson(comparison, file);

            ComparisonDAO.saveCreatedComparison(file);

        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static Comparison loadComparison(File file) throws Exception {
        try {

            Comparison loadedComparison = JsonUtils.loadComparisonFromJson(file);

            ComparisonDAO.saveLoadedComparison(file);

            return loadedComparison;

        } catch (IOException e) {
            throw new IOException("Erro ao ler arquivo: " + e.getMessage());
        }
    }

    public static void deleteSavedComparison(SavedComparison savedComparison) throws Exception {
        try {

            File file = savedComparison.getFile();

            if (file.exists()) {
                file.delete();
            }

            ComparisonDAO.deleteSavedComparison(savedComparison);

        } catch (Exception e) {
            throw new IOException("Erro ao excluir: " + e.getMessage());
        }
    }



    /// HELPERS

    public static void setTableColumnsSettings(List<ComparedTable> comparedTableList,
                                               List<ComparedSource> comparedSourceList,
                                               boolean loadFromDb) {
        SchemaService.loadColumnsSettings(comparedTableList, comparedSourceList, loadFromDb);
    }


    /// privates

    private static List<Source> getSourcesFromFiles(List<File> fileList) {
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {

            String sourceName = fileList.get(i).getName();
            String sourceId = FilenameUtils.removeExtension(sourceName);

            sources.add(new Source(sourceId, i, fileList.get(i)));
        }
        return sources;
    }

    private static void setComparedTables(Comparison comparison, Map<String, Map<String, SourceTable>> groupedTables) {

        for (Map<String, SourceTable> perSourceTable : groupedTables.values()) {
            ComparedTable comparedTable = new ComparedTable(perSourceTable);
            comparison.getComparedTables().add(comparedTable);
        }

    }

    private static void setComparedTableColumns(List<ComparedTable> comparedTableList) {
        for (ComparedTable comparedTable : comparedTableList) {

            if (!comparedTable.getComparedTableColumns().isEmpty()) continue;


            Map<String, Map<String, SourceTableColumn>> groupedColumns = new HashMap<>();

            for (Map.Entry<String, SourceTable> entry : comparedTable.getPerSourceTable().entrySet()) {
                String sourceId = entry.getKey();
                SourceTable sourceTable = entry.getValue();

                for (SourceTableColumn sourceTableColumn : sourceTable.getSourceTableColumns()) {
                    String columnName = sourceTableColumn.getColumnName();

                    groupedColumns
                            .computeIfAbsent(columnName, k -> new HashMap<>())
                            .put(sourceId, sourceTableColumn);
                }
            }

            for (Map<String, SourceTableColumn> perSourceTableColumn : groupedColumns.values()) {
                ComparedTableColumn comparedTableColumn = new ComparedTableColumn(comparedTable, perSourceTableColumn);
                comparedTable.getComparedTableColumns().add(comparedTableColumn);
            }
        }
    }


}
