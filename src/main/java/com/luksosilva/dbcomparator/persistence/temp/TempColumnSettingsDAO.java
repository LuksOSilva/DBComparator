package com.luksosilva.dbcomparator.persistence.temp;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTableColumn;
import com.luksosilva.dbcomparator.model.live.comparison.customization.ColumnConfig;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TempColumnSettingsDAO {

    public static List<ColumnConfig> selectConfigOfColumns(List<ComparedTableColumn> comparedTableColumnList) throws Exception {

        List<ColumnConfig> columnConfigs = new ArrayList<>();

        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String placeholders = comparedTableColumnList.stream()
                    .map(t -> "?")
                    .collect(Collectors.joining(", "));

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_TEMP_COMPARED_COLUMN_CONFIGS)
                    .formatted(placeholders);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                int index = 1;
                for (ComparedTableColumn comparedTableColumn : comparedTableColumnList) {
                    ps.setInt(index++, comparedTableColumn.getCodComparedColumn());
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ColumnConfig columnConfig = new ColumnConfig(
                                rs.getInt("COD_COMPARED_COLUMN"),
                                rs.getBoolean("IS_IDENTIFIER"),
                                rs.getBoolean("IS_COMPARABLE")
                        );
                        columnConfigs.add(columnConfig);
                    }
                }

            }
        }

        return columnConfigs;
    }

    public static void saveTableColumnsSettings(List<ComparedTable> comparedTableList) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            String sql = SQLiteUtils.loadSQL(SqlFiles.REPLACE_TEMP_COMPARED_COLUMN_CONFIGS);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (ComparedTable comparedTable : comparedTableList) {
                    for (ComparedTableColumn comparedTableColumn : comparedTable.getComparedTableColumns()) {

                        ps.setInt(1, comparedTableColumn.getCodComparedColumn());
                        ps.setBoolean(2, comparedTableColumn.getColumnSetting().isIdentifier());
                        ps.setBoolean(3, comparedTableColumn.getColumnSetting().isComparable());

                        ps.addBatch();
                    }
                }

                ps.executeBatch();
                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public static void clearTables() throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.CLEAR_TEMP_COMPARED_COLUMN_CONFIGS);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.execute();

            }

        }
    }

}
