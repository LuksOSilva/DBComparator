package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.model.source.Source;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.FileUtils;
import javafx.css.StyleClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class AttachSourcesScreenController {

    @FXML
    public Button nextButton;
    @FXML
    public Button previousButton;
    @FXML
    public Text cancelText;

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



    public Map<Pane, Source> perPaneSource = new LinkedHashMap<>();



    public void nextStep(ActionEvent event) {
    }

    public void previousStep(ActionEvent event) {
    }

    public void cancelComparison(MouseEvent mouseEvent) {
    }


    public void attachSource(MouseEvent mouseEvent) {
        Pane clickedPane = (Pane) mouseEvent.getSource();

        Stage ownerStage = (Stage) clickedPane.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha uma fonte de dados");

        if (!perPaneSource.isEmpty()) {

            Source lastAttachedSource = null;
            for (Source source : perPaneSource.values()) {
                lastAttachedSource = source;
            }

            fileChooser.setInitialDirectory(lastAttachedSource.getPath().getParentFile());
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("DB Files", "*.s3db", "*.db"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(ownerStage);

        if (selectedFile != null) {

            if (!perPaneSource.isEmpty()) {
                for (Source source : perPaneSource.values()) {

                    if (FileUtils.areFilesEqual(source.getPath(), selectedFile)) {

                        showFileAlreadyChosenWarning(selectedFile);
                        return;
                    }
                    if (FileUtils.areFileNamesEqual(source.getPath(), selectedFile)) {

                        showFileNameRepeatedWarning(selectedFile);
                        return;

                    }

                }
            }

            System.out.println("File selected: " + selectedFile.getAbsolutePath());
            perPaneSource.put(clickedPane, new Source(selectedFile));

            changePaneToAttached(clickedPane);

        } else {
            System.out.println("File selection cancelled.");
            // Handle cases where the user cancels the dialog
        }

    }


    public void detachSource(MouseEvent mouseEvent) {
        Pane clickedPane = (Pane) mouseEvent.getSource();

        perPaneSource.remove(clickedPane);

        changePaneToEmpty(clickedPane);

    }

    //

    private void changePaneToAttached(Pane pane) {
        pane.setOnMouseClicked(this::detachSource);
        pane.getStyleClass().remove("empty-drop-box");
        pane.getStyleClass().add("attached-drop-box");

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
        pane.getStyleClass().remove("attached-drop-box");
        pane.getStyleClass().add("empty-drop-box");

        if (pane == attachSourceA) {
            sourceIdLabelA.setVisible(false);
            sourcePathLabelA.setVisible(false);
        }
        else if (pane == attachSourceB) {
            sourceIdLabelB.setVisible(false);
            sourcePathLabelB.setVisible(false);
        }
    }


    private void showFileAlreadyChosenWarning(File selectedFile) {
        DialogUtils.showWarning(
                "Arquivo já selecionado.",
                "O arquivo " + selectedFile.getName() + " já foi selecionado."
        );
    }
    private void showFileNameRepeatedWarning(File selectedFile) {
        DialogUtils.showWarning(
                "Os arquivos devem ter nomes diferentes.",
                "Um arquivo de nome " + FilenameUtils.removeExtension(selectedFile.getName()) + " já foi selecionado."
        );
    }


}
