package com.luksosilva.dbcomparator;

import com.luksosilva.dbcomparator.enums.FxmlFiles;

import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {

        Application.launch(args);

    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FxmlUtils.loadGUI(FxmlFiles.HOME_SCREEN);
        stage.setTitle("DBComparator");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(new Scene(root));
        stage.show();
    }
}