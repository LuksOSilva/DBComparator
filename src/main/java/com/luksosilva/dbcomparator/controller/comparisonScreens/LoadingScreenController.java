package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.navigator.ComparisonStepsNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LoadingScreenController implements BaseController {

    @FXML
    private Label loadingMessageLabel;

    @Override
    public void setTitle(String message) {
        if (loadingMessageLabel != null) {
            loadingMessageLabel.setText(message);
        }
    }


    @Override
    public void init(ConfigRegistry configRegistry, ComparisonStepsNavigator navigator) {}

}
