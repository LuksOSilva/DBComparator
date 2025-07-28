package com.luksosilva.dbcomparator.repository;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.comparison.customization.ColumnSettings;
import com.luksosilva.dbcomparator.model.source.SourceTable;
import com.luksosilva.dbcomparator.model.source.SourceTableColumn;
import com.luksosilva.dbcomparator.util.SqlFormatter;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class SchemaRepository {


    public static void mapSourceTable(ComparedSource comparedSource) {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            SQLiteUtils.attachSource(connection, comparedSource);

            loadTableNames(connection, comparedSource);
            loadRecordCounts(connection, comparedSource);
            loadTableColumns(connection, comparedSource);

            SQLiteUtils.detachSource(connection, comparedSource);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public static List<String> selectValidateIdentifiers(String sourceId, String filePath, String tableName, List<String> identifierColumns) {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            List<String> results = new ArrayList<>();

            String sql = SqlFormatter.buildSelectValidateIdentifiers(sourceId, tableName, identifierColumns);

            SQLiteUtils.attachSource(connection, filePath, sourceId);

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




    //

    private static void loadTableNames(Connection conn, ComparedSource comparedSource) throws Exception {
        try (Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(
                     SqlFormatter.buildPragmaTableList(comparedSource.getSourceId()))
        ) {
            while (resultSet.next()) {

                String tableName = resultSet.getString("name");

                comparedSource.getSource().getSourceTables().add(new SourceTable(tableName));

            }
        }
    }
    private static void loadRecordCounts(Connection conn, ComparedSource comparedSource) throws Exception {
        for (SourceTable sourceTable : comparedSource.getSource().getSourceTables()) {

            try (Statement stmt = conn.createStatement();
                 ResultSet resultSet = stmt.executeQuery(
                         SqlFormatter.buildSelectTableRecordCount(comparedSource.getSourceId(), sourceTable.getTableName()))
            ) {
                while (resultSet.next()) {

                    int recordCount = resultSet.getInt("RECORD_COUNT");

                    sourceTable.setRecordCount(recordCount);

                }
            }

        }
    }

    private static void loadTableColumns(Connection conn, ComparedSource comparedSource) throws Exception {
        for (SourceTable sourceTable : comparedSource.getSource().getSourceTables()) {

            try (Statement stmt = conn.createStatement();
                 ResultSet resultSet = stmt.executeQuery(
                         SqlFormatter.buildPragmaTableInfo(comparedSource.getSourceId(), sourceTable.getTableName()))
            ) {
                while (resultSet.next()) {

                    int sequence = resultSet.getInt("cid");
                    String columnName = resultSet.getString("name");
                    String type = resultSet.getString("type");
                    boolean notNull = resultSet.getInt("notnull") > 0;
                    boolean isPk = resultSet.getInt("pk") > 0;

                    SourceTableColumn sourceTableColumn =
                            new SourceTableColumn(sequence, columnName, type, notNull, isPk);


                    sourceTable.getSourceTableColumns().add(sourceTableColumn);

                }
            }
        }
    }




}
