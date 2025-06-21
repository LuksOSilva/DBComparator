package com.luksosilva.dbcomparator.model.comparison.result;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;

import java.util.Map;
import java.util.stream.Collectors;

public class DifferingColumn {

    private ComparedTableColumn comparedTableColumn;
    private Map<ComparedSource, Object> perSourceValue;


    public DifferingColumn(ComparedTableColumn comparedTableColumn, Map<ComparedSource, Object> perSourceValue) {
        this.comparedTableColumn = comparedTableColumn;
        this.perSourceValue = perSourceValue;
    }


    public String getColumnName() {
        return comparedTableColumn.getColumnName();
    }


    public Map<String, Object> getPerSourceValue() {

        return perSourceValue.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getSourceId(),
                        Map.Entry::getValue
                ));
    }
}
