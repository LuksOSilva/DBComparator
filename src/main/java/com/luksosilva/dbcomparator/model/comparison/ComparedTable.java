package com.luksosilva.dbcomparator.model.comparison;

import com.luksosilva.dbcomparator.model.source.SourceTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparedTable {

    private Map<ComparedSource, SourceTable> perSourceTable;

    private List<ComparedTableColumn> comparedTableColumns = new ArrayList<>();


    public ComparedTable(Map<ComparedSource, SourceTable> perSourceTable) {
        this.perSourceTable = perSourceTable;
    }

    public String getTableName() {
        return getPerSourceTable().values().stream()
                .findFirst()
                .map(SourceTable::getTableName)
                .orElse(null);
    }

    public List<ComparedTableColumn> getComparedTableColumns() {
        return comparedTableColumns;
    }

    public Map<ComparedSource, SourceTable> getPerSourceTable() {
        return perSourceTable;
    }



    @Override
    public String toString() {
        String sourceTableDetails = perSourceTable.entrySet().stream()
                .map(entry -> "{" + entry.getKey().getSourceId() + ": " + entry.getValue().getTableName() + " (Records: " + entry.getValue().getRecordCount() + ")}")
                .collect(Collectors.joining(", "));
        String columnsDetails = comparedTableColumns.stream()
                .map(ComparedTableColumn::toString)
                .collect(Collectors.joining("\n  ", "\n  ", "")); // Indent for readability
        return "ComparedTable{perSourceTable=[" + sourceTableDetails + "], columns=[" + columnsDetails + "\n]}";
    }
}
