package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ComparisonQueryExecutor {

    public static Stream<Map<String, Object>> streamQueryDifferences(
            Map<String, String> sourcesInfo, String sql) throws SQLException {

        Connection connection = SQLiteUtils.getDataSource().getConnection();
        attachAllSources(connection, sourcesInfo);

        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);

        Iterator<Map<String, Object>> iterator = getIterator(resultSet);

        Spliterator<Map<String, Object>> spliterator =
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);


        Stream<Map<String, Object>> stream = StreamSupport.stream(spliterator, false);

        return stream.onClose(() -> {
            try {
                resultSet.close();
            } catch (Exception ignored) {}
            try {
                stmt.close();
            } catch (Exception ignored) {}
            try {
                detachAllSources(connection, sourcesInfo);
            } catch (Exception ignored) {}
            try {
                connection.close();
            } catch (Exception ignored) {}
        });
    }

    private static Iterator<Map<String, Object>> getIterator(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        Iterator<Map<String, Object>> iterator = new Iterator<>() {
            boolean hasNext = advance();

            private boolean advance() {
                try {
                    return resultSet.next();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Map<String, Object> next() {
                if (!hasNext) {
                    throw new NoSuchElementException();
                }
                try {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                    }
                    hasNext = advance();
                    return row;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return iterator;
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
