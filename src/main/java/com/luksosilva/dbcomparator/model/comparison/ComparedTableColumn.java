package com.luksosilva.dbcomparator.model.comparison;

import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.Map;
import java.util.stream.Collectors;

public class ComparedTableColumn {

    private Map<ComparedSource, SourceTableColumn> perSourceTableColumn;
    private ComparedTableColumnSettings comparedTableColumnSettings;

    public ComparedTableColumn(Map<ComparedSource, SourceTableColumn> perSourceTableColumn) {
        this.perSourceTableColumn = perSourceTableColumn;
    }

    public void setColumnSetting(ComparedTableColumnSettings comparedTableColumnSettings) {
        this.comparedTableColumnSettings = comparedTableColumnSettings;
    }

    public Map<ComparedSource, SourceTableColumn> getPerSourceTableColumn() {
        return perSourceTableColumn;
    }

    public String getColumnName() {
        return getPerSourceTableColumn().values().stream()
                .findFirst()
                .map(SourceTableColumn::getColumnName)
                .orElse(null);
    }

    public ComparedTableColumnSettings getColumnSetting() {
        return comparedTableColumnSettings;
    }

    @Override
    public String toString() {
        String sourceColumnDetails = perSourceTableColumn.entrySet().stream()
                .map(entry -> "{" + entry.getKey().getSourceId() + ": " + entry.getValue().toString() + "}")
                .collect(Collectors.joining(", "));
        return "ComparedTableColumn{perSourceTableColumn=[" + sourceColumnDetails + "], columnSettings=" + comparedTableColumnSettings + "}";
    }
}
