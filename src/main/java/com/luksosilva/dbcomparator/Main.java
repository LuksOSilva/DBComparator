package com.luksosilva.dbcomparator;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.source.Source;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.service.ComparisonService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {

        Application.launch(args);


        //Comparison comparison = new Comparison(ComparisonRepository.getNextComparisonId());
        Comparison comparison = new Comparison("0001");

        ////GET SOURCES
        List<Source> sourceList = new ArrayList<>();
        sourceList.add(new Source(new File("C:\\Users\\lucas.silva\\Downloads\\MEmu Download\\Bases\\SUP-61691\\sup.s3db")));
        sourceList.add(new Source(new File("C:\\Users\\lucas.silva\\\\Downloads\\MEmu Download\\Bases\\SUP-61691\\rca.s3db")));
        //sourceList.add(new Source(new File("C:\\Users\\lucas.silva\\\\Downloads\\MEmu Download\\Bases\\SUP-59151\\new\\tst.s3db")));

        System.out.println("Starting process sources: " + LocalDateTime.now());
        ComparisonService.processSources(comparison, sourceList);
        System.out.println("Finished process sources:: " + LocalDateTime.now());


        ////GET TABLES

        Map<String, Map<ComparedSource, SourceTable>> groupedTables = new HashMap<>();

        for (ComparedSource comparedSource : comparison.getComparedSources()) {

            for (SourceTable sourceTable : comparedSource.getSource().getSourceTables()) {
                String tableName = sourceTable.getTableName();

                groupedTables
                        .computeIfAbsent(tableName, k -> new HashMap<>())
                        .put(comparedSource, sourceTable);


            }

        }


        System.out.println("Starting process tables: " + LocalDateTime.now());
        ComparisonService.processTables(comparison, groupedTables);
        System.out.println("Finished process tables: " + LocalDateTime.now());


        ////GET FILTERS

        Map<ComparedTableColumn, List<String>> perComparedTableColumnFilter = new HashMap<>();

//        for (ComparedTable comparedTable : comparison.getComparedTables()) {
//            for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {
//
//                if (comparedTableColumn.getColumnName().equals("codcli")) {
//                    List<String> filterValues = new ArrayList<>();
//                    filterValues.add("1839");
//                    perComparedTableColumnFilter.put(comparedTableColumn, filterValues);
//                }
//
//
//            }
//        }

        System.out.println("Starting process filters: " + LocalDateTime.now());
        ComparisonService.processFilters(perComparedTableColumnFilter);
        System.out.println("Finished process filters: " + LocalDateTime.now());

        System.out.println("Starting compare: " + LocalDateTime.now());
        ComparisonService.compare(comparison);
        System.out.println("Finished compare: " + LocalDateTime.now());


    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FxmlUtils.loadGUI(FxmlFiles.HOME_SCREEN);
        stage.setTitle("hi");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(new Scene(root));
        stage.show();
    }
}