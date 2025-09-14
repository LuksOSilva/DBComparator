package com.luksosilva.dbcomparator.model.live.comparison.compared;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "columnId"
)
@JsonIgnoreProperties({"columnFilters"})
public class ComparedTableColumn {
    /// OLD
    private String columnId;
    private ComparedTable comparedTable;
    private Map<String, SourceTableColumn> perSourceTableColumn;
    /// NEW
    private int codComparedColumn;
    private int codComparedTable;
    private String columnName;
    private boolean hasSchemaDifference;
    private boolean existsOnAllSources;


    private ColumnSettings columnSettings;
    private final List<ColumnFilter> columnFilters = new ArrayList<>();

    /// OLD
    public ComparedTableColumn() {}
    public ComparedTableColumn(ComparedTable comparedTable, Map<String, SourceTableColumn> perSourceTableColumn) {
        this.comparedTable = comparedTable;
        this.perSourceTableColumn = perSourceTableColumn;
        computeColumnId();
    }
    /// NEW
    public ComparedTableColumn(int codComparedColumn,
                               int codComparedTable,
                               String columnName,
                               boolean hasSchemaDifference,
                               boolean existsOnAllSources) {

        this.codComparedColumn = codComparedColumn;
        this.codComparedTable = codComparedTable;
        this.columnName = columnName;
        this.hasSchemaDifference = hasSchemaDifference;
        this.existsOnAllSources = existsOnAllSources;
    }

    public int getCodComparedColumn() {
        return codComparedColumn;
    }

    public int getCodComparedTable() {
        return codComparedTable;
    }

    public boolean hasSchemaDifference() {
        return hasSchemaDifference;
    }

    public boolean existsOnAllSources() {
        return existsOnAllSources;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public void setComparedTable(ComparedTable comparedTable) {
        this.comparedTable = comparedTable;
    }

    public void setColumnSettings(ColumnSettings columnSettings) {
        this.columnSettings = columnSettings;
    }

    public void setColumnSetting(ColumnSettings columnSettings) {
        this.columnSettings = columnSettings;
    }

    public ComparedTable getComparedTable() {
        return comparedTable;
    }

    public String getColumnId() {
        return columnId;
    }

//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
//    public String getColumnName() {
//        return getPerSourceTableColumn().values().stream()
//                .findFirst()
//                .map(SourceTableColumn::getColumnName)
//                .orElse(null);
//    }


    public String getColumnName() {
        return columnName;
    }

    private void computeColumnId() {
        this.columnId = comparedTable.getTableName() + "." + getColumnName();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<String> getColumnTypes() {
        if (perSourceTableColumn == null) return new ArrayList<>();

        return perSourceTableColumn.values().stream()
                .map(SourceTableColumn::getType)
                .map(String::toUpperCase)
                .map(s -> s.replaceAll("\\s*\\(.*\\)$", ""))
                .distinct()
                .toList();
    }

    public Map<String, SourceTableColumn> getPerSourceTableColumn() {
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
