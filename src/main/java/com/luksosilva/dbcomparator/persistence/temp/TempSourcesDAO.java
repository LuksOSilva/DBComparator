package com.luksosilva.dbcomparator.persistence.temp;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempSourcesDAO {

    public static List<Source> selectSources() throws Exception {

        List<Source> sources = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_SOURCES);

            try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)
            ) {
                while (rs.next()) {

                    String sourceId = rs.getString("SOURCE_ID");
                    int sequence = rs.getInt("SEQUENCE");
                    File file = new File(rs.getString("SOURCE_PATH"));

                    sources.add(new Source(sourceId, sequence, file));
                }
            }
        }

        return sources;
    }

    public static List<SourceTableColumn> selectSourceColumns() throws Exception {

        List<SourceTableColumn> sourceTableColumns = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_SOURCE_COLUMNS);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {
                while (rs.next()) {

                    String sourceId = rs.getString("SOURCE_ID");
                    String tableName = rs.getString("TABLE_NAME");
                    int sequence = rs.getInt("SEQUENCE");
                    String columnName = rs.getString("COLUMN_NAME");
                    String type = rs.getString("TYPE");
                    boolean isNotNull = rs.getBoolean("NOT_NULL");
                    boolean isPK = rs.getBoolean("IS_PK");

                    SourceTableColumn sourceTableColumn = new SourceTableColumn(
                            sourceId,tableName,sequence,columnName,type,isNotNull,isPK
                    );

                    sourceTableColumns.add(sourceTableColumn);

                }
            }

        }

        return sourceTableColumns;
    }

    public static List<File> selectSourcesFiles() throws Exception {

        List<File> sourcesFile = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_SOURCES_FILES);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ){
                while (rs.next()) {

                    File file = new File(rs.getString("SOURCE_PATH"));
                    sourcesFile.add(file);
                }
            }

        }

        return sourcesFile;
    }

    public static List<SourceTable> selectSourceTables() throws Exception {

        List<SourceTable> sourceTables = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_SOURCE_TABLES);

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)
            ) {
                while (rs.next()) {

                    String sourceId = rs.getString("SOURCE_ID");
                    String tableName = rs.getString("TABLE_NAME");
                    int recordCount = rs.getInt("RECORD_COUNT");

                    SourceTable sourceTable = new SourceTable(sourceId, tableName, recordCount);

                    sourceTables.add(sourceTable);
                }
            }

        }

        return sourceTables;
    }

    public static void clearTables() throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.CLEAR_TEMP_SOURCES);
            String[] statements = sql.split(";");

            try (Statement statement = connection.createStatement()) {

                for (String s : statements) {
                    statement.execute(s);
                }
            }
        }
    }

    public static void saveTempSources(List<Source> sourceList) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try {
                saveTempSource(connection, sourceList);
                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public static void deleteTempSources(List<Source> sourceList) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String deleteSourcesSql = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_SOURCES);
            String deleteTablesSql = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_SOURCE_TABLES);
            String deleteColumnsSql = SQLiteUtils.loadSQL(SqlFiles.DELETE_TEMP_SOURCE_TABLE_COLUMNS);

            try (
                    PreparedStatement psSources = connection.prepareStatement(deleteSourcesSql);
                    PreparedStatement psTables = connection.prepareStatement(deleteTablesSql);
                    PreparedStatement psColumns = connection.prepareStatement(deleteColumnsSql)
            ) {
                for (Source source : sourceList) {
                    String id = source.getId();

                    psSources.setString(1, id);
                    psSources.addBatch();

                    psTables.setString(1, id);
                    psTables.addBatch();

                    psColumns.setString(1, id);
                    psColumns.addBatch();
                }

                psColumns.executeBatch();
                psTables.executeBatch();
                psSources.executeBatch();

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    ///

    private static void saveTempSource(Connection conn, List<Source> sources) throws Exception {

        String sql = SQLiteUtils.loadSQL(SqlFiles.INSERT_TEMP_SOURCE);

        try(PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Source source : sources) {

                ps.setString(1, source.getId());
                ps.setInt(2, source.getSequence());
                ps.setString(3, source.getFile().getCanonicalPath());

                ps.addBatch();

                saveTempSourceTable(conn, source.getId(), source.getSourceTables());
            }

            ps.executeBatch();
        }
    }

    private static void saveTempSourceTable(Connection conn,
                                            String sourceId,
                                            List<SourceTable> sourceTables) throws Exception {

        String sql = SQLiteUtils.loadSQL(SqlFiles.INSERT_TEMP_SOURCE_TABLE);

        try(PreparedStatement ps = conn.prepareStatement(sql)) {

            for (SourceTable sourceTable : sourceTables) {

                ps.setString(1, sourceId);
                ps.setString(2, sourceTable.getTableName());
                ps.setInt(3, sourceTable.getRecordCount());

                ps.addBatch();

                saveTempSourceTableColumn(conn, sourceId, sourceTable.getTableName(), sourceTable.getSourceTableColumns());
            }

            ps.executeBatch();
        }
    }

    private static void saveTempSourceTableColumn(Connection conn,
                                                  String sourceId,
                                                  String tableName,
                                                  List<SourceTableColumn> sourceTableColumns)throws Exception {

        String sql = SQLiteUtils.loadSQL(SqlFiles.INSERT_TEMP_SOURCE_TABLE_COLUMN);

        try(PreparedStatement ps = conn.prepareStatement(sql)) {

            for (SourceTableColumn sourceTableColumn : sourceTableColumns) {

                ps.setString(1, sourceId);
                ps.setString(2, tableName);
                ps.setInt(3, sourceTableColumn.getSequence());
                ps.setString(4, sourceTableColumn.getColumnName());
                ps.setString(5, sourceTableColumn.getType());
                ps.setBoolean(6, sourceTableColumn.isNotNull());
                ps.setBoolean(7, sourceTableColumn.isPk());

                ps.addBatch();
            }

            ps.executeBatch();
        }

    }


}
