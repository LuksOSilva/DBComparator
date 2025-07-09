package com.luksosilva.dbcomparator.model.comparison;

import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComparedTableColumn {

    private Map<ComparedSource, SourceTableColumn> perSourceTableColumn;
    private ComparedTableColumnSettings comparedTableColumnSettings;

    private final List<String> columnFilters = new ArrayList<>();

    public ComparedTableColumn(Map<ComparedSource, SourceTableColumn> perSourceTableColumn) {
        this.perSourceTableColumn = perSourceTableColumn;
    }

    public void setColumnSetting(ComparedTableColumnSettings comparedTableColumnSettings) {
        this.comparedTableColumnSettings = comparedTableColumnSettings;
    }

    public String getColumnName() {
        return getPerSourceTableColumn().values().stream()
                .findFirst()
                .map(SourceTableColumn::getColumnName)
                .orElse(null);
    }


    public Map<ComparedSource, SourceTableColumn> getPerSourceTableColumn() {
        return perSourceTableColumn;
    }



    public ComparedTableColumnSettings getColumnSetting() {
        return comparedTableColumnSettings;
    }


    public List<String> getColumnFilter() {
        return columnFilters;
    }

    public boolean hasColumnSetting() {
        return comparedTableColumnSettings != null;
    }

    public void removeColumnSetting() {
        comparedTableColumnSettings = null;
    }

    public void addColumnFilter(String filter) {
        columnFilters.add(filter);
    }



}
