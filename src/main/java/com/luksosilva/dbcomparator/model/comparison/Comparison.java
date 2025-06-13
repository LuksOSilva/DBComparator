package com.luksosilva.dbcomparator.model.comparison;

import com.luksosilva.dbcomparator.model.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.model.enums.ComparisonStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Comparison {\n");
        sb.append("  comparisonId='").append(comparisonId).append("',\n");
        sb.append("  description='").append(description).append("',\n");
        sb.append("  status=").append(status).append(",\n");
        sb.append("  createdAt=").append(createdAt).append(",\n");

        sb.append("  comparedSources=[\n");
        for (ComparedSource cs : comparedSources) {
            sb.append("    ").append(cs.toString()).append(",\n");
        }
        sb.append("  ],\n");

        sb.append("  comparedTables=[\n");
        for (ComparedTable ct : comparedTables) {
            sb.append("    ").append(ct.toString()).append(",\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}
