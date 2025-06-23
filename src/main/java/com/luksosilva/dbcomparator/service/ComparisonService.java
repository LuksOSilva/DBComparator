package com.luksosilva.dbcomparator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.luksosilva.dbcomparator.builder.ComparisonResultBuilder;
import com.luksosilva.dbcomparator.builder.SelectDifferencesBuilder;
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
        SchemaService.loadColumnsSettings(comparison.getComparedTables(), comparison.getComparedSources());
    }

    //3
    public static void processColumnSettings(Comparison comparison,
                                             Map<ComparedTableColumn, ComparedTableColumnSettings> perComparedTableColumnColumnSettings,
                                             boolean saveSettingsAsDefault) {

        perComparedTableColumnColumnSettings.forEach((comparedTableColumn, comparedTableColumnSettings) -> {

            comparedTableColumn.getColumnSetting().changeIsComparableTo(comparedTableColumnSettings.isComparable());
            comparedTableColumn.getColumnSetting().changeIsIdentifierTo(comparedTableColumnSettings.isIdentifier());

        });


        if (saveSettingsAsDefault) {
            saveColumnsSettings(comparison.getComparedTables());
        }
    }

    //4
    public static void processFilters(Map<ComparedTableColumn, List<String>> perComparedTableColumnFilter) {

        perComparedTableColumnFilter.forEach((comparedTableColumn, filter) -> {

            comparedTableColumn.getColumnFilter().addAll(filter);

        });

    }

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




    //privates

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

    private static void saveColumnsSettings(List<ComparedTable> comparedTableList) {
        SchemaService.saveColumnSettings(comparedTableList);
    }

}
