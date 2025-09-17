package com.luksosilva.dbcomparator.persistence.temp;

import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.service.SourceService;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TempComparedTablesDAO {

    public static List<ComparedTable> selectComparedTables() throws Exception {

        List<ComparedTable> comparedTableList = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_TABLES);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {
                while (rs.next()) {

                    int codComparedTable = rs.getInt("COD_COMPARED_TABLE");
                    String tableName = rs.getString("TABLE_NAME");
                    boolean hasRecordCountDifference = rs.getBoolean("HAS_RECORD_COUNT_DIFFERENCE");
                    boolean hasSchemaDifference = rs.getBoolean("HAS_SCHEMA_DIFFERENCE");
                    ColumnSettingsValidationResultType columnValidation = ColumnSettingsValidationResultType.valueOf(rs.getString("COLUMN_CONFIGS_VALIDATION"));
                    FilterValidationResultType filterValidation = FilterValidationResultType.valueOf(rs.getString("FILTER_VALIDATION"));



                    ComparedTable comparedTable = new ComparedTable(
                            codComparedTable, tableName, hasRecordCountDifference, hasSchemaDifference, columnValidation, filterValidation
                    );

                    comparedTableList.add(comparedTable);

                }
            }

        }

        return comparedTableList;
    }

    public static List<ComparedTableColumn> selectComparedColumnsOfTables(List<ComparedTable> comparedTables) throws Exception {

        List<ComparedTableColumn> comparedTableColumnList = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String placeholders = comparedTables.stream()
                    .map(t -> "?")
                    .collect(Collectors.joining(", "));

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_TABLE_COLUMNS)
                    .formatted(placeholders);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                int index = 1;
                for (ComparedTable comparedTable : comparedTables) {
                    ps.setInt(index++, comparedTable.getCodComparedTable());
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ComparedTableColumn column = new ComparedTableColumn(
                                rs.getInt("COD_COMPARED_COLUMN"),
                                rs.getInt("COD_COMPARED_TABLE"),
                                rs.getString("COLUMN_NAME"),
                                rs.getBoolean("IS_PK_ANY_SOURCE"),
                                rs.getBoolean("HAS_SCHEMA_DIFFERENCE"),
                                rs.getBoolean("EXISTS_ON_ALL_SOURCES")
                        );
                        comparedTableColumnList.add(column);
                    }
                }
            }
        }

        return comparedTableColumnList;
    }


    public static void computeComparedColumns(List<ComparedTable> comparedTableList) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String sql = SQLiteUtils.loadSQL(SqlFiles.PROCESS_TEMP_COMPARED_COLUMNS);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (ComparedTable comparedTable : comparedTableList) {

                    ps.setInt(1, comparedTable.getCodComparedTable());
                    ps.addBatch();

                }

                ps.executeBatch();
                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw new Exception(e.getMessage());
            }
        }
    }




    public static List<ComparedTable> selectComparedTableFromSources() throws Exception {

        List<SourceTable> sourceTables = SourceService.getSourceTables();

        List<ComparedTable> comparedTables = new ArrayList<>();



        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_TABLES_FROM_SOURCES);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {

                int lastUsedCod = selectLastCodComparedTable();

                while (rs.next()) {
                    lastUsedCod++;

                    String tableName = rs.getString("TABLE_NAME");
                    boolean hasRecordCountDifference = rs.getBoolean("HAS_RECORD_COUNT_DIFFERENCE");
                    boolean hasSchemaDifference = rs.getBoolean("HAS_SCHEMA_DIFFERENCE");

                    ComparedTable comparedTable = new ComparedTable(lastUsedCod,
                            tableName, hasRecordCountDifference, hasSchemaDifference);

                    List<SourceTable> sourceTablesOfComparedTable = sourceTables.stream()
                            .filter(sourceTable -> sourceTable.getTableName().equals(tableName))
                            .toList();

                    comparedTable.setSourceTables(sourceTablesOfComparedTable);


                    comparedTables.add(comparedTable);

                }
            }
        }


        return comparedTables;
    }

    private static int selectLastCodComparedTable() throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_TABLES_MAX_COD);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {

                while (rs.next()) {
                    return rs.getInt("LAST_USED_COD");
                }

            }

        }
        return 0;
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

    public static void computeTempComparedColumnConfigs(boolean prioritizeUserColumnSettings,
                                                        List<ComparedTable> comparedTables) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String sql = SQLiteUtils.loadSQL(SqlFiles.PROCESS_TEMP_COMPARED_COLUMN_CONFIGS);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (ComparedTable comparedTable : comparedTables) {

                    ps.setBoolean(1, prioritizeUserColumnSettings);
                    ps.setString(2, comparedTable.getTableName());

                    ps.addBatch();
                }

                ps.executeBatch();
                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

        }
    }

    public static void updateTempComparedTableColumnValidation(List<ComparedTable> comparedTableList) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String sql = SQLiteUtils.loadSQL(SqlFiles.UPDATE_TEMP_COMPARED_TABLE_COLUMN_CONFIG_VALIDATION);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (ComparedTable comparedTable : comparedTableList) {

                    String validationResult = comparedTable.getColumnSettingsValidationResult().toString();

                    ps.setString(1, validationResult);
                    ps.setInt(2, comparedTable.getCodComparedTable());

                    ps.addBatch();
                }

                ps.executeBatch();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

            connection.commit();
        }
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
                        ps.setBoolean(4, comparedTableColumn.isPkAnySource());
                        ps.setBoolean(5, comparedTableColumn.hasSchemaDifference());
                        ps.setBoolean(6, comparedTableColumn.existsOnAllSources());

                        ps.addBatch();
                    }
                }
                System.out.println("iniciando execução de batch");
                ps.executeBatch();
                System.out.println("execução finalizada");
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

                    psComparedColumns.addBatch();

                    psColumnConfigs.addBatch();

                    psColumnFilters.addBatch();

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
                throw new Exception("Falha ao deletar tabelas: " + e.getMessage());
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
