package com.luksosilva.dbcomparator.enums;


public enum FxmlFiles {

    HOME_SCREEN("HomeScreen"),

    /// COMPARISON
    LOADING_SCREEN("comparisonScreens/LoadingScreen"),
    COMPARE_SCHEMAS_SCREEN("comparisonScreens/CompareSchemasScreen"),
    ATTACH_SOURCES_SCREEN("comparisonScreens/AttachSourcesScreen"),
    SELECT_TABLES_SCREEN("comparisonScreens/SelectTablesScreen"),
    COLUMN_SETTINGS_SCREEN("comparisonScreens/ColumnSettingsScreen"),
    SET_FILTERS_SCREEN("comparisonScreens/SetFiltersScreen"),

    /// DIALOGS
    COLUMN_SETTINGS_VALIDATION_DIALOG("dialogs/ColumnSettingsValidationDialog"),
    CONFIRMATION_DIALOG("dialogs/ConfirmationDialog"),
    COMPARE_SCHEMAS_DIALOG("dialogs/CompareSchemasDialog"),
    ADD_FILTER_DIALOG("dialogs/AddFilterDialog");

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
