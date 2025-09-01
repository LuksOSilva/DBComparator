package com.luksosilva.dbcomparator.model.live.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SourceTable {

    private String tableName;
    private int recordCount;

    private List<SourceTableColumn> sourceTableColumns = new ArrayList<>();

    public SourceTable() {}

    public SourceTable(String tableName) {
        this.tableName = tableName;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setSourceTableColumns(List<SourceTableColumn> sourceTableColumns) {
        this.sourceTableColumns = sourceTableColumns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<SourceTableColumn> getSourceTableColumns() {
        return sourceTableColumns;
    }



    public int getRecordCount() {
        return recordCount;
    }

    public boolean equalSchema(SourceTable that) {
        return Objects.equals(this.getTableName(), that.getTableName())
                && Objects.equals(this.getSourceTableColumns(), that.getSourceTableColumns());
    }
    public boolean equalRecordCount(SourceTable that) {
        return recordCount == that.recordCount;
    }

}
