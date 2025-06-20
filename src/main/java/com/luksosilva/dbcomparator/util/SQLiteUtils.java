package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.model.comparison.ComparedSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

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
