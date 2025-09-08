package com.luksosilva.dbcomparator.model.live.source;

import java.util.Objects;

public class SourceTableColumn {

    private int sequence;
    private String columnName;
    private String type;
    private boolean notNull;
    private boolean isPk;


    public SourceTableColumn() {}

    public SourceTableColumn(int sequence, String columnName, String type, boolean notNull, boolean isPk) {
        this.sequence = sequence;
        this.columnName = columnName;
        this.type = type;
        this.isPk = isPk;
        this.notNull = notNull;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public void setPk(boolean pk) {
        isPk = pk;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isPk() {
        return isPk;
    }

    public boolean isNotNull() {
        return notNull;
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
