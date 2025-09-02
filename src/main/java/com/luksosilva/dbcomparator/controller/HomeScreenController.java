package com.luksosilva.dbcomparator.controller;

import com.luksosilva.dbcomparator.controller.comparisonScreens.AttachSourcesScreenController;
import com.luksosilva.dbcomparator.controller.comparisonScreens.ComparisonResultScreenController;
import com.luksosilva.dbcomparator.enums.FxmlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.Comparison;
import com.luksosilva.dbcomparator.model.persistence.SavedComparison;
import com.luksosilva.dbcomparator.persistence.ComparisonDAO;
import com.luksosilva.dbcomparator.service.ComparisonService;
import com.luksosilva.dbcomparator.util.DialogUtils;
import com.luksosilva.dbcomparator.util.wrapper.FxLoadResult;
import com.luksosilva.dbcomparator.util.FxmlUtils;
import com.luksosilva.dbcomparator.viewmodel.persistence.SavedComparisonViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeScreenController {



    private Stage currentStage;

    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 10;
    private final List<SavedComparisonViewModel> savedComparisonViewModelList = new ArrayList<>();


    @FXML
    public Button newComparisonBtn;
    @FXML
    public Button importComparisonBtn;
    @FXML
    public VBox comparisonsContainer;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button prevBtn;
    @FXML
    public Label pageLabel;
    @FXML
    public Button nextBtn;

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void init() {
        try {
            List<SavedComparison> savedComparisons = ComparisonDAO.loadAllComparisons();

            setupSavedComparisonViewModels(savedComparisons);

            setupComparisonsHistory();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void newComparisonBtnClick(MouseEvent mouseEvent) {
        startNewComparison();
    }

    public void importComparisonBtnClick(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Comparison JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DBC Files", "*.dbc", "*.json"));
        File file = fileChooser.showOpenDialog(currentStage);
        if (file == null) return;

        loadComparison(file);
    }

    public void loadComparisonBtnClick(SavedComparison savedComparison) {
        loadComparison(savedComparison.getFile());
    }

    public void deleteComparisonBtnClick(SavedComparison savedComparison) {
        deleteComparison(savedComparison);
    }

    //

    private void setupSavedComparisonViewModels(List<SavedComparison> savedComparisonList) {
        for (SavedComparison savedComparison : savedComparisonList) {
            savedComparisonViewModelList.add(new SavedComparisonViewModel(savedComparison));
        }
    }

    private void setupComparisonsHistory() {
        comparisonsContainer.getChildren().clear();
        renderCurrentPage();

        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            renderCurrentPage();
        });
    }



    private void renderCurrentPage() {
        comparisonsContainer.getChildren().clear();

        String filter = searchTextField.getText();
        List<SavedComparisonViewModel> filtered = new ArrayList<>(savedComparisonViewModelList);

        filtered.sort((a, b) -> {
            LocalDateTime dateA = (a.getLastLoadedAtRaw() != null)
                    ? a.getLastLoadedAtRaw()
                    : a.getCreatedAtRaw();

            LocalDateTime dateB = (b.getLastLoadedAtRaw() != null)
                    ? b.getLastLoadedAtRaw()
                    : b.getCreatedAtRaw();

            return dateB.compareTo(dateA);
        });


        if (filter != null && !filter.isBlank()) {
            String lower = filter.toLowerCase();
            filtered.removeIf(vm -> vm.getDescription() == null ||
                    !vm.getDescription().toLowerCase().contains(lower));
        }

        if (filtered.isEmpty()) {
            if (filter != null && !filter.isBlank()) {
                if (filter.length() > 50) {
                    showEmptyMessage(getTooLongMessage());
                    return;
                }

                showEmptyMessage("Nenhuma comparação encontrada para \"" + filter + "\".");
            } else {
                showEmptyMessage("Suas comparações aparecerão aqui.");
            }
            return;
        }


        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex;

        if (fromIndex >= filtered.size()) {
            currentPage = 0;
            fromIndex = 0;
            toIndex = Math.min(ITEMS_PER_PAGE, filtered.size());
        } else {
            toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filtered.size());
        }

        List<SavedComparisonViewModel> pageItems = filtered.subList(fromIndex, toIndex);


        for (SavedComparisonViewModel savedComparisonVM : pageItems) {
            HBox card = constructCardContainer();
            VBox infoBox = constructCardInfoBox(savedComparisonVM);
            HBox actionButtons = constructCardActionButtons(savedComparisonVM);

            HBox.setHgrow(infoBox, Priority.ALWAYS);
            card.getChildren().addAll(infoBox, actionButtons);

            card.setUserData(savedComparisonVM.getModel());

            comparisonsContainer.getChildren().add(card);
        }


        if (filtered.size() > ITEMS_PER_PAGE) {
            prevBtn.setDisable(false);
            nextBtn.setDisable(false);


            prevBtn.setDisable(currentPage == 0);
            prevBtn.setOnAction(e -> {
                if (currentPage > 0) {
                    currentPage--;
                    renderCurrentPage();
                }
            });

            nextBtn.setDisable(toIndex >= filtered.size());
            nextBtn.setOnAction(e -> {
                if (toIndex < filtered.size()) {
                    currentPage++;
                    renderCurrentPage();
                }
            });

        }
        else {
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
        }


        int totalPages = (int) Math.ceil(filtered.size() / (double) ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        pageLabel.setText("Página " + (currentPage + 1) + " de " + totalPages);
    }

    private void showEmptyMessage(String message) {
        comparisonsContainer.getChildren().clear();

        Label emptyLabel = new Label(message);
        emptyLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 14px; -fx-font-style: italic;");

        // Center it nicely
        VBox wrapper = new VBox(emptyLabel);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(20));

        comparisonsContainer.getChildren().add(wrapper);

        // Disable pagination controls since no items to paginate
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
        pageLabel.setText("Página 1 de 1");
    }


    private HBox constructCardContainer() {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ccc;");
        card.setPrefWidth(comparisonsContainer.getPrefWidth());
        card.setAlignment(Pos.CENTER_LEFT);

        return card;
    }

    private VBox constructCardInfoBox(SavedComparisonViewModel savedComparisonVM) {
        VBox infoBox = new VBox();
        infoBox.setSpacing(5);
        Label descriptionLabel = new Label(savedComparisonVM.getDescription());
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label fileLabel = new Label(savedComparisonVM.getFilePath());
        fileLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        Label createdLabel = new Label(savedComparisonVM.getCreatedAt());
        createdLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #777;");

        Label lastLoadedLabel = new Label(savedComparisonVM.getLastLoadedAt());
        lastLoadedLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #777;");

        infoBox.getChildren().addAll(descriptionLabel, fileLabel, createdLabel, lastLoadedLabel);

        return infoBox;
    }

    private HBox constructCardActionButtons(SavedComparisonViewModel savedComparisonVM) {
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        Button openBtn = new Button("Abrir");
        openBtn.setOnAction(e -> loadComparisonBtnClick(savedComparisonVM.getModel()));

        Button deleteBtn = new Button("Excluir");
        deleteBtn.setOnAction(e -> deleteComparisonBtnClick(savedComparisonVM.getModel()));

        buttonBox.getChildren().addAll(openBtn, deleteBtn);

        return buttonBox;
    }



    //

    private void startNewComparison() {
        try {

            FxLoadResult<Parent, AttachSourcesScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.ATTACH_SOURCES_SCREEN);

            Parent root = screenData.node;
            AttachSourcesScreenController controller = screenData.controller;

            controller.setCurrentStage(currentStage);
            controller.setComparison(new Comparison());

            Scene scene = new Scene(root, currentStage.getScene().getWidth(), currentStage.getScene().getHeight());
            currentStage.setScene(scene);
            currentStage.show();

        } catch (IOException e) {
            DialogUtils.showError(currentStage,
                    "Erro ao carregar página",
                    e.getMessage());
        }
    }

    private void loadComparison(File file) {
        try {

            Comparison loadedComparison = ComparisonService.loadComparison(file);


            FxLoadResult<Parent, ComparisonResultScreenController> screenData =
                    FxmlUtils.loadScreen(FxmlFiles.COMPARISON_RESULT_SCREEN);

            Parent root = screenData.node;
            ComparisonResultScreenController controller = screenData.controller;

            controller.setCurrentStage(currentStage);
            controller.init(loadedComparison);


            Scene scene = new Scene(root, currentStage.getScene().getWidth(), currentStage.getScene().getHeight());
            currentStage.setScene(scene);
            currentStage.show();

        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao carregar comparação",
                    e.getMessage());
        }
    }

    private void deleteComparison(SavedComparison savedComparison) {
        boolean confirmCancel = DialogUtils.askConfirmation(currentStage,
                "Deletar comparação",
                "Deseja deletar essa comparação? O arquivo continuará salvo na sua máquina");;
        if (!confirmCancel) return;

        try {
            ComparisonService.deleteSavedComparison(savedComparison);

            for (SavedComparisonViewModel savedComparisonViewModel : savedComparisonViewModelList) {
                if (savedComparisonViewModel.getModel().equals(savedComparison)) {
                    savedComparisonViewModelList.remove(savedComparisonViewModel);
                    break;
                }
            }

            for (Node card : comparisonsContainer.getChildren()) {
                if (card.getUserData().equals(savedComparison)) {
                    comparisonsContainer.getChildren().remove(card);
                    break;
                }
            }

            renderCurrentPage();

        } catch (Exception e) {
            DialogUtils.showError(currentStage,
                    "Erro ao excluir comparação",
                    e.getMessage());
        }
    }


    public void settingsBtnClick(MouseEvent mouseEvent) {
    }

    public void exitBtnClick(MouseEvent mouseEvent) {
        Platform.exit();
        System.exit(0);
    }

    private String getTooLongMessage() {
        Random random = new Random();
        int number = random.nextInt(10);

        return switch (number) {
            case 0 -> "Tá procurando uma comparação ou escrevendo uma redação?";
            case 1 -> "O Word é pra lá >";
            case 2 -> "Continua digitando... alguma hora deve aparecer alguma coisa, né?";
            case 3 -> "Tá difícil assim achar o que precisa?";
            case 4 -> "Se ainda não apareceu não é agora que vai aparecer...";
            case 5 -> "Dormiu no teclado?";
            case 6 -> "Se eu ganhasse 1 real por letra digitada...";
            case 7 -> "Iniciando autodestruição em 3...";
            case 8 -> "Tá se divertindo?";
            case 9 -> "Eu devia ter colocado um limite de caracteres..";
            default -> "Essa mensagem não deveria aparecer...";
        };
    }
}
