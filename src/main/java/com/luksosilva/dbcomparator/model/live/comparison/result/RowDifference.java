package com.luksosilva.dbcomparator.model.live.comparison.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RowDifference {

    private List<IdentifierColumn> identifierColumns = new ArrayList<>();
    private List<ComparableColumn> comparableColumns = new ArrayList<>();
    private Set<String> existsOnSources = new HashSet<>();

    public RowDifference() {}

    @JsonCreator
    public RowDifference(
            @JsonProperty("identifierColumns") List<IdentifierColumn> identifierColumns,
            @JsonProperty("comparableColumns") List<ComparableColumn> comparableColumns,
            @JsonProperty("existsOnSources") Set<String> existsOnSources) {
        this.identifierColumns = identifierColumns;
        this.existsOnSources = existsOnSources;
        if (comparableColumns != null) {
            this.comparableColumns = comparableColumns;
        }
    }

    public List<IdentifierColumn> getIdentifierColumns() {
        return identifierColumns;
    }

    public List<ComparableColumn> getComparableColumns() {
        return comparableColumns;
    }

    public Set<String> getExistsOnSources() {
        return existsOnSources;
    }


    public void addIdentifierColumn(IdentifierColumn identifierColumn) {
        this.identifierColumns.add(identifierColumn);
    }
    public void addAllDifferingColumns(List<ComparableColumn> comparableColumnList) {
        this.comparableColumns.addAll(comparableColumnList);
    }
    public void addExistsOnSource(String sourceId) {
        existsOnSources.add(sourceId);
    }


}
