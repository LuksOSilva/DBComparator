package com.luksosilva.dbcomparator.model.comparison;

import com.luksosilva.dbcomparator.model.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.model.enums.ComparisonStatus;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Comparison {

    private String comparisonId;
    private String description;
    private LocalDateTime createdAt;

    private ComparisonStatus status;

    private final List<ComparedSource> comparedSources = new ArrayList<>();
    private final List<ComparedTable> comparedTables = new ArrayList<>();

    private ComparisonResult comparisonResult;

    public Comparison(String comparisonId) {
        this.comparisonId = comparisonId;
    }

    public List<ComparedSource> getComparedSources() {
        return comparedSources;
    }

    public List<ComparedTable> getComparedTables() {
        return comparedTables;
    }

    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }

    public void setComparisonResult(ComparisonResult comparisonResult) {
        this.comparisonResult = comparisonResult;
    }

}
