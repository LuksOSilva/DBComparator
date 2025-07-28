package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.customization.Filter;
import com.luksosilva.dbcomparator.model.comparison.customization.TableFilter;


import java.util.List;
import java.util.Map;

public class FilterService {

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
        }


        filters.forEach(Filter::apply);
    }

    public static void editFilter(Map<Filter, Filter> perNewFilterOldFilter) {

        perNewFilterOldFilter.forEach(((newFilter, oldFilter) -> {

            // Remove old filter
            if (oldFilter instanceof TableFilter) {
                ComparedTable oldTable = ((TableFilter) oldFilter).getComparedTable();
                oldTable.removeFilter();

            } else if (oldFilter instanceof ColumnFilter) {
                ComparedTableColumn oldColumn = ((ColumnFilter) oldFilter).getComparedTableColumn();
                oldColumn.getColumnFilters().remove((ColumnFilter) oldFilter);
            }

            // Add new filter
            if (newFilter instanceof TableFilter) {
                ComparedTable newTable = ((TableFilter) newFilter).getComparedTable();
                newTable.setFilter((TableFilter) newFilter);

                //removes all column filters if any
                newTable.getComparedTableColumns().forEach(col -> col.getColumnFilters().clear());

            } else if (newFilter instanceof ColumnFilter) {
                ComparedTableColumn newColumn = ((ColumnFilter) newFilter).getComparedTableColumn();
                newColumn.addColumnFilter((ColumnFilter) newFilter);
            }

        }));
    }

    public static void deleteFilter(Filter filter) {


        if (filter instanceof TableFilter) {
            ((TableFilter) filter).getComparedTable().setFilter(null);
            return;
        }
        ((ColumnFilter) filter).getComparedTableColumn().getColumnFilters().remove(filter);

    }


}
