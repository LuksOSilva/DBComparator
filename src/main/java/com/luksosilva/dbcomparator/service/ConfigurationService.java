package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.live.comparison.config.Config;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.persistence.ConfigurationDAO;

import java.security.PublicKey;
import java.util.List;

public class ConfigurationService {

    public static ConfigRegistry getConfigRegistry() throws Exception {
        return ConfigurationDAO.loadConfigs();
    }

    public static void saveConfigurations(List<Config> alteredConfigs) throws Exception {
        for (Config config : alteredConfigs) {
            ConfigurationDAO.updateConfig(config);
        }
    }

}
