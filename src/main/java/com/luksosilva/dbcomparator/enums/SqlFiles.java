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

    CLEAR_TEMP_SOURCES("temp/source", "clear"),
    INSERT_TEMP_SOURCE("temp/source", "insert_temp_source"),
    INSERT_TEMP_SOURCE_TABLE("temp/source", "insert_temp_source_table"),
    INSERT_TEMP_SOURCE_TABLE_COLUMN("temp/source", "insert_temp_source_table_column"),
    DELETE_TEMP_SOURCES("temp/source","delete_temp_sources"),
    DELETE_TEMP_SOURCE_TABLES("temp/source","delete_temp_source_tables"),
    DELETE_TEMP_SOURCE_TABLE_COLUMNS("temp/source","delete_temp_source_table_columns"),
    SELECT_TEMP_SOURCES("temp/source", "select_temp_sources"),
    SELECT_TEMP_SOURCE_TABLES("temp/source", "select_temp_source_tables"),
    SELECT_TEMP_SOURCES_FILES("temp/source", "select_temp_sources_files"),
    SELECT_TEMP_SOURCE_COLUMNS("temp/source", "select_temp_source_columns"),

    CLEAR_TEMP_COMPARED_TABLES("temp/comparedTable", "clear"),
    SELECT_TEMP_COMPARED_TABLES("temp/comparedTable", "select_temp_compared_tables"),
    SELECT_TEMP_COMPARED_TABLE_COLUMNS("temp/comparedTable", "select_temp_compared_table_columns"),
    SELECT_TEMP_COMPARED_TABLES_MAX_COD("temp/comparedTable", "select_temp_compared_tables_max_cod"),
    INSERT_TEMP_COMPARED_TABLES("temp/comparedTable","insert_temp_compared_tables"),
    INSERT_TEMP_COMPARED_COLUMNS("temp/comparedTable","insert_temp_compared_columns"),
    DELETE_TEMP_COMPARED_TABLES("temp/comparedTable", "delete_temp_compared_tables"),
    DELETE_TEMP_COMPARED_TABLE_COLUMNS("temp/comparedTable", "delete_temp_compared_table_columns"),
    DELETE_TEMP_COMPARED_TABLE_COLUMN_CONFIGS("temp/comparedTable", "delete_temp_compared_table_column_configs"),
    DELETE_TEMP_COMPARED_TABLE_COLUMN_FILTERS("temp/comparedTable", "delete_temp_compared_table_column_filters"),
    DELETE_TEMP_COMPARED_TABLE_FILTERS("temp/comparedTable", "delete_temp_compared_table_filters"),
    SELECT_TEMP_COMPARED_TABLES_NAMES("temp/comparedTable", "select_temp_compared_tables_names"),
    SELECT_TEMP_COMPARED_TABLES_FROM_SOURCES("temp/comparedTable", "select_temp_compared_tables_from_sources"),
    PROCESS_TEMP_COMPARED_COLUMNS("temp/comparedTable", "process_temp_compared_columns"),
    PROCESS_TEMP_COMPARED_COLUMN_CONFIGS("temp/comparedTable","process_temp_compared_column_configs"),
    UPDATE_TEMP_COMPARED_TABLE_COLUMN_CONFIG_VALIDATION("temp/comparedTable", "update_temp_compared_table_column_config_validation"),

    CLEAR_TEMP_COMPARED_COLUMN_CONFIGS("temp/columnSettings", "clear"),
    REPLACE_TEMP_COMPARED_COLUMN_CONFIGS("temp/columnSettings","replace_temp_compared_column_configs"),
    SELECT_TEMP_COMPARED_COLUMN_CONFIGS("temp/columnSettings", "select_temp_compared_column_configs"),


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
