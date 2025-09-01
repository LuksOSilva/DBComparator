package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.controller.comparisonScreens.AttachSourcesScreenController;
import com.luksosilva.dbcomparator.controller.comparisonScreens.ComparisonResultScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class HomeScreenController {

    private Stage currentStage;


    @FXML
    public Button newComparisonBtn;
    @FXML
    public Button importComparisonBtn;

    public void init() {

    }

    public void newComparisonBtnClick(MouseEvent mouseEvent) {
        startNewComparison(mouseEvent);
    }

    public void importComparisonBtnClick(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Comparison JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DBC Files", "*.dbc", "*.json"));
        File file = fileChooser.showOpenDialog(currentStage);
        if (file == null) return;

        loadComparison(mouseEvent, file);
    }

    public void loadComparisonBtnClick() {

    }


    //

    private void startNewComparison(MouseEvent mouseEvent) {
        try {

            Scene currentScene = ((Node) mouseEvent.getSource()).getScene();
            currentStage = (Stage) currentScene.getWindow();


            FxLoadResult<Parent, AttachSourcesScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.ATTACH_SOURCES_SCREEN);

            Parent root = screenData.node;
            AttachSourcesScreenController controller = screenData.controller;

            controller.setCurrentStage(currentStage);
            controller.setComparison(new Comparison());

            Scene scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            currentStage.setScene(scene);
            currentStage.show();

        } catch (IOException e) {
            DialogUtils.showError(
                    "Erro ao carregar página",
                    e.getMessage());
        }
    }

    private void loadComparison(MouseEvent mouseEvent, File file) {
        try {

            Comparison loadedComparison = ComparisonService.loadComparison(file);


            Scene currentScene = ((Node) mouseEvent.getSource()).getScene();
            currentStage = (Stage) currentScene.getWindow();


            FxLoadResult<Parent, ComparisonResultScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.COMPARISON_RESULT_SCREEN);

            Parent root = screenData.node;
            ComparisonResultScreenController controller = screenData.controller;

            controller.setCurrentStage(currentStage);
            controller.init(loadedComparison);


            Scene scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            currentStage.setScene(scene);
            currentStage.show();

        } catch (Exception e) {
            DialogUtils.showError(
                    "Erro ao carregar comparação",
                    e.getMessage());
        }
    }


    public void settingsBtnClick(MouseEvent mouseEvent) {
    }

    public void exitBtnClick(MouseEvent mouseEvent) {
    }
}
