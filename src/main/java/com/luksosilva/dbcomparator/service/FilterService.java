package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.builder.FilterSqlBuilder;
import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.Filter;
import com.luksosilva.dbcomparator.model.live.comparison.customization.TableFilter;
import com.luksosilva.dbcomparator.model.live.comparison.customization.validation.FilterValidationResult;
import com.luksosilva.dbcomparator.persistence.FilterValidator;
import com.luksosilva.dbcomparator.util.FileUtils;


import java.util.List;
import java.util.Map;

public class FilterService {


    public static void validateFilters(List<ComparedTable> comparedTableList, List<ComparedSource> comparedSourceList) {

        for (ComparedTable comparedTable : comparedTableList) {

            if (comparedTable.getFilterValidationResult().isValid()) {
                continue; // skip if already valid
            }

            if (comparedTable.getFilter() == null &&
                    comparedTable.getComparedTableColumns().stream()
                        .allMatch(comparedColumn -> comparedColumn.getColumnFilters().isEmpty())) {

                comparedTable.setFilterValidationResult(new FilterValidationResult(FilterValidationResultType.VALID));
                continue;
            }

            for (String sourceId : comparedTable.getPerSourceTable().keySet()) {

                ComparedSource comparedSource = comparedSourceList.stream().filter(cs -> cs.getSourceId().equals(sourceId)).findFirst().orElse(null);
                if (comparedSource == null) continue;

                String filterSql = FilterSqlBuilder.build(comparedTable, comparedSource);

                // Run the test query
                FilterValidationResult result = FilterValidator.selectValidateFilter(
                        sourceId,
                        FileUtils.getCanonicalPath(comparedSource.getSource().getPath()),
                        comparedTable.getTableName(),
                        filterSql
                );

                comparedTable.setFilterValidationResult(result);
                if (comparedTable.getFilterValidationResult().isInvalid()) { //stops validating if its invalid in current source
                    break;
                }
            }
        }


    }



    public static void applyFilters(List<Filter> filters) {

        for (Filter filter : filters) {
            if (filter instanceof TableFilter tableFilter) { //if new filter is TableFilter, delete any ColumnFilter.

                ComparedTable comparedTable = tableFilter.getComparedTable();

                boolean hasColumnFilters = comparedTable.getComparedTableColumns().stream()
                        .anyMatch(tableColumn -> !tableColumn.getColumnFilters().isEmpty());
                if (hasColumnFilters) {
                    comparedTable.getComparedTableColumns().forEach(col -> col.getColumnFilters().clear());
                }

            } else if (filter instanceof ColumnFilter columnFilter) { //if new filter is ColumnFilter, delete any TableFilter

                ComparedTableColumn comparedTableColumn = columnFilter.getComparedTableColumn();
                if (comparedTableColumn.getComparedTable().getFilter() != null) {
                    comparedTableColumn.getComparedTable().setFilter(null);
                }

            }

            clearValidation(filter);
        }


        filters.forEach(Filter::apply);
    }

    public static void editFilter(Map<Filter, Filter> perNewFilterOldFilter) {

        perNewFilterOldFilter.forEach(((newFilter, oldFilter) -> {

            clearValidation(newFilter);

            // Remove old filter
            if (oldFilter instanceof TableFilter tableFilter) {
                ComparedTable oldTable = tableFilter.getComparedTable();
                oldTable.removeFilter();

            } else if (oldFilter instanceof ColumnFilter columnFilter) {
                ComparedTableColumn oldColumn = columnFilter.getComparedTableColumn();
                oldColumn.getColumnFilters().remove(columnFilter);
            }

            // Add new filter
            if (newFilter instanceof TableFilter tableFilter) {
                ComparedTable newTable = tableFilter.getComparedTable();
                newTable.setFilter(tableFilter);

                //removes all column filters if any
                newTable.getComparedTableColumns().forEach(col -> col.getColumnFilters().clear());

            } else if (newFilter instanceof ColumnFilter columnFilter) {
                ComparedTableColumn newColumn = columnFilter.getComparedTableColumn();
                newColumn.addColumnFilter(columnFilter);
            }

        }));
    }

    public static void deleteFilter(Filter filter) {


        if (filter instanceof TableFilter tableFilter) {

            tableFilter.getComparedTable().setFilter(null);

        } else if (filter  instanceof ColumnFilter columnFilter) {

            columnFilter.getComparedTableColumn().getColumnFilters().remove(filter);

        }

        clearValidation(filter);
    }


    private static void clearValidation(Filter filter) {
        ComparedTable comparedTable = filter.getComparedTable();
        comparedTable.clearFilterValidation();
    }


}
