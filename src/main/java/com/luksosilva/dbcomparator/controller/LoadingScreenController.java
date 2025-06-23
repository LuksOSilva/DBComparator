package com.luksosilva.dbcomparator.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LoadingScreenController {

    @FXML
    private Label loadingMessageLabel;

    public void setMessage(String message) {
        if (loadingMessageLabel != null) {
            loadingMessageLabel.setText(message);
        }
    }

}
