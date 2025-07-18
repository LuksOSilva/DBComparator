package com.luksosilva.dbcomparator.model.comparison;

import com.luksosilva.dbcomparator.enums.ColumnFilterType;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComparedTableColumn {

    private final Map<ComparedSource, SourceTableColumn> perSourceTableColumn;
    private ColumnSettings columnSettings;

    private final List<ColumnFilter> columnFilters = new ArrayList<>();

    public ComparedTableColumn(Map<ComparedSource, SourceTableColumn> perSourceTableColumn) {
        this.perSourceTableColumn = perSourceTableColumn;
    }

    public void setColumnSetting(ColumnSettings columnSettings) {
        this.columnSettings = columnSettings;
    }

    public String getColumnName() {
        return getPerSourceTableColumn().values().stream()
                .findFirst()
                .map(SourceTableColumn::getColumnName)
                .orElse(null);
    }

    public List<String> getColumnTypes() {
        return perSourceTableColumn.values().stream()
                .map(SourceTableColumn::getType)
                .map(String::toUpperCase)
                .map(s -> s.replaceAll("\\s*\\(.*\\)$", ""))
                .distinct()
                .toList();
    }

    public Map<ComparedSource, SourceTableColumn> getPerSourceTableColumn() {
        return perSourceTableColumn;
    }



    public ColumnSettings getColumnSetting() {
        return columnSettings;
    }


    public List<ColumnFilter> getColumnFilter() {
        return columnFilters;
    }

    public boolean hasColumnSetting() {
        return columnSettings != null;
    }

    public void removeColumnSetting() {
        columnSettings = null;
    }

    public void addColumnFilter(ColumnFilter filter) {
        columnFilters.add(filter);
    }
    public void addColumnFilter(List<ColumnFilter> filters) {
        columnFilters.addAll(filters);
    }

    public boolean hasSameType() {
        return perSourceTableColumn.values().stream()
                .map(SourceTableColumn::getType)
                .distinct()
                .count() <= 1;
    }


}
