package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.enums.FilterValidationResultType;
import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.validation.FilterValidationResult;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ColumnSettingsValidator {

    public static boolean selectValidateColumnSettings(Source source, ComparedTable comparedTable) {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String identifiersList = SQLiteUtils.getSqlList(comparedTable.getIdentifierColumns(), ComparedTableColumn::getColumnName);

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_VALIDATE_IDENTIFIERS)
                    .formatted(source.getId(), comparedTable.getTableName(), identifiersList);

            SQLiteUtils.attachSource(connection, source);

            try (Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(sql)
            ) {
                while (resultSet.next()) {
                    return false; //not valid
                }
            }

            SQLiteUtils.detachSource(connection, source);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true; //valid
    }

}
