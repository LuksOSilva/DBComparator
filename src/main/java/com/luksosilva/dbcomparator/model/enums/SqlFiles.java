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
    SD_COALESCE_IDENTIFIER_COLUMNS("SelectDifferencesPieces\\coalesce_identifier_columns"),
    SD_SELECT_COMPARABLE_COLUMNS("SelectDifferencesPieces\\select_comparable_columns"),
    SD_FROM_CLAUSE("SelectDifferencesPieces\\from_clause"),
    SD_JOIN_CLAUSE("SelectDifferencesPieces\\join_clause"),
    SD_ON_CLAUSE("SelectDifferencesPieces\\on_clause");

    private final String fileName;

    SqlFiles(String fileName) {
        this.fileName = fileName;
    }

    public String fullPath() {
        return "\\sql\\" + fileName + ".sql";
    }


}
