package com.luksosilva.dbcomparator.model.persistence;

import java.io.File;
import java.time.LocalDateTime;

public class SavedComparison {

    private final int comparisonId;
    private final String description;
    private final File file;
    private final boolean isImported;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoadedAt;


    public SavedComparison(int comparisonId, String description, File file, boolean isImported, LocalDateTime createdAt, LocalDateTime lastLoadedAt) {
        this.comparisonId = comparisonId;
        this.description = description;
        this.file = file;
        this.isImported = isImported;
        this.createdAt = createdAt;
        this.lastLoadedAt = lastLoadedAt;
    }

    public void setLastLoadedAt(LocalDateTime lastLoadedAt) {
        this.lastLoadedAt = lastLoadedAt;
    }

    public int getComparisonId() {
        return comparisonId;
    }

    public String getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    public boolean isImported() {
        return isImported;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoadedAt() {
        return lastLoadedAt;
    }
}
