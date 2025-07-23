package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.comparison.ColumnFilter;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;


import java.util.List;
import java.util.Map;

public class ColumnFilterService {

    public static void addFilter(Map<ComparedTableColumn, List<ColumnFilter>> perComparedTableColumnFilter) {
        perComparedTableColumnFilter.forEach(ComparedTableColumn::addColumnFilter);
    }

    public static void editFilter(Map<ComparedTableColumn, Map<ColumnFilter, ColumnFilter>> perComparedTableColumnFilter) {

        perComparedTableColumnFilter.forEach(((comparedTableColumn, mapOfColumnFilter) -> {

            mapOfColumnFilter.forEach((oldColumnFilter, newColumnFilter) -> {

                comparedTableColumn.getColumnFilter().remove(oldColumnFilter);
                comparedTableColumn.addColumnFilter(newColumnFilter);

            });

        }));
    }

    public static void deleteFilter(ComparedTableColumn comparedTableColumn, ColumnFilter columnFilter) {
        comparedTableColumn.getColumnFilter().remove(columnFilter);
    }

}
