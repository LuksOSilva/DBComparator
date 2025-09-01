package com.luksosilva.dbcomparator.model.live.comparison.customization;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnSettings {

    private static final Logger log = LoggerFactory.getLogger(ColumnSettings.class);
    private boolean isComparable;
    private boolean isIdentifier;


    public ColumnSettings() {}

    public ColumnSettings(boolean isComparable, boolean isIdentifier) {
        this.isComparable = isComparable;
        this.isIdentifier = isIdentifier;
    }

    public boolean isComparable() {
        return isComparable;
    }

    public boolean isIdentifier() {
        return isIdentifier;
    }

    public void setComparable(boolean comparable) {
        isComparable = comparable;
    }

    public void setIdentifier(boolean identifier) {
        isIdentifier = identifier;
    }


    public void changeIsComparableTo(boolean isComparable) {
        this.isComparable =isComparable;
    }
    public void changeIsIdentifierTo(boolean isIdentifier) {
        this.isIdentifier =isIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnSettings that = (ColumnSettings) o;

        return isComparable == that.isComparable && isIdentifier == that.isIdentifier;
    }

    @Override
    public int hashCode() {
        int result = (isComparable ? 1 : 0);
        result = 31 * result + (isIdentifier ? 1 : 0);
        return result;
    }

}

