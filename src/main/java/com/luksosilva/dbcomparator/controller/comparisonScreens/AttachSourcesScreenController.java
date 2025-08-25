package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.Comparison;
import com.luksosilva.dbcomparator.model.source.Source;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FileUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AttachSourcesScreenController {

    private Stage currentStage;

    private Scene nextScene;

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    private Comparison comparison;
    private final Map<Pane, Source> perPaneSource = new LinkedHashMap<>();

    private static final String EMPTY_DROP_BOX_CLASS = "empty-drop-box";
    private static final String ATTACHED_DROP_BOX_CLASS = "attached-drop-box";

    public Comparison getComparison() {
        return comparison;
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    public void setNextScene(Scene nextScene) {this.nextScene = nextScene; }

    @FXML
    public Button nextStepBtn;
    @FXML
    public Button previousStepBtn;
    @FXML
    public Text cancelBtn;

    @FXML
    public AnchorPane attachSourceA;
    @FXML
    public Label sourceIdLabelA;
    @FXML
    public Label sourcePathLabelA;

    @FXML
    public AnchorPane attachSourceB;
    @FXML
    public Label sourceIdLabelB;
    @FXML
    public Label sourcePathLabelB;

    public Tooltip attachPaneToolTip;
    public Tooltip detachPaneToolTip;


    public void initialize() {
        setupToolTips();
        setupDragAndDrop(attachSourceA);
        setupDragAndDrop(attachSourceB);
    }

    public boolean needToProcess() {

        List<ComparedSource> comparedSources = comparison.getComparedSources();
        List<Source> currentSources = new ArrayList<>(perPaneSource.values());

        if (comparedSources.size() != currentSources.size()) {
            return true;
        }

        for (Source source : currentSources) {
            boolean found = comparedSources.stream()
                    .anyMatch(cs -> cs.getSource().equals(source));
            if (!found) {
                return true;
            }
        }

        return false;
    }

    private void setupToolTips() {
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
                new FileChooser.ExtensionFilter("SQLite Databases", "*.sqlite", "*.db", "*.s3db"),
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

    private void setupDragAndDrop(Pane pane) {
        pane.setOnDragEntered(event -> {
            if (event.getGestureSource() != pane && event.getDragboard().hasFiles()) {
                pane.getStyleClass().add("drag-over");
            }
            event.consume();
        });

        pane.setOnDragExited(event -> {
            pane.getStyleClass().remove("drag-over");
            event.consume();
        });

        pane.setOnDragOver(event -> {
            if (event.getGestureSource() != pane &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        pane.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                List<File> validFiles = db.getFiles().stream()
                        .filter(this::isSelectedFileValid)
                        .limit(2)
                        .toList();

                if (validFiles.isEmpty()) {
                    event.setDropCompleted(false);
                    return;
                }

                Iterator<File> fileIterator = validFiles.iterator();

                // Attach first file to the pane that received the drop
                if (fileIterator.hasNext()) {
                    File file = fileIterator.next();
                    Source newSource = new Source(file);

                    if (!perPaneSource.containsKey(pane)) {
                        // Pane is empty, attach directly
                        perPaneSource.put(pane, newSource);
                        changePaneToAttached(pane);
                        success = true;
                    } else {
                        // Pane already has a source, ask if should replace
                        boolean confirmReplace = DialogUtils.askConfirmation("Substituir arquivo?",
                                "Já existe um arquivo aqui. Deseja substituí-lo?");
                        if (confirmReplace) {
                            perPaneSource.put(pane, newSource);
                            changePaneToAttached(pane);
                            success = true;
                        }
                    }
                }

                // If there’s a second file, attach to the other pane if available
                if (fileIterator.hasNext()) {
                    File secondFile = fileIterator.next();
                    Source secondSource = new Source(secondFile);

                    // Find the other pane
                    Pane otherPane = (pane == attachSourceA) ? attachSourceB : attachSourceA;

                    if (!perPaneSource.containsKey(otherPane)) {
                        perPaneSource.put(otherPane, secondSource);
                        changePaneToAttached(otherPane);
                        success = true;
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }


    private boolean isValidExtension(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".sqlite") ||
                name.endsWith(".db") ||
                name.endsWith(".s3db");
    }



    private boolean isSelectedFileValid(File selectedFile) {
        if (!isValidExtension(selectedFile)) {
            DialogUtils.showWarning("Arquivo não é um banco de dados.",
                    "Apenas arquivos de extensão .s3db ou .db podem ser adicionados.");
            return false;
        }

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

    public void nextStep(MouseEvent mouseEvent) {
        if (perPaneSource.isEmpty()) {
            DialogUtils.showWarning("Fontes Faltantes", "Você deve anexar ao menos uma fonte para prosseguir.");
            return;
        }

        Scene currentScene = currentStage.getScene();
        currentScene.setUserData(AttachSourcesScreenController.this);


        if (!needToProcess() && nextScene != null) {
            currentStage.setScene(nextScene);
            return;
        }

        try {
            FxLoadResult<Parent, LoadingScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.LOADING_SCREEN);

            Parent root = screenData.node;
            LoadingScreenController controller = screenData.controller;

            controller.setMessage("Processando fontes de dados, aguarde...");


            Scene scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            //Scene scene = new Scene(root);
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

                comparison.getComparedSources()
                        .removeIf(comparedSource -> !perPaneSource.containsValue(comparedSource.getSource()));

                List<Source> notProcessedSources = perPaneSource.values().stream()
                        .filter(source -> comparison.getComparedSources().stream()
                                .noneMatch(comparedSource -> comparedSource.getSource().equals(source)))
                        .toList();

                ComparisonService.processSources(comparison, notProcessedSources);



                FxLoadResult<Parent, SelectTablesScreenController> screenData =
                        FxmlUtils.loadScreen(FxmlFiles.SELECT_TABLES_SCREEN);

                Parent nextScreenRoot = screenData.node;
                SelectTablesScreenController controller = screenData.controller;

                controller.setCurrentStage(currentStage);
                controller.setPreviousScene(currentScene);
                controller.setComparison(comparison);
                controller.init();

                return nextScreenRoot;
            }
        };


        processSourcesTask.setOnSucceeded(event -> {
            try {


                Parent nextScreenRoot = processSourcesTask.getValue();
                Scene nextScreenScene = new Scene(nextScreenRoot, currentScene.getWidth(), currentScene.getHeight());

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

                //Scene currentScreenScene = new Scene(root, currentStage.getWidth(), currentStage.getHeight());
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
            Scene scene = new Scene(root, currentStage.getWidth(), currentStage.getHeight());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            DialogUtils.showError("Erro de Carregamento", "Não foi possível carregar a tela inicial: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

}