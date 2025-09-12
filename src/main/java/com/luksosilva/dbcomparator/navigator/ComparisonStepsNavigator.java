package com.luksosilva.dbcomparator.navigator;

import com.luksosilva.dbcomparator.controller.HomeScreenController;
import com.luksosilva.dbcomparator.controller.comparisonScreens.BaseController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.service.ComparedTableService;
import com.luksosilva.dbcomparator.service.SourceService;
import com.luksosilva.dbcomparator.service.TableComparisonResultService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class ComparisonStepsNavigator {

    private Stage stage;

    public ComparisonStepsNavigator(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public <T extends BaseController> void goTo(FxmlFiles fxml, Consumer<T> init) {
        try {
            FxLoadResult<Parent, T> screenData = FxmlUtils.loadScreen(fxml);
            T controller = screenData.controller;

            init.accept(controller);

            Scene scene = new Scene(screenData.node, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            DialogUtils.showError(stage, "Erro de Navegação", e.getMessage());
        }
    }

    public void runTask(Task<?> task, Runnable onSuccess) {
        task.setOnSucceeded(e -> onSuccess.run());
        task.setOnFailed(e -> DialogUtils.showError(stage, "Erro", task.getException().getMessage()));
        new Thread(task).start();
    }

    public void cancelComparison() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                SourceService.clearTempTables();
                ComparedTableService.clearTempTables();
                TableComparisonResultService.clearTempTables();
                return null;
            }
        };

        runTask(task, () -> {
            goTo(FxmlFiles.HOME_SCREEN, ctrl -> {
                ctrl.setTitle("");
                ctrl.init(new ConfigRegistry(), new ComparisonStepsNavigator(stage));
            });
        });
    }

}
