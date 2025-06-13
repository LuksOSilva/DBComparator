package com.luksosilva.dbcomparator.model.enums;

public enum SqlPlaceholders {

    SOURCE_ID("{{source_id}}"),
    TABLE_NAME("{{table_name}}"),
    COLUMN_NAME("{{column_name}}"),
    LIST_TABLE_NAMES("{{list_table_names}}"),
    IS_COMPARABLE("{{is_comparable}}"),
    IS_IDENTIFIER("{{is_identifier}}"),

    //select differences
    SOURCE_ID_DATA("{{source_id_data}}"),
    IDENTIFIER_COLUMNS("{{coalesce_identifier_columns}}");



    private final String placeholder;

    SqlPlaceholders(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
