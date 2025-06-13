package com.luksosilva.dbcomparator.model.source;

public class SourceTableColumn {

    private int sequence;
    private String columnName;
    private String type;
    private boolean notNull;
    private boolean isPk;


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

    @Override
    public String toString() {
        return "SourceTableColumn{sequence=" + sequence + ", name='" + columnName + "', pk=" + isPk + ", notNull=" + notNull + "}";
    }

}
