package com.luksosilva.dbcomparator.model.comparison;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparedTableColumn {

    private Map<ComparedSource, SourceTableColumn> perSourceTableColumn;
    private ComparedTableColumnSettings comparedTableColumnSettings;

    private List<String> columnFilter = new ArrayList<>();

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
        return columnFilter;
    }





}
