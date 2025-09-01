package com.luksosilva.dbcomparator.queue;

import com.luksosilva.dbcomparator.builder.ComparisonResultBuilder;
import com.luksosilva.dbcomparator.builder.SelectDifferencesBuilder;
import com.luksosilva.dbcomparator.controller.comparisonScreens.ComparisonResultScreenController;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.result.TableComparisonResult;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ComparisonQueueManager {
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final BlockingQueue<ComparedTable> queue = new LinkedBlockingQueue<>();
    private volatile boolean stopRequested = false;

    public void add(ComparedTable table) {
        queue.offer(table);
    }

    public void start(List<ComparedSource> sources, ComparisonResultScreenController controller) {
        executor.submit(() -> {
            while (!stopRequested) {
                try {
                    ComparedTable table = queue.take();
                    if (stopRequested) break;

                    //1
                    table.setSqlSelectDifferences(SelectDifferencesBuilder.build(table, sources));

                    //2
                    TableComparisonResult result =
                            ComparisonResultBuilder.buildTableComparisonResult(table, sources);

                    Platform.runLater(() -> controller.addComparedTableResult(result));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        stopRequested = true;
        executor.shutdownNow(); // stops current work
        queue.clear(); // drops any pending jobs
    }
}
