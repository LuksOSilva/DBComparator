package com.luksosilva.dbcomparator.enums;


public enum FxmlFiles {

    HOME_SCREEN("HomeScreen"),
    ATTACH_SOURCES_SCREEN("comparisonScreens/AttachSourcesScreen");

    private final String fileName;

    FxmlFiles(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return "/fxml/" + fileName + ".fxml";
    }

    public String fileName() {
        return fileName;
    }
}
