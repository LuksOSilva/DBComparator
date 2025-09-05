package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.enums.ConfigKeys;
import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.comparison.config.Config;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.SqlFormatter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationDAO {


    public static ConfigRegistry loadConfigs() throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SQLiteUtils.loadSQL(SqlFiles.SELECT_DBC_CONFIGS);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                List<Config> configs = new ArrayList<>();

                while (rs.next()) {

                    ConfigKeys configKey;
                    try {
                         configKey = ConfigKeys.valueOf(rs.getString("CONFIG_KEY"));
                    } catch (Exception e) {
                        continue;
                    }


                    configs.add(new Config(
                            configKey,
                            rs.getString("DESCRIPTION"),
                            rs.getString("CONFIG_VALUE")
                            ));
                }

                return new ConfigRegistry(configs);
            }
        }
    }

    public static void updateConfig(Config config) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SqlFormatter.buildUpdateDBCComparisonConfig(
                    config.getConfigKey().toString(), config.getConfigValue());

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }

        }
    }

}
