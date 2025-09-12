package com.luksosilva.dbcomparator.controller.comparisonScreens;

import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.navigator.ComparisonStepsNavigator;
import com.luksosilva.dbcomparator.service.SourceService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FileUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

public class AttachSourcesScreenController implements BaseController {


    private ComparisonStepsNavigator navigator;
    private Stage currentStage;


    private Comparison comparison = new Comparison();
    private final Map<Pane, File> perPaneFile = new LinkedHashMap<>();

    private static final String EMPTY_DROP_BOX_CLASS = "empty-drop-box";
    private static final String ATTACHED_DROP_BOX_CLASS = "attached-drop-box";

    public Comparison getComparison() {
        return comparison;
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }


    @FXML
    public Text titleLabel;
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

    public void init(ConfigRegistry configRegistry, ComparisonStepsNavigator navigator) {
        this.comparison.setConfigRegistry(configRegistry);
        this.navigator = navigator;
        this.currentStage = navigator.getStage();

        setupToolTips();
        setupDragAndDrop(attachSourceA);
        setupDragAndDrop(attachSourceB);

        computedAttachedSources();
    }


    private void computedAttachedSources() {
        try {
            List<File> attachedFiles = SourceService.getSourcesFiles();
            if (attachedFiles.isEmpty()) return;


            if (attachedFiles.getFirst().exists()) {
                perPaneFile.put(attachSourceA, attachedFiles.getFirst());
                changePaneToAttached(attachSourceA);
            }
            if (attachedFiles.getLast().exists()) {
                perPaneFile.put(attachSourceB, attachedFiles.getLast());
                changePaneToAttached(attachSourceB);
            }

        } catch (Exception e) {
            DialogUtils.showWarning(currentStage,
                    "Algo deu errado ao verificar se já existem fontes anexadas",
                    e.getMessage());
        }
    }


    private void setupToolTips() {
        attachPaneToolTip = new Tooltip("Adicionar banco de dados");
        detachPaneToolTip = new Tooltip("Remover banco de dados");

        Tooltip.install(attachSourceA, attachPaneToolTip);
        Tooltip.install(attachSourceB, attachPaneToolTip);
    }


    public void attachSource(MouseEvent mouseEvent) {
        Pane clickedPane = (Pane) mouseEvent.getSource();
        Stage ownerStage = (Stage) clickedPane.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha uma fonte de dados");

        if (!perPaneFile.isEmpty()) {

            File lastAttachedFile = new ArrayList<>(perPaneFile.values()).get(perPaneFile.size() - 1);

            fileChooser.setInitialDirectory(lastAttachedFile.getParentFile());
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQLite Databases", "*.sqlite", "*.db", "*.s3db"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(ownerStage);
        if (selectedFile == null) return;

        boolean isValid = isSelectedFileValid(selectedFile);
        if (!isValid) return;


        perPaneFile.put(clickedPane, selectedFile);
        changePaneToAttached(clickedPane);

    }


    public void detachSource(MouseEvent mouseEvent) {
        boolean confirm = DialogUtils.askConfirmation(currentStage,
                "Remover banco.",
                "Deseja remover esse banco anexado?");
        if (!confirm) {
            return;
        }

        Pane clickedPane = (Pane) mouseEvent.getSource();

        perPaneFile.remove(clickedPane);

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


                    if (!perPaneFile.containsKey(pane)) {
                        // Pane is empty, attach directly
                        perPaneFile.put(pane, file);
                        changePaneToAttached(pane);
                        success = true;
                    } else {
                        // Pane already has a source, ask if should replace
                        boolean confirmReplace = DialogUtils.askConfirmation(currentStage,
                                "Substituir arquivo?",
                                "Já existe um arquivo aqui. Deseja substituí-lo?");
                        if (confirmReplace) {
                            perPaneFile.put(pane, file);
                            changePaneToAttached(pane);
                            success = true;
                        }
                    }
                }

                // If there’s a second file, attach to the other pane if available
                if (fileIterator.hasNext()) {
                    File secondFile = fileIterator.next();

                    // Find the other pane
                    Pane otherPane = (pane == attachSourceA) ? attachSourceB : attachSourceA;

                    if (!perPaneFile.containsKey(otherPane)) {
                        perPaneFile.put(otherPane, secondFile);
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
            DialogUtils.showWarning(currentStage,
                    "Arquivo não é um banco de dados.",
                    "Apenas arquivos de extensão .s3db ou .db podem ser adicionados.");
            return false;
        }

        if (FilenameUtils.removeExtension(selectedFile.getName()).equalsIgnoreCase("main")) {
            DialogUtils.showWarning(currentStage,
                    "Nome inválido.",
                    "O nome " + FilenameUtils.removeExtension(selectedFile.getName()) + " não pode ser utilizado.");
            return false;
        }
        if (!perPaneFile.isEmpty()) {
            for (File file : perPaneFile.values()) {

                if (FileUtils.areFilesEqual(file, selectedFile)) {

                    DialogUtils.showWarning(currentStage,
                            "Arquivo já selecionado.",
                            "O arquivo " + selectedFile.getName() + " já foi selecionado.");
                    return false;
                }
                if (FileUtils.areFileNamesEqual(file, selectedFile)) {

                    DialogUtils.showWarning(currentStage,
                            "Nome já utilizado.",
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

        File attachedFile = perPaneFile.get(pane);

        if (pane == attachSourceA) {
            sourceIdLabelA.setText(FilenameUtils.removeExtension(attachedFile.getName()));
            sourcePathLabelA.setText(FileUtils.getDisplayPath(attachedFile));

            sourceIdLabelA.setVisible(true);
            sourcePathLabelA.setVisible(true);
        }
        else if (pane == attachSourceB) {
            sourceIdLabelB.setText(FilenameUtils.removeExtension(attachedFile.getName()));
            sourcePathLabelB.setText(FileUtils.getDisplayPath(attachedFile));

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
        if (perPaneFile.size() <2) {
            DialogUtils.showWarning(currentStage,
                    "Fontes Faltantes",
                    "Você deve anexar duas fontes para prosseguir.");
            return;
        }

        navigator.goTo(FxmlFiles.LOADING_SCREEN, ctrl -> {
            ctrl.setTitle("Processando fontes, aguarde...");
        });

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                SourceService.processSources(perPaneFile.values().stream().toList(), comparison.getConfigRegistry());
                return null;
            }
        };

        navigator.runTask(task, () -> {
            navigator.goTo(FxmlFiles.SELECT_TABLES_SCREEN, ctrl -> {
                ctrl.setTitle("selecione as tabelas a serem comparadas");
                ctrl.init(comparison.getConfigRegistry(), navigator);
            });
        });
    }

    public void previousStep(MouseEvent mouseEvent) {
        cancelComparison(mouseEvent);
    }

    public void cancelComparison(MouseEvent mouseEvent) {
        boolean confirmCancel = DialogUtils.askConfirmation(currentStage,
                "Cancelar comparação",
                "Deseja realmente cancelar essa comparação? Nenhuma informação será salva");;
        if (!confirmCancel) {
            return;
        }

        navigator.goTo(FxmlFiles.LOADING_SCREEN, ctrl -> {
            ctrl.setTitle("cancelando comparação, aguarde...");
        });

        navigator.cancelComparison();
    }

    @Override
    public void setTitle(String message) {
        titleLabel.setText(message);
    }
}