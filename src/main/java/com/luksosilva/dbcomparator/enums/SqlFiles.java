package com.luksosilva.dbcomparator.enums;


public enum SqlFiles {


    REPLACE_COLUMN_DEFAULTS("persistence", "replace_column_defaults"),
    INSERT_DBC_COMPARISONS("persistence", "insert_dbc_comparisons"),
    SELECT_DBC_COMPARISON("persistence", "select_dbc_comparison"),
    SELECT_DBC_COMPARISONS("persistence", "select_dbc_comparisons"),
    UPDATE_LAST_LOADED0("persistence", "update_last_loaded"),
    DELETE_DBC_COMPARISON("persistence", "delete_dbc_comparison"),

    SELECT_DBC_CONFIGS("config", "select_dbc_configs"),
    UPDATE_DBC_CONFIG("config", "update_dbc_config"),

    PRAGMA_TABLE_LIST("comparison", "pragma_table_list"),
    SELECT_TABLE_RECORD_COUNT("comparison", "select_table_record_count"),
    PRAGMA_TABLE_INFO("comparison", "pragma_table_info"),
    SELECT_COLUMN_DEFAULTS("comparison", "select_column_defaults"),
    SELECT_VALIDATE_IDENTIFIERS("comparison", "select_validate_identifiers"),
    SELECT_VALIDATE_FILTERS("comparison", "select_validate_filters"),
    SELECT_DIFFERENCES("comparison", "select_differences"),

    /// TEMPORARY TABLES

    INSERT_TEMP_SOURCE("temp/source", "insert_temp_source"),
    INSERT_TEMP_SOURCE_TABLE("temp/source", "insert_temp_source_table"),
    INSERT_TEMP_SOURCE_TABLE_COLUMN("temp/source", "insert_temp_source_table_column"),

    CLEAR_TEMP_SOURCES("temp/source", "clear"),
    CLEAR_TEMP_COMPARED_TABLES("temp/comparedTable", "clear"),
    CLEAR_TEMP_TABLE_COMPARISON_RESULT("temp/tableComparisonResult", "clear");



    private final String type;
    private final String fileName;

    SqlFiles(String type, String fileName) {
        this.type = type;
        this.fileName = fileName;
    }

    public String fullPath() {
        return "/sql/" + type + "/" + fileName + ".sql";
    }


}
