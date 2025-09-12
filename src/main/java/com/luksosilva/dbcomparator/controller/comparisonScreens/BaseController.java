package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.navigator.ComparisonStepsNavigator;
import javafx.stage.Stage;

public interface BaseController {

    void setTitle(String title);
    void init(ConfigRegistry configRegistry, ComparisonStepsNavigator comparisonStepsNavigator);
}
