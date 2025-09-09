package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.model.live.comparison.customization.validation.FilterValidationResult;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FilterValidator {

    public static FilterValidationResult selectValidateFilter(String sourceId, String sourcePath, String tableName, String filterSql) {

        FilterValidationResult filterValidationResult;

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SqlFormatter.buildSelectValidateFilter(sourceId, tableName, filterSql);

            SQLiteUtils.attachSource(connection, sourcePath, sourceId);


            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                if (!rs.next()) {
                    filterValidationResult = new FilterValidationResult(FilterValidationResultType.NO_RECORDS_FOUND, sourceId);
                } else {
                    filterValidationResult = new FilterValidationResult(FilterValidationResultType.VALID);
                }


            } catch (SQLException e) {
                filterValidationResult = new FilterValidationResult(FilterValidationResultType.INVALID_SYNTAX, sourceId, e.getMessage());
            }

            SQLiteUtils.detachSource(connection, sourceId);

            return  filterValidationResult;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
