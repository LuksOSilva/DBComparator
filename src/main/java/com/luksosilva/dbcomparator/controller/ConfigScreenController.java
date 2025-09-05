package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.config.Config;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.service.ConfigurationService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.config.ConfigRegistryViewModel;
import com.luksosilva.dbcomparator.viewmodel.live.comparison.config.ConfigViewModel;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreenController {

    private Stage currentStage;

    @FXML private VBox configContainer;
    @FXML private TextField searchTextField;

    private ConfigRegistryViewModel configRegistryViewModel;

    private ConfigRegistry configRegistry;

    public void init(Stage currentStage, ConfigRegistry registry) {
        this.currentStage = currentStage;
        setConfigRegistry(registry);
        setupSearchTextField();
        populateConfigs();
    }

    public void setConfigRegistry(ConfigRegistry registry) {
        this.configRegistry = registry;
        setupViewModels();
    }

    public void setupSearchTextField() {
        searchTextField.textProperty().addListener((obs, old, filter) -> {
            populateConfigs();
        });
    }

    private void setupViewModels() {
        this.configRegistryViewModel = new ConfigRegistryViewModel(configRegistry);

        List<ConfigViewModel> configViewModels = new ArrayList<>();
        for (Config config : configRegistry.getConfigs()) {
            configViewModels.add(new ConfigViewModel(config));
        }
        configRegistryViewModel.setConfigViewModels(configViewModels);
    }

    private boolean isAcceptedByFilter(ConfigViewModel configVM) {
        String filter = searchTextField.getText().toLowerCase().trim();
        String configDescription = configVM.getDescription().toLowerCase().trim();

        if (filter.isBlank()) return true;

        return configDescription.contains(filter);
    }

    private void populateConfigs() {
        configContainer.getChildren().clear();

        for (ConfigViewModel configVM : configRegistryViewModel.getConfigViewModels()) {

            if (!isAcceptedByFilter(configVM)) continue;

            Separator firstSeparator = new Separator();
            configContainer.getChildren().add(firstSeparator);

            HBox row = new HBox(10);
            row.setFillHeight(true);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(0, 50, 0, 10));

            Label descLabel = new Label(configVM.getDescription());
            descLabel.getStyleClass().add("config-desc");
            descLabel.setWrapText(true);

            HBox.setHgrow(descLabel, Priority.ALWAYS);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().bindBidirectional(configVM.configValueProperty());


            row.getChildren().addAll(descLabel, spacer, checkBox);
            configContainer.getChildren().add(row);

        }

        if (configContainer.getChildren().isEmpty()) {
            showEmptyMessage("Nenhuma configuração encontrada para \"" + searchTextField.getText().toLowerCase().trim() + "\".");
        }

    }

    private void showEmptyMessage(String message) {
        configContainer.getChildren().clear();

        Label emptyLabel = new Label(message);
        emptyLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 14px; -fx-font-style: italic;");

        VBox wrapper = new VBox(emptyLabel);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(20));

        configContainer.getChildren().add(wrapper);
    }

    @FXML
    private void onSaveClick() {
        List<Config> alteredConfigs = new ArrayList<>();

        for (ConfigViewModel configVM : configRegistryViewModel.getConfigViewModels()) {
            if (configVM.hasChanged()) {

                configVM.commit();
                alteredConfigs.add(configVM.getModel());

            }
        }
        if (alteredConfigs.isEmpty()) return;


        try {
            ConfigurationService.saveConfigurations(alteredConfigs);
        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao salvar configurações",
                    e.getMessage());
        }
    }


    @FXML
    private void onBackClick(MouseEvent mouseEvent) {
        try {
            FxLoadResult<Parent, HomeScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.HOME_SCREEN);

            Parent root = screenData.node;
            HomeScreenController controller = screenData.controller;

            controller.setCurrentStage(currentStage);
            controller.init();

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, currentStage.getScene().getWidth(), currentStage.getScene().getHeight());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {

        }
    }


}
