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


    SD_WITH_CLAUSE("comparison/SelectDifferencesPieces", "with_clause"),
    SD_COALESCE_IDENTIFIER_COLUMN("comparison/SelectDifferencesPieces", "coalesce_identifier_column"),
    SD_SELECT_COMPARABLE_COLUMNS("comparison/SelectDifferencesPieces", "select_comparable_columns"),
    SD_FROM_CLAUSE("comparison/SelectDifferencesPieces", "from_clause"),
    SD_JOIN_CLAUSE("comparison/SelectDifferencesPieces", "join_clause"),
    SD_ON_CLAUSE("comparison/SelectDifferencesPieces", "on_clause"),
    SD_COALESCE_COMPARABLE_COLUMN("comparison/SelectDifferencesPieces", "coalesce_comparable_column"),
    SD_WHERE_CLAUSE("comparison/SelectDifferencesPieces", "where_clause"),
    SD_COALESCE_VALUES("comparison/SelectDifferencesPieces", "coalesce_values");

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
