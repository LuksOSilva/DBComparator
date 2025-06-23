package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class FxmlUtils {

    public static <T extends Parent, U> FxLoadResult<T, U> loadScreen(FxmlFiles fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(FxmlUtils.class.getResource(fxmlFile.getPath()));
        T root = loader.load();
        U controller = loader.getController();

        return new FxLoadResult<>(root, controller);
    }

    public static <T extends Parent, U> FxLoadResult<Stage, U> createNewStage(FxmlFiles fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(FxmlUtils.class.getResource(fxmlFile.getPath()));

        Parent root = loader.load();
        U controller = loader.getController();

        Stage newStage = new Stage();
        newStage.setTitle(title);
        newStage.setScene(new Scene(root));

        return new FxLoadResult<>(newStage, controller);
    }


    public static Parent loadGUI(FxmlFiles fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(FxmlUtils.class.getResource(fxmlFile.getPath()));
        return loader.load();
    }

}
