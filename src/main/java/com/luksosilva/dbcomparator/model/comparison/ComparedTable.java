package com.luksosilva.dbcomparator.model.comparison;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.luksosilva.dbcomparator.model.source.SourceTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparedTable {

    private Map<ComparedSource, SourceTable> perSourceTable;

    private List<ComparedTableColumn> comparedTableColumns = new ArrayList<>();

    private String queryDifferences;


    public ComparedTable(Map<ComparedSource, SourceTable> perSourceTable) {
        this.perSourceTable = perSourceTable;
    }


    public String getTableName() {
        return getPerSourceTable().values().stream()
                .findFirst()
                .map(SourceTable::getTableName)
                .orElse(null);
    }

    @JsonIgnore
    public List<ComparedTableColumn> getComparedTableColumns() {
        return comparedTableColumns;
    }

    @JsonIgnore
    public Map<ComparedSource, SourceTable> getPerSourceTable() {
        return perSourceTable;
    }

    @JsonIgnore
    public String getQueryDifferences() {
        return queryDifferences;
    }

    public void setQueryDifferences(String queryDifferences) {
        this.queryDifferences = queryDifferences;
    }




}
