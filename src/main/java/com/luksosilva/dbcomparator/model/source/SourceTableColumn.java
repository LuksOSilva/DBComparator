package com.luksosilva.dbcomparator.model.source;

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

}
