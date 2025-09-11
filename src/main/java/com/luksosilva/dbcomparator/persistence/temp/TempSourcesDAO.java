package com.luksosilva.dbcomparator.persistence.temp;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class TempSourcesDAO {

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
