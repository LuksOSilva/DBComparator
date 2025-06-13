package com.luksosilva.dbcomparator.model.enums;


public enum FxmlFiles {

    MAIN_SCREEN("MainScreen", "MainScreen");

    private final String directory;
    private final String fileName;

    FxmlFiles(String directory, String fileName) {
        this.directory = directory;
        this.fileName = fileName;
    }

    public String getPath() {
        return "/fxml/" + directory + "/" + fileName + ".fxml";
    }

    public String directory() {
        return directory;
    }

    public String fileName() {
        return fileName;
    }
}
