package com.luksosilva.dbcomparator.repository;

import com.luksosilva.dbcomparator.exception.ComparisonException;
import com.luksosilva.dbcomparator.model.enums.SqlFiles;
import com.luksosilva.dbcomparator.util.SqlFormatter;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ComparisonRepository {

    public static String getNextComparisonId() {

        try (Connection connection = SQLiteUtils.getDataSource().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(
                     SqlFormatter.loadSQL(SqlFiles.SELECT_NEXT_COMPARISON_ID))
        ) {
            if (resultSet.next()) {
                return resultSet.getString("NEXT_COMPARISON_ID");
            }
        } catch (Exception e) {
            throw new ComparisonException("Failed to retrieve next comparison ID: ", e);
        }
        // default
        return "0001";
    }

}
