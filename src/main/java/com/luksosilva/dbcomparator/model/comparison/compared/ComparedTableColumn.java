package com.luksosilva.dbcomparator.model.comparison.compared;

import com.luksosilva.dbcomparator.model.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComparedTableColumn {

    private final ComparedTable comparedTable;
    private final Map<ComparedSource, SourceTableColumn> perSourceTableColumn;
    private ColumnSettings columnSettings;

    private final List<ColumnFilter> columnFilters = new ArrayList<>();

    public ComparedTableColumn(ComparedTable comparedTable, Map<ComparedSource, SourceTableColumn> perSourceTableColumn) {
        this.comparedTable = comparedTable;
        this.perSourceTableColumn = perSourceTableColumn;
    }

    public void setColumnSetting(ColumnSettings columnSettings) {
        this.columnSettings = columnSettings;
    }

    public ComparedTable getComparedTable() {
        return comparedTable;
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


    public List<ColumnFilter> getColumnFilters() {
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

    public boolean hasFilter() {
        return !getColumnFilters().isEmpty();
    }




}
