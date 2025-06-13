package com.luksosilva.dbcomparator.model.comparison;

public class ComparedTableColumnSettings {

    private Boolean isComparable;
    private Boolean isIdentifier;


    public ComparedTableColumnSettings() {}

    public ComparedTableColumnSettings(Boolean isComparable, Boolean isIdentifier) {
        this.isComparable = isComparable;
        this.isIdentifier = isIdentifier;
    }

    public Boolean isComparable() {
        return isComparable;
    }

    public Boolean isIdentifier() {
        return isIdentifier;
    }

    @Override
    public String toString() {
        return "ColumnSetting{isComparable=" + isComparable + ", isIdentifier=" + isIdentifier + "}";
    }
}
