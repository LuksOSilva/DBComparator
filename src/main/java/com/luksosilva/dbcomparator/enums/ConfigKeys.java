package com.luksosilva.dbcomparator.enums;

public enum ConfigKeys {

    DBC_CONSIDER_SQLITE_TABLES("N"),
    DBC_PRIORITIZE_USER_COLUMN_SETTINGS("Y");

    private final String defaultValue;

    ConfigKeys(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return "Y".equalsIgnoreCase(defaultValue);
    }
}
