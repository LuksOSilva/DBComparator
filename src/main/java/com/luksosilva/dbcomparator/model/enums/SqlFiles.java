package com.luksosilva.dbcomparator.model.enums;


public enum SqlFiles {

    SELECT_NEXT_COMPARISON_ID("select_next_comparison_id"),
    PRAGMA_TABLE_LIST("pragma_table_list"),
    SELECT_TABLE_RECORD_COUNT("select_table_record_count"),
    PRAGMA_TABLE_INFO("pragma_table_info"),
    SELECT_MAP_COLUMN_SETTINGS("select_map_column_settings"),
    REPLACE_COLUMN_SETTINGS("replace_column_settings"),


    SELECT_DIFFERENCES("select_differences"),
    SD_WITH_CLAUSE("SelectDifferencesPieces\\with_clause"),
    SD_IDENTIFIER_COLUMNS("SelectDifferencesPieces\\identifier_columns");

    private final String fileName;

    SqlFiles(String fileName) {
        this.fileName = fileName;
    }

    public String fullPath() {
        return "\\sql\\" + fileName + ".sql";
    }


    public String fileName() {
        return fileName;
    }

}
