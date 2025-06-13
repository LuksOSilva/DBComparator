package com.luksosilva.dbcomparator.model.source;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SourceTable {

    private String tableName;
    private int recordCount;

    private List<SourceTableColumn> sourceTableColumns = new ArrayList<>();

    public SourceTable(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<SourceTableColumn> getSourceTableColumns() {
        return sourceTableColumns;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getRecordCount() {
        return recordCount;
    }

    @Override
    public String toString() {
        return "SourceTable{name='" + tableName + "', recordCount=" + recordCount + ", columns=" + sourceTableColumns.stream().map(SourceTableColumn::toString).collect(Collectors.joining(", ")) + "}";
    }
}
