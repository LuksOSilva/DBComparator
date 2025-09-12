package com.luksosilva.dbcomparator.persistence.temp;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.service.SourceService;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TempComparedTablesDAO {

    public static List<ComparedTable> selectComparedTableFromSources() throws Exception {

        List<ComparedTable> comparedTables = new ArrayList<>();

        List<SourceTable> sourceTables = SourceService.getSourceTables();

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
