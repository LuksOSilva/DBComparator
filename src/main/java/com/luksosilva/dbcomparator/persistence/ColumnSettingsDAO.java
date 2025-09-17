package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnConfig;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnSettingsDAO {


    public static void saveTableColumnsSettings(ComparedTable comparedTable) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String sql = SQLiteUtils.loadSQL(SqlFiles.REPLACE_COLUMN_DEFAULTS);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {

                    ps.setString(1, comparedTable.getTableName());
                    ps.setString(2, comparedTableColumn.getColumnName());
                    ps.setBoolean(3, comparedTableColumn.getColumnSetting().isIdentifier());
                    ps.setBoolean(4, comparedTableColumn.getColumnSetting().isComparable());

                    ps.addBatch();
                }

                ps.executeBatch();
                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }



}
