package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.enums.ConfigKeys;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import com.luksosilva.dbcomparator.util.SqlFormatter;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaLoader {


    public static void mapSourceTable(Source source, ConfigRegistry configRegistry) {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            SQLiteUtils.attachSource(connection, source);

            loadTableNames(connection, source, configRegistry);
            loadRecordCounts(connection, source);
            loadTableColumns(connection, source);

            SQLiteUtils.detachSource(connection, source.getId());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //

    private static void loadTableNames(Connection conn, Source source, ConfigRegistry configRegistry) throws Exception {
        boolean considerSqliteTables = configRegistry.getConfigValueOf(ConfigKeys.DBC_CONSIDER_SQLITE_TABLES);

        try (Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(
                     SqlFormatter.buildPragmaTableList(source.getId()))
        ) {
            while (resultSet.next()) {

                String tableName = resultSet.getString("name");

                if (!considerSqliteTables && tableName.startsWith("sqlite")) continue;

                source.getSourceTables().add(new SourceTable(tableName));

            }
        }
    }

    private static void loadRecordCounts(Connection conn, Source source) throws Exception {
        for (SourceTable sourceTable : source.getSourceTables()) {

            try (Statement stmt = conn.createStatement();
                 ResultSet resultSet = stmt.executeQuery(
                         SqlFormatter.buildSelectTableRecordCount(source.getId(), sourceTable.getTableName()))
            ) {
                while (resultSet.next()) {

                    int recordCount = resultSet.getInt("RECORD_COUNT");

                    sourceTable.setRecordCount(recordCount);

                }
            }

        }
    }

    private static void loadTableColumns(Connection conn, Source source) throws Exception {
        for (SourceTable sourceTable : source.getSourceTables()) {

            try (Statement stmt = conn.createStatement();
                 ResultSet resultSet = stmt.executeQuery(
                         SqlFormatter.buildPragmaTableInfo(source.getId(), sourceTable.getTableName()))
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
