package com.luksosilva.dbcomparator.persistence.temp;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.service.SourceService;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TempComparedTablesDAO {

    public static List<ComparedTableColumn> selectComparedColumnsFromSources() throws Exception {

        List<ComparedTableColumn> comparedTableColumns = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_COLUMNS_FROM_SOURCES);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {
                int counter = 1;
                while (rs.next()) {

                    int codComparedTable = rs.getInt("COD_COMPARED_TABLE");
                    String columnName = rs.getString("COLUMN_NAME");
                    boolean hasSchemaDifference = rs.getBoolean("HAS_SCHEMA_DIFFERENCE");
                    boolean existsOnAllSources = rs.getBoolean("EXISTS_ON_ALL_SOURCES");

                    ComparedTableColumn comparedTableColumn = new ComparedTableColumn(
                            counter, codComparedTable, columnName, hasSchemaDifference, existsOnAllSources
                    );

                    comparedTableColumns.add(comparedTableColumn);

                    counter++;
                }
            }
        }

        return comparedTableColumns;
    }

    public static List<ComparedTable> selectComparedTableFromSources() throws Exception {

        List<SourceTable> sourceTables = SourceService.getSourceTables();

        List<ComparedTable> comparedTables = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_TABLES_FROM_SOURCES);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {
                int counter = 1;
                while (rs.next()) {

                    String tableName = rs.getString("TABLE_NAME");
                    boolean hasRecordCountDifference = rs.getBoolean("HAS_RECORD_COUNT_DIFFERENCE");
                    boolean hasSchemaDifference = rs.getBoolean("HAS_SCHEMA_DIFFERENCE");

                    List<SourceTable> sourceTablesOfComparedTable = sourceTables.stream()
                            .filter(sourceTable -> sourceTable.getTableName().equals(tableName))
                            .toList();

                    ComparedTable comparedTable = new ComparedTable(counter,
                            tableName, hasRecordCountDifference, hasSchemaDifference, sourceTablesOfComparedTable);

                    comparedTables.add(comparedTable);
                    counter++;
                }
            }
        }


        return comparedTables;
    }

    public static List<String> selectComparedTablesNames() throws Exception {

        List<String> comparedTablesNames = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_TABLES_NAMES);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {

                while (rs.next()) {
                    comparedTablesNames.add(rs.getString("TABLE_NAME"));
                }

            }
        }

        return comparedTablesNames;
    }

    public static void saveTempComparedTables(List<ComparedTable> comparedTables) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String sql = SQLiteUtils.loadSQL(SqlFiles.INSERT_TEMP_COMPARED_TABLES);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (ComparedTable comparedTable : comparedTables) {

                    ps.setInt(1, comparedTable.getCodComparedTable());
                    ps.setString(2, comparedTable.getTableName());
                    ps.setBoolean(3, comparedTable.hasRecordCountDifference());
                    ps.setBoolean(4, comparedTable.hasSchemaDifference());

                    ps.addBatch();

                }

                ps.executeBatch();

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw new Exception("Falha ao salvar tabelas: " + e.getMessage());
            }

        }
    }

    public static void saveTempComparedColumns(List<ComparedTable> comparedTables) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            String sql = SQLiteUtils.loadSQL(SqlFiles.INSERT_TEMP_COMPARED_COLUMNS);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (ComparedTable comparedTable : comparedTables) {
                    for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {

                        ps.setInt(1, comparedTableColumn.getCodComparedColumn());
                        ps.setInt(2, comparedTableColumn.getCodComparedTable());
                        ps.setString(3, comparedTableColumn.getColumnName());
                        ps.setBoolean(4, comparedTableColumn.hasSchemaDifference());
                        ps.setBoolean(5, comparedTableColumn.existsOnAllSources());

                        ps.addBatch();
                    }
                }

                ps.executeBatch();
            }
        }
    }

    public static void deleteTempComparedTables(List<String> comparedTablesNames) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String deleteComparedTablesSql = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_COMPARED_TABLES);
            String deleteComparedTableColumnsSql = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_COMPARED_TABLE_COLUMNS);
            String deleteComparedTableColumnConfigsSql = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_COMPARED_TABLE_COLUMN_CONFIGS);
            String deleteComparedTableColumnFiltersSql = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_COMPARED_TABLE_COLUMN_FILTERS);
            String deleteComparedTableFilters = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_COMPARED_TABLE_FILTERS);

            try (
                    PreparedStatement psComparedTables = connection.prepareStatement(deleteComparedTablesSql);
                    PreparedStatement psComparedColumns = connection.prepareStatement(deleteComparedTableColumnsSql);
                    PreparedStatement psColumnConfigs = connection.prepareStatement(deleteComparedTableColumnConfigsSql);
                    PreparedStatement psColumnFilters = connection.prepareStatement(deleteComparedTableColumnFiltersSql);
                    PreparedStatement psComparedTableFilters = connection.prepareStatement(deleteComparedTableFilters)
            ) {
                for (String tableName : comparedTablesNames) {

                    psComparedTables.setString(1, tableName);
                    psComparedTables.addBatch();

                    psComparedColumns.setString(1, tableName);
                    psComparedColumns.addBatch();

                    psColumnConfigs.setString(1, tableName);
                    psColumnConfigs.addBatch();

                    psColumnFilters.setString(1, tableName);
                    psColumnFilters.addBatch();

                    psComparedTableFilters.setString(1, tableName);
                    psComparedTableFilters.addBatch();

                }

                psComparedTables.executeBatch();
                psComparedColumns.executeBatch();
                psColumnConfigs.executeBatch();
                psColumnFilters.executeBatch();
                psComparedTableFilters.executeBatch();

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new Exception("Falha ao deletar tabelas", e);
            }
        }
    }

    public static void clearTables() throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.CLEAR_TEMP_COMPARED_TABLES);
            String[] statements = sql.split(";");

            try (Statement statement = connection.createStatement()) {

                for (String s : statements) {
                    statement.execute(s);
                }

            }

        }
    }



}
