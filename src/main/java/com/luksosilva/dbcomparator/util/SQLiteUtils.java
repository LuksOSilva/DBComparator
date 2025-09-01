package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class SQLiteUtils {


    public static DataSource getDataSource() {
        var dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:sqlite:database/DBCdatabase.db");
        dataSource.setMaximumPoolSize(1);
        return dataSource;

    }

    public static void runSql (Connection conn, String sql) {
        try (Statement statement = conn.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error while trying to run SQL: \n" + sql + "\n", e);
        }
    }

    public static String loadSQL(SqlFiles sqlFile) {
        try (InputStream inputStream = SqlFormatter.class.getClassLoader().getResourceAsStream(sqlFile.fullPath())) {
            if (inputStream == null) {
                throw new RuntimeException("SQL file not found: " + sqlFile.fullPath());
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not load SQL file: " + sqlFile.fullPath(), e);
        }
    }

    public static void attachSource(Connection conn, String filePath, String sourceId) {
        try {

            String query =
                    "ATTACH DATABASE '" + filePath +"' AS " + sourceId;
            runSql(conn, query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void detachSource(Connection conn, String sourceId) {
        try {

            String query =
                    "DETACH DATABASE " + sourceId;
            runSql(conn, query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void attachSource(Connection conn, ComparedSource comparedSource) {
        try {

            String query =
                    "ATTACH DATABASE '" +comparedSource.getSource().getPath().getCanonicalPath() +"' AS " + comparedSource.getSourceId();
            runSql(conn, query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void detachSource(Connection conn, ComparedSource comparedSource) {
        try {

            String query =
                    "DETACH DATABASE " + comparedSource.getSourceId();
            runSql(conn, query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
