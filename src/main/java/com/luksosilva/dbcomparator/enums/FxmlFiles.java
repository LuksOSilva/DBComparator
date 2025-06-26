package com.luksosilva.dbcomparator.enums;


public enum FxmlFiles {

    HOME_SCREEN("HomeScreen"),
    LOADING_SCREEN("comparisonScreens/LoadingScreen"),
    ATTACH_SOURCES_SCREEN("comparisonScreens/AttachSourcesScreen"),
    SELECT_TABLES_SCREEN("comparisonScreens/SelectTablesScreen"),
    COLUMN_SETTINGS_SCREEN("comparisonScreens/ColumnSettingsScreen"),
    SET_FILTERS_SCREEN("comparisonScreens/SetFiltersScreen");

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
