package com.luksosilva.dbcomparator.model.live.comparison.compared;

import com.fasterxml.jackson.annotation.*;
import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import com.luksosilva.dbcomparator.model.live.comparison.customization.TableFilter;
import com.luksosilva.dbcomparator.model.live.comparison.customization.validation.FilterValidationResult;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;

import java.util.*;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "tableName"
)
@JsonIgnoreProperties({
        "columnSettingsValidationResult",
        "filterValidationResult",
        "filter",
        "sqlUserFilter",
        "columnSettingsInvalid",
        "columnSettingsValid",
        "orderedComparedTableColumns",
        "totalRecordCount",
        "comparableComparedTableColumns",
        "identifierComparedTableColumns",
})
public class ComparedTable {

    private int codComparedTable;
    private String tableName;
    private boolean hasRecordCountDifference;
    private boolean hasSchemaDifference;

    private List<SourceTable> sourceTables = new ArrayList<>();
    private Map<String, SourceTable> perSourceTable;
    private List<ComparedTableColumn> comparedTableColumns = new ArrayList<>();

    private ColumnSettingsValidationResultType columnSettingsValidationResult = ColumnSettingsValidationResultType.NOT_VALIDATED;
    private FilterValidationResultType filterValidationResult = FilterValidationResultType.NOT_VALIDATED;


    private FilterValidationResult OLDfilterValidationResult = new FilterValidationResult(FilterValidationResultType.NOT_VALIDATED);

    private TableFilter filter;

    private String sqlSelectDifferences;

    public ComparedTable() {}

    public ComparedTable(int codComparedTable,
                         String tableName,
                         boolean hasRecordCountDifference,
                         boolean hasSchemaDifference,
                         ColumnSettingsValidationResultType columnValidation,
                         FilterValidationResultType filterValidation) {

        this.codComparedTable = codComparedTable;
        this.tableName = tableName;
        this.hasRecordCountDifference = hasRecordCountDifference;
        this.hasSchemaDifference = hasSchemaDifference;
        this.columnSettingsValidationResult = columnValidation;
        this.filterValidationResult = filterValidation;
    }

    public ComparedTable(int codComparedTable,
                         String tableName,
                         boolean hasRecordCountDifference,
                         boolean hasSchemaDifference) {

        this.codComparedTable = codComparedTable;
        this.tableName = tableName;
        this.hasRecordCountDifference = hasRecordCountDifference;
        this.hasSchemaDifference = hasSchemaDifference;
    }

    public ComparedTable(ComparedTable that) {
        this.codComparedTable = that.codComparedTable;
        this.tableName = that.tableName;
        this.hasRecordCountDifference = that.hasRecordCountDifference;
        this.hasSchemaDifference = that.hasSchemaDifference;
        this.sourceTables = that.sourceTables;
        this.perSourceTable = that.perSourceTable;
        this.comparedTableColumns = that.comparedTableColumns;
        this.columnSettingsValidationResult = that.columnSettingsValidationResult;
        this.filterValidationResult = that.filterValidationResult;
        this.filter = that.filter;
        this.sqlSelectDifferences = that.sqlSelectDifferences;
    }

    @JsonCreator
    public ComparedTable(
            @JsonProperty("perSourceTable") Map<String, SourceTable> perSourceTable,
            @JsonProperty("comparedTableColumns") List<ComparedTableColumn> comparedTableColumns,
            @JsonProperty("sqlSelectDifferences") String sqlSelectDifferences) {
        this.perSourceTable = perSourceTable;
        this.comparedTableColumns = comparedTableColumns;
        this.sqlSelectDifferences = sqlSelectDifferences;
    }

    public void setSourceTables(List<SourceTable> sourceTables) {
        this.sourceTables = sourceTables;
    }

    public String getTableName() {
        return tableName;
    }

    public int getCodComparedTable() {
        return codComparedTable;
    }

    public boolean hasRecordCountDifference() {
        return hasRecordCountDifference;
    }

    public boolean hasSchemaDifference() {
        return hasSchemaDifference;
    }

    public List<SourceTable> getSourceTables() {
        return sourceTables;
    }

