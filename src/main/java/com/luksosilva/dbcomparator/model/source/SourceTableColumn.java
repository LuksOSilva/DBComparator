package com.luksosilva.dbcomparator.model.source;

import java.util.Objects;

public class SourceTableColumn {

    private final int sequence;
    private final String columnName;
    private final String type;
    private final boolean notNull;
    private final boolean isPk;


    public SourceTableColumn(int sequence, String columnName, String type, boolean notNull, boolean isPk) {
        this.sequence = sequence;
        this.columnName = columnName;
        this.type = type;
        this.isPk = isPk;
        this.notNull = notNull;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isPk() {
        return isPk;
    }

    public String getType() {
        return type;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceTableColumn that)) return false;
        return sequence == that.sequence &&
                notNull == that.notNull &&
                isPk == that.isPk &&
                Objects.equals(columnName, that.columnName) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence, columnName, type, notNull, isPk);
    }

}
