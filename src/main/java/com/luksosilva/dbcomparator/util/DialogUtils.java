package com.luksosilva.dbcomparator.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class DialogUtils {


    public static void showWarning(String headerText, String contentText) {

        Alert alert = new Alert(Alert.AlertType.WARNING); // Or WARNING, or ERROR
        alert.setTitle("Atenção");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();

    }

    public static void showError(String title, String headerText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    public static boolean askConfirmation(String title, String headerText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);

        ButtonType buttonTypeYes = new ButtonType("Sim");
        ButtonType buttonTypeNo = new ButtonType("Não");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == buttonTypeYes;
    }
}