    public List<ComparedTableColumn> getComparedTableColumns() {
        return comparedTableColumns;
    }

    public void setComparedTableColumns(List<ComparedTableColumn> comparedTableColumns) {
        this.comparedTableColumns = comparedTableColumns;
    }

    public List<ComparedTableColumn> getOrderedComparedTableColumns() {

        return comparedTableColumns.stream()
                .sorted((tableColumn1, tableColumn2) -> {

                    // Get minimum sequence for col1
                    int minSeq1 = tableColumn1.getPerSourceTableColumn().values().stream()
                            .mapToInt(SourceTableColumn::getSequence)
                            .min()
                            .orElse(Integer.MAX_VALUE);

                    // Get minimum sequence for col2
                    int minSeq2 = tableColumn2.getPerSourceTableColumn().values().stream()
                            .mapToInt(SourceTableColumn::getSequence)
                            .min()
                            .orElse(Integer.MAX_VALUE);

                    // Primary sort: by min sequence
                    int cmp = Integer.compare(minSeq1, minSeq2);
                    if (cmp != 0) return cmp;

                    // Secondary sort: by column name
                    return tableColumn1.getColumnName().compareTo(tableColumn2.getColumnName());
                })
                .toList();
    }


    public Map<String, SourceTable> getPerSourceTable() {
        return perSourceTable;
    }


    public String getSqlSelectDifferences() {
        return sqlSelectDifferences;
    }

    public void setSqlSelectDifferences(String sqlSelectDifferences) {
        this.sqlSelectDifferences = sqlSelectDifferences;
    }


    public TableFilter getFilter() {
        return filter;
    }

    public void setFilter(TableFilter filter) {
        this.filter = filter;
    }

    public void removeFilter() {
        this.filter = null;
    }


    public boolean hasTableFilter() {
        return getFilter() != null;
    }

    public boolean hasColumnFilter() {
        return getComparedTableColumns().stream().anyMatch(ComparedTableColumn::hasFilter);
    }

    public boolean hasFilter() {
        return hasTableFilter() || hasColumnFilter();
    }

    public int getTotalRecordCount() {
        int totalRecordCount = 0;

        for (SourceTable sourceTable : perSourceTable.values()) {
            totalRecordCount += sourceTable.getRecordCount();
        }

        return totalRecordCount;
    }

    public List<ComparedTableColumn> getComparableColumns() {
        return getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isComparable())
                .toList();
    }
    public List<ComparedTableColumn> getIdentifierColumns() {
        return getComparedTableColumns().stream()
                .filter(comparedTableColumn -> comparedTableColumn.getColumnSetting().isIdentifier())
                .toList();
    }


    /// COLUMN SETTINGS VALIDATION

    public ColumnSettingsValidationResultType getColumnSettingsValidationResult() {
        return columnSettingsValidationResult;
    }

    public void setColumnSettingsValidationResult(ColumnSettingsValidationResultType columnSettingsValidationResult) {
        this.columnSettingsValidationResult = columnSettingsValidationResult;
    }

    public void clearColumnSettingValidation() {
        columnSettingsValidationResult = ColumnSettingsValidationResultType.NOT_VALIDATED;
    }

    public boolean isColumnSettingsValid() {
        if (columnSettingsValidationResult == null) return false;

        return columnSettingsValidationResult == ColumnSettingsValidationResultType.VALID;
    }

    public boolean isColumnSettingsInvalid() {
        if (columnSettingsValidationResult == null) return false;

        return columnSettingsValidationResult != ColumnSettingsValidationResultType.VALID
                && columnSettingsValidationResult != ColumnSettingsValidationResultType.NOT_VALIDATED;
    }

    /// FILTER VALIDATION

    public FilterValidationResult getFilterValidationResult() {
        return OLDfilterValidationResult;
    }

    public void setFilterValidationResult(FilterValidationResult filterValidationResult) {
        this.OLDfilterValidationResult = filterValidationResult;
    }


    public void clearFilterValidation() {
        this.OLDfilterValidationResult = new FilterValidationResult(FilterValidationResultType.NOT_VALIDATED);
    }


}
