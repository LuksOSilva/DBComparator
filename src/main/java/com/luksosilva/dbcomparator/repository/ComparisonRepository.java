package com.luksosilva.dbcomparator.repository;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.util.SqlFormatter;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComparisonRepository {

    public static String getNextComparisonId()  {

        try (Connection connection = SQLiteUtils.getDataSource().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(
                     SQLiteUtils.loadSQL(SqlFiles.SELECT_NEXT_COMPARISON_ID))
        ) {
            if (resultSet.next()) {
                return resultSet.getString("NEXT_COMPARISON_ID");
            }
        } catch (Exception e) {
            System.out.println("Failed to retrieve next comparison ID: " + e);
        }
        // default
        return "0001";
    }

    public static List<Map<String, Object>> executeQueryDifferences(Map<String, String> sourcesInfo, String sql) {

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()){

            attachAllSources(connection, sourcesInfo);

            try (Statement stmt = connection.createStatement();
                ResultSet resultSet = stmt.executeQuery(sql)
            ) {

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = resultSet.getObject(i);
                        row.put(columnName, value);
                    }

                    results.add(row);
                }
            }

            detachAllSources(connection, sourcesInfo);

            return results;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void attachAllSources(Connection conn, Map<String, String> sourceInfo) {

        for (Map.Entry<String, String> entry : sourceInfo.entrySet()) {
            String sourceId = entry.getKey();
            String filePath = entry.getValue();

            SQLiteUtils.attachSource(conn, filePath, sourceId);
        }

    }
    private static void detachAllSources(Connection conn, Map<String, String> sourceInfo) {

        for (Map.Entry<String, String> entry : sourceInfo.entrySet()) {
            String sourceId = entry.getKey();

            SQLiteUtils.detachSource(conn, sourceId);
        }

    }

}
