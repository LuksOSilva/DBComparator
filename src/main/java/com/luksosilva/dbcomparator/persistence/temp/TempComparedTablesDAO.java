package com.luksosilva.dbcomparator.persistence.temp;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.util.SQLiteUtils;

import java.sql.Connection;
import java.sql.Statement;

public class TempComparedTablesDAO {

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
