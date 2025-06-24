package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.source.Source;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FileUtils;
import com.luksosilva.dbcomparator.util.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.concurrent.Task;
import javafx.css.StyleClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AttachSourcesScreenController {

    private static final String EMPTY_DROP_BOX_CLASS = "empty-drop-box";
    private static final String ATTACHED_DROP_BOX_CLASS = "attached-drop-box";

    private Comparison comparison;

    public Comparison getComparison() {
        return comparison;
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    private final Map<Pane, Source> perPaneSource = new LinkedHashMap<>();


    @FXML
    public Button nextStepBtn;
    @FXML
    public Button previousStepBtn;
    @FXML
    public Text cancelBtn;

    @FXML
    public Pane attachSourceA;
    @FXML
    public Label sourceIdLabelA;
    @FXML
    public Label sourcePathLabelA;

    @FXML
    public Pane attachSourceB;
    @FXML
    public Label sourceIdLabelB;
    @FXML
    public Label sourcePathLabelB;

    public Tooltip attachPaneToolTip;
    public Tooltip detachPaneToolTip;



    public void initialize() {
        attachPaneToolTip = new Tooltip("Adicionar banco de dados");
        detachPaneToolTip = new Tooltip("Remover banco de dados");

        attachPaneToolTip.setShowDelay(Duration.millis(200));
        detachPaneToolTip.setShowDelay(Duration.millis(200));

        Tooltip.install(attachSourceA, attachPaneToolTip);
        Tooltip.install(attachSourceB, attachPaneToolTip);
    }


    public void attachSource(MouseEvent mouseEvent) {
        Pane clickedPane = (Pane) mouseEvent.getSource();

        Stage ownerStage = (Stage) clickedPane.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha uma fonte de dados");

        if (!perPaneSource.isEmpty()) {

            Source lastAttachedSource = new ArrayList<>(perPaneSource.values()).get(perPaneSource.size() - 1);

            fileChooser.setInitialDirectory(lastAttachedSource.getPath().getParentFile());
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("DB Files", "*.s3db", "*.db"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(ownerStage);
        if (selectedFile == null) return;

        boolean isValid = isSelectedFileValid(selectedFile);
        if (!isValid) return;

        System.out.println("File selected: " + selectedFile.getAbsolutePath());
        perPaneSource.put(clickedPane, new Source(selectedFile));
        changePaneToAttached(clickedPane);

    }


    public void detachSource(MouseEvent mouseEvent) {
        boolean confirm = DialogUtils.askConfirmation("Remover banco.", "Deseja remover esse banco anexado?");
        if (!confirm) {
            return;
        }

        Pane clickedPane = (Pane) mouseEvent.getSource();

        perPaneSource.remove(clickedPane);

        changePaneToEmpty(clickedPane);

    }


    public void nextStep(MouseEvent mouseEvent) {
        if (perPaneSource.isEmpty()) {
            DialogUtils.showWarning("Fontes Faltantes", "Você deve anexar ao menos uma fonte para prosseguir.");
            return;
        }

        Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

        try {
            FxLoadResult<Parent, LoadingScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.LOADING_SCREEN);

            Parent root = screenData.node;
            LoadingScreenController controller = screenData.controller;

            controller.setMessage("Processando fontes de dados, aguarde...");

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela de carregamento: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        Task<Parent> processSourcesTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {

                ComparisonService.processSources(comparison, perPaneSource.values().stream().toList());

                FxLoadResult<Parent, SelectTablesScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.SELECT_TABLES_SCREEN);

                Parent nextScreenRoot = screenData.node;
                SelectTablesScreenController controller = screenData.controller;

                controller.setComparison(comparison);
                controller.init();

                return nextScreenRoot;
            }
        };


        processSourcesTask.setOnSucceeded(event -> {
            try {

                Parent nextScreenRoot = processSourcesTask.getValue();

                Scene nextScreenScene = new Scene(nextScreenRoot);

                currentStage.setScene(nextScreenScene);

            } catch (Exception e) {
                DialogUtils.showError("Erro de Transição", "Não foi possível exibir a próxima tela: " + e.getMessage());
                e.printStackTrace();
            }
        });


        processSourcesTask.setOnFailed(event -> {
            DialogUtils.showError("Erro de Processamento", "Ocorreu um erro durante o processamento: " + processSourcesTask.getException().getMessage());
            processSourcesTask.getException().printStackTrace();

            try {
                FxLoadResult<Parent, AttachSourcesScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.ATTACH_SOURCES_SCREEN);

                Parent root = screenData.node;

                Scene currentScreenScene = new Scene(root);
                currentStage.setScene(currentScreenScene);

            } catch (IOException e) {
                DialogUtils.showError("Erro de Recuperação", "Não foi possível recarregar a tela anterior: " + e.getMessage());
                e.printStackTrace();
            }
        });


        new Thread(processSourcesTask).start();


    }

    public void previousStep(MouseEvent mouseEvent) {
        cancelComparison(mouseEvent);
    }

    public void cancelComparison(MouseEvent mouseEvent) {

        boolean confirmCancel = DialogUtils.askConfirmation("Cancelar comparação",
                "Deseja realmente cancelar essa comparação? Nenhuma informação será salva");;
        if (!confirmCancel) {
            return;
        }

        try {
            FxLoadResult<Parent, AttachSourcesScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.HOME_SCREEN);

            Parent root = screenData.node;

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela inicial: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }



    //

    private boolean isSelectedFileValid(File selectedFile) {
        if (FilenameUtils.removeExtension(selectedFile.getName()).equalsIgnoreCase("main")) {
            DialogUtils.showWarning("Nome inválido.",
                    "O nome " + FilenameUtils.removeExtension(selectedFile.getName()) + " não pode ser utilizado.");
            return false;
        }
        if (!perPaneSource.isEmpty()) {
            for (Source source : perPaneSource.values()) {

                if (FileUtils.areFilesEqual(source.getPath(), selectedFile)) {

                    DialogUtils.showWarning("Arquivo já selecionado.",
                            "O arquivo " + selectedFile.getName() + " já foi selecionado.");
                    return false;
                }
                if (FileUtils.areFileNamesEqual(source.getPath(), selectedFile)) {

                    DialogUtils.showWarning("Nome já utilizado.",
                            "Um arquivo de nome " + FilenameUtils.removeExtension(selectedFile.getName()) + " já foi selecionado.");
                    return false;
                }
            }
        }

        return true;
    }


    private void changePaneToAttached(Pane pane) {
        pane.setOnMouseClicked(this::detachSource);
        pane.getStyleClass().remove(EMPTY_DROP_BOX_CLASS);
        pane.getStyleClass().add(ATTACHED_DROP_BOX_CLASS);

        Tooltip.install(pane, detachPaneToolTip);

        Source attachedSource = perPaneSource.get(pane);

        if (pane == attachSourceA) {
            sourceIdLabelA.setText(FilenameUtils.removeExtension(attachedSource.getPath().getName()));
            sourcePathLabelA.setText(FileUtils.getDisplayPath(attachedSource.getPath()));

            sourceIdLabelA.setVisible(true);
            sourcePathLabelA.setVisible(true);
        }
        else if (pane == attachSourceB) {
            sourceIdLabelB.setText(FilenameUtils.removeExtension(attachedSource.getPath().getName()));
            sourcePathLabelB.setText(FileUtils.getDisplayPath(attachedSource.getPath()));

            sourceIdLabelB.setVisible(true);
            sourcePathLabelB.setVisible(true);
        }

    }

    private void changePaneToEmpty(Pane pane) {
        pane.setOnMouseClicked(this::attachSource);
        pane.getStyleClass().remove(ATTACHED_DROP_BOX_CLASS);
        pane.getStyleClass().add(EMPTY_DROP_BOX_CLASS);

        Tooltip.install(pane, attachPaneToolTip);

        if (pane == attachSourceA) {
            sourceIdLabelA.setVisible(false);
            sourcePathLabelA.setVisible(false);
        }
        else if (pane == attachSourceB) {
            sourceIdLabelB.setVisible(false);
            sourcePathLabelB.setVisible(false);
        }
    }

}