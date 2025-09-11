package com.luksosilva.dbcomparator.model.live.comparison;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.comparison.result.ComparisonResult;
import com.luksosilva.dbcomparator.model.live.source.Source;


import java.util.ArrayList;
import java.util.List;


public class Comparison {

    private ConfigRegistry configRegistry;

    private List<Source> sources = new ArrayList<>();
    private List<ComparedTable> comparedTables = new ArrayList<>();


    private ComparisonResult comparisonResult;

    public Comparison() {}

    @JsonCreator
    public Comparison(
            @JsonProperty("configsRegistry") ConfigRegistry configsRegistry,
            @JsonProperty("comparedSources") List<Source> sources,
            @JsonProperty("comparedTables") List<ComparedTable> comparedTables,
            @JsonProperty("comparisonResult") ComparisonResult comparisonResult) {
        this.configRegistry = configsRegistry;
        this.sources = sources;
        this.comparedTables = comparedTables;
        this.comparisonResult = comparisonResult;
    }

    /**SUPER TEMP**/
    public List<ComparedSource> getComparedSources() {
        List<ComparedSource> list = new ArrayList<>();
        return list;
    }
    /**SUPER TEMP**/

    public List<Source> getSources() {
        return sources;
    }

    public List<ComparedTable> getComparedTables() {
        return comparedTables;
    }

    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }

    public ConfigRegistry getConfigRegistry() { return configRegistry; }

    public void setComparisonResult(ComparisonResult comparisonResult) {
        this.comparisonResult = comparisonResult;
    }

    public void setConfigRegistry(ConfigRegistry configRegistry) { this.configRegistry = configRegistry; }
}
