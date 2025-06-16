package com.luksosilva.dbcomparator.model.enums;

public enum SqlPlaceholders {

    SOURCE_ID("{{source_id}}"),
    TABLE_NAME("{{table_name}}"),
    COLUMN_NAME("{{column_name}}"),
    LIST_TABLE_NAMES("{{list_table_names}}"),
    IS_COMPARABLE("{{is_comparable}}"),
    IS_IDENTIFIER("{{is_identifier}}"),

    //select differences
    WITH_CLAUSE("{{with_clause}}"),
    SELECT_CLAUSE("{{select_clause}}"),
    FROM_CLAUSE("{{from_clause}}"),
    WHERE_CLAUSE("{{where_clause}}"),
    IDENTIFIER_COLUMNS("{{identifier_columns}}"),
    JOIN_CLAUSE("{{join_clause}}"),
    ON_CLAUSE("{{on_clause}}"),
    EQUALS_IDENTIFIER_COLUMNS("{{equals_identifier_columns}}"),
    CONDITION_COMPARABLE_COLUMNS("{{condition_comparable_columns}}"),
    DEFAULT_VALUE("{{default_value}}");



    private final String placeholder;

    SqlPlaceholders(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
