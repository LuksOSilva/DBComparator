package com.luksosilva.dbcomparator.queue;

import com.luksosilva.dbcomparator.builder.ComparisonResultBuilder;
import com.luksosilva.dbcomparator.builder.SelectDifferencesBuilder;
import com.luksosilva.dbcomparator.controller.comparisonScreens.ComparisonResultScreenController;
import com.luksosilva.dbcomparator.log.ComparisonLogger;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.result.TableComparisonResult;
import javafx.application.Platform;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class ComparisonQueueManager {
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final BlockingQueue<ComparedTable> queue = new LinkedBlockingQueue<>();
    private volatile boolean stopRequested = false;

    public void addTable(ComparedTable table) {
        queue.offer(table);
    }

    public void start(List<ComparedSource> sources, ComparisonResultScreenController controller) {
        executor.submit(() -> {

            File logDir = new File(System.getenv("APPDATA"), "DBComparator/logs");
            if (!logDir.exists()) logDir.mkdirs();

            File comparisonLogFile = new File(logDir, getTimestamp() + "-comparison.log");

            try (PrintWriter log = new PrintWriter(new FileWriter(comparisonLogFile, true))) {
                ComparisonLogger logger = new ComparisonLogger(log);


                while (!stopRequested) {
                    try {
                        ComparedTable table = queue.take();
                        if (stopRequested) break;

                        logger.log("Starting comparison for table: " + table.getTableName());



                        // Step 1: build select
                        logger.log("Building query");
                        //table.setSqlSelectDifferences(SelectDifferencesBuilder.build(table, sources));
                        table.setSqlSelectDifferences(SelectDifferencesBuilder.build(table, sources));
                        logger.log("Query built successfully");

                        // Step 2: compare table
                        TableComparisonResult result =
                                ComparisonResultBuilder.buildTableComparisonResult(table, sources, logger);
                        logger.log("Comparison finished for table: " + table.getTableName());

                        // Update UI
                        Platform.runLater(() -> controller.addComparedTableResult(result));

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.log("Error comparing table: " + e.getMessage());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        stopRequested = true;
        executor.shutdownNow();
        queue.clear();
    }


    private String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        return "[" +LocalDateTime.now().format(formatter) + "]";
    }

}
