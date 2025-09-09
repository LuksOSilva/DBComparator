package com.luksosilva.dbcomparator.enums;

public enum SqlPlaceholders {

    COMPARISON_ID("{{comparison_id}}"),
    DESCRIPTION("{{description}}"),
    FILE_PATH("{{file_path}}"),
    IS_IMPORTED("{{is_imported}}"),
    CREATED_AT("{{created_at}}"),
    LAST_LOADED_AT("{{last_loaded_at}}"),

    CONFIG_VALUE("{{config_value}}"),
    CONFIG_KEY("{{config_key}}"),

    SOURCE_ID("{{source_id}}"),
    TABLE_NAME("{{table_name}}"),
    COLUMN_NAME("{{column_name}}"),
    LIST_TABLE_NAMES("{{list_table_names}}"),
    IS_COMPARABLE("{{is_comparable}}"),
    IS_IDENTIFIER("{{is_identifier}}"),
    FILTER_SQL("{{filter_sql}}"),
    IDENTIFIER_COLUMNS("{{identifier_columns}}"),

    //select differences
    WITH_CLAUSE("{{with_clause}}"),
    SELECT_CLAUSE("{{select_clause}}"),
    GROUP_BY_CLAUSE("{{group_by_clause}}"),
    HAVING_CLAUSE("{{having_clause}}");



    private final String placeholder;

    SqlPlaceholders(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
