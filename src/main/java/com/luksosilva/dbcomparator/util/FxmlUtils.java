package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.model.enums.FxmlFiles;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Objects;

public class FxmlUtils {

    public static Parent loadGUI(FxmlFiles fxmlFile) {
        try{
            return FXMLLoader.load(Objects.requireNonNull(FxmlUtils.class.getResource(fxmlFile.getPath())));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
