package com.luksosilva.dbcomparator;

import com.luksosilva.dbcomparator.controller.HomeScreenController;
import com.luksosilva.dbcomparator.controller.comparisonScreens.AttachSourcesScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;

import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setMinWidth(1000.0);
        stage.setMinHeight(650.0);

        FxLoadResult<Parent, HomeScreenController> screenData =
                FxmlUtils.loadScreen(FxmlFiles.HOME_SCREEN);

        Parent root = screenData.node;
        HomeScreenController controller = screenData.controller;

        controller.setCurrentStage(stage);
        controller.init();

        //checks if file was opened
        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) {
            String filePath = args.getFirst();
            File file = new File(filePath);
            if (file.exists()) {
                controller.loadComparison(file);
            }
        }


        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}