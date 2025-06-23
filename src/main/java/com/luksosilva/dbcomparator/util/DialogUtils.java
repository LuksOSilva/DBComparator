package com.luksosilva.dbcomparator.util;

import javafx.scene.control.Alert;

public class DialogUtils {


    public static void showWarning(String headerText, String contentText) {

        Alert alert = new Alert(Alert.AlertType.WARNING); // Or WARNING, or ERROR
        alert.setTitle("Atenção");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();

    }
}
