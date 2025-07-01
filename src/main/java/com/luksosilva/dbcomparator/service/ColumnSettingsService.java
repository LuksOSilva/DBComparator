package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.ComparedTableColumnSettings;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ColumnSettingsService {


    public static ComparedTableColumnSettings getColumnSettings
            (ComparedTable comparedTable,
             ComparedTableColumn comparedTableColumn,
             Optional<Map<ComparedTable, Map<ComparedTableColumn, ComparedTableColumnSettings>>> optionalPerComparedTableColumnSetting) {


        List<ComparedSource> comparedSourceList = new ArrayList<>();
        comparedTable.getPerSourceTable().forEach((comparedSource, sourceTable) ->
                comparedSourceList.add(comparedSource));

        boolean existsInAllSources = getExistsInAllSources(comparedTableColumn, comparedSourceList);
        //1. If column doesn't exist in all sources, it is neither identifier nor comparable.
        if (!existsInAllSources) {
            return new ComparedTableColumnSettings(false, false);
        }

        boolean tableHasPrimaryKey = comparedTable.getPerSourceTable().values().stream()
                .flatMap(sourceTable -> sourceTable.getSourceTableColumns().stream())
                .anyMatch(SourceTableColumn::isPk);

        boolean isPkInAnySource = getIsPkInAnySource(comparedTableColumn);
        boolean isPkInAllSources = getIsPkInAllSources(comparedTableColumn);



        return optionalPerComparedTableColumnSetting
                .map(map -> map.getOrDefault(comparedTable, Map.of()).get(comparedTableColumn))
                .orElseGet(() -> getDefaultColumnSettings(
                        tableHasPrimaryKey, isPkInAnySource, isPkInAllSources
                ));
    }


    //

    private static ComparedTableColumnSettings getDefaultColumnSettings(boolean tableHasPrimaryKey, boolean isPkInAnySource, boolean isPkInAllSources) {
        boolean isIdentifier;
        boolean isComparable;

        //2. If table doesn't have any PK in any sources, all columns are identifiers.
        if (!tableHasPrimaryKey) {
            isIdentifier = true;
            isComparable = false;
        }
        //3. If column is PK in at least 1 source and not in the others, it is an identifier.
        else if (isPkInAnySource && !isPkInAllSources) {
            isIdentifier = true;
            isComparable = false;
        }
        //4. If column exists in all sources and table has primary keys, identifier if its PK else comparable.
        else {
            isIdentifier = isPkInAnySource;
            isComparable = !isPkInAnySource;
        }


        return new ComparedTableColumnSettings(isComparable, isIdentifier);
    }



    private static boolean getExistsInAllSources(ComparedTableColumn comparedTableColumn, List<ComparedSource> comparedSourceList) {
        return comparedTableColumn.getPerSourceTableColumn().size() == comparedSourceList.size();
    }

    private static boolean getIsPkInAnySource(ComparedTableColumn comparedTableColumn) {
        List<SourceTableColumn> sourceTableColumnList =
                comparedTableColumn.getPerSourceTableColumn().values().stream().toList();

        return sourceTableColumnList.stream().anyMatch(SourceTableColumn::isPk);
    }

    private static boolean getIsPkInAllSources(ComparedTableColumn comparedTableColumn) {
        List<SourceTableColumn> sourceTableColumnList =
                comparedTableColumn.getPerSourceTableColumn().values().stream().toList();

        return sourceTableColumnList.stream().allMatch(SourceTableColumn::isPk);
    }


}
