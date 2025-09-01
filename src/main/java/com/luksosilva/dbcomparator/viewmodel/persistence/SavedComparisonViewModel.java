package com.luksosilva.dbcomparator.viewmodel.persistence;

import com.luksosilva.dbcomparator.model.persistence.SavedComparison;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SavedComparisonViewModel {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private SavedComparison model;

    private SimpleStringProperty comparisonId = new SimpleStringProperty();
    private SimpleStringProperty description = new SimpleStringProperty();
    private SimpleStringProperty filePath = new SimpleStringProperty();
    private SimpleBooleanProperty isImported = new SimpleBooleanProperty();
    private SimpleStringProperty createdAt = new SimpleStringProperty();
    private SimpleStringProperty lastLoadedAt = new SimpleStringProperty();

    public SavedComparisonViewModel(SavedComparison savedComparison) {
        this.model = savedComparison;

        this.comparisonId.set(String.valueOf(savedComparison.getComparisonId()));
        this.description.set(savedComparison.getDescription());
        this.isImported.set(savedComparison.isImported());
        this.createdAt.set(savedComparison.getCreatedAt().format(DATE_TIME_FORMATTER));

        if (savedComparison.getLastLoadedAt() != null) {
            this.lastLoadedAt.set(savedComparison.getLastLoadedAt().format(DATE_TIME_FORMATTER));
        }

        try {
            this.filePath.set(savedComparison.getFile().getCanonicalPath());
        } catch (IOException e) {
            this.filePath.set("ERROR");
        }
    }

    public SavedComparison getModel() {
        return model;
    }

    public String getComparisonId() {
        return comparisonId.get();
    }

    public SimpleStringProperty comparisonIdProperty() {
        return comparisonId;
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public String getFilePath() {
        return filePath.get();
    }

    public SimpleStringProperty filePathProperty() {
        return filePath;
    }

    public boolean getIsImported() {
        return isImported.get();
    }

    public SimpleBooleanProperty isImportedProperty() {
        return isImported;
    }

    public String getCreatedAt() {
        String label = getIsImported() ? "Importado: " : "Criado: ";

        return label + createdAt.get();
    }

    public SimpleStringProperty createdAtProperty() {
        return createdAt;
    }

    public String getLastLoadedAt() {
        return lastLoadedAt.get() != null ?
                "Último carregamento: " + lastLoadedAt.get() :
                "Nunca carregado";
    }

    public SimpleStringProperty lastLoadedAtProperty() {
        return lastLoadedAt;
    }

    // NEW: Raw dates for sorting
    public LocalDateTime getCreatedAtRaw() { return model.getCreatedAt(); }
    public LocalDateTime getLastLoadedAtRaw() { return model.getLastLoadedAt(); }
}
