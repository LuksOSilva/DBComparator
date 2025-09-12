package com.luksosilva.dbcomparator;

import com.luksosilva.dbcomparator.controller.HomeScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;

import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main extends Application {
    public static void main(String[] args) {
        try {

            SQLiteUtils.createDatabase();

            Application.launch(args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("DBComparator");
        InputStream iconStream = getClass().getResourceAsStream("/icons/logo-nobackground.png");
        if (iconStream != null) {
            Image icon = new Image(iconStream);
            stage.getIcons().add(icon);
        }


        stage.setMinWidth(1000.0);
        stage.setMinHeight(650.0);

        FxLoadResult<Parent, HomeScreenController> screenData =
                FxmlUtils.loadScreen(FxmlFiles.HOME_SCREEN);

        Parent root = screenData.node;
        HomeScreenController controller = screenData.controller;

        controller.setStage(stage);
        controller.init();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();


        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) {
            String filePath = args.getFirst();
            File file = new File(filePath);
            if (file.exists()) {
                controller.loadComparison(file);
            }
        }
    }
}