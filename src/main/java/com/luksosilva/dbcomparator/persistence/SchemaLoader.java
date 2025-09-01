package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import com.luksosilva.dbcomparator.util.SqlFormatter;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaLoader {


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
