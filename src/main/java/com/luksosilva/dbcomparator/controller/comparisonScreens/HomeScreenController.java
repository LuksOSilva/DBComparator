package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeScreenController {

    private Stage stage;
    private Scene scene;
    private Parent root;


    public void newComparisonBtnClick(ActionEvent event) {
        try {

            openAttachSourcesScreen(event);

        } catch (IOException e) {
            throw new RuntimeException("não foi possível iniciar uma nova comparação" + e);
        }
    }


    //

    private void openAttachSourcesScreen(ActionEvent event) throws IOException {

        FxLoadResult<Parent, AttachSourcesScreenController> screenData =
                FxmlUtils.loadScreen(FxmlFiles.ATTACH_SOURCES_SCREEN);

        Parent root = screenData.node;
        AttachSourcesScreenController controller = screenData.controller;

        controller.setComparison(new Comparison());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

//    private void createNewScreen(ActionEvent event, String screenName) throws IOException {
//
//        FxLoadResult<Stage, AttachSourcesScreenController> newWindowData =
//                FxmlUtils.createNewStage(FxmlFiles.ATTACH_SOURCES_SCREEN, screenName);
//
//        Stage newStage = newWindowData.node;
//        AttachSourcesScreenController controller = newWindowData.controller;
//
//        // controller.initData(someConfigurationObject);
//
//        newStage.show();
//
//    }



}
