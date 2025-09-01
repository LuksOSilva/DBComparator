package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnSettingsDAO {

    public static Optional<Map<ComparedTable, Map<ComparedTableColumn, ColumnSettings>>> loadTableColumnsSettingsFromDb(List<ComparedTable> comparedTableList) {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String listTableNames = comparedTableList.stream()
                    .map(ComparedTable -> "\"" + ComparedTable.getTableName() + "\"")
                    .collect(Collectors.joining(", "));

            try (Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(
                         SqlFormatter.buildSelectMapColumnSettings(listTableNames))
            ) {

                Map<ComparedTable, Map<ComparedTableColumn, ColumnSettings>> perComparedTableColumnSetting = new HashMap<>();

                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    String columnName = resultSet.getString("COLUMN_NAME");

                    ComparedTable comparedTable = comparedTableList.stream()
                            .filter(ComparedTable -> ComparedTable.getTableName().equals(tableName))
                            .findFirst()
                            .orElse(null);

                    if (comparedTable == null) continue;

                    ComparedTableColumn comparedTableColumn = comparedTable.getComparedTableColumns().stream()
                            .filter(ComparedTableColumn -> ComparedTableColumn.getColumnName().equals(columnName))
                            .findFirst()
                            .orElse(null);

                    if (comparedTableColumn == null) continue;

                    //converts string to boolean
                    boolean isComparable = resultSet.getString("IS_COMPARABLE").equals("Y");
                    boolean isIdentifier = resultSet.getString("IS_IDENTIFIER").equals("Y");

                    ColumnSettings columnSettings = new ColumnSettings(isComparable, isIdentifier);


                    perComparedTableColumnSetting
                            .computeIfAbsent(comparedTable, k -> new HashMap<>())
                            .put(comparedTableColumn, columnSettings);

                }

                return perComparedTableColumnSetting.isEmpty() ?
                        Optional.empty() : Optional.of(perComparedTableColumnSetting);

            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveTableColumnsSettings(ComparedTable comparedTable, ComparedTableColumn comparedTableColumn) {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SqlFormatter.buildReplaceColumnSettings(
                    comparedTable.getTableName(),
                    comparedTableColumn.getColumnName(),
                    comparedTableColumn.getColumnSetting().isComparable(),
                    comparedTableColumn.getColumnSetting().isIdentifier());


            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> selectValidateColumnSettings(String sourceId, String sourcePath, String tableName, List<String> identifierColumns) {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            List<String> results = new ArrayList<>();

            String sql = SqlFormatter.buildSelectValidateIdentifiers(sourceId, tableName, identifierColumns);

            SQLiteUtils.attachSource(connection, sourcePath, sourceId);

            try (Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(sql)
            ) {

                while (resultSet.next()) {

                    results.add(resultSet.getString("source_id"));

                }

            }

            SQLiteUtils.detachSource(connection, sourceId);

            return results;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
