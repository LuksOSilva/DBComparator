package com.luksosilva.dbcomparator.model.live.comparison.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.luksosilva.dbcomparator.enums.ConfigKeys;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedSource;
import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.result.ComparisonResult;

import java.util.List;

public class ConfigRegistry {

    private List<Config> configs;

    public ConfigRegistry() { }

    public ConfigRegistry(List<Config> configs) {
        this.configs = configs;
    }


    public List<Config> getConfigs() {
        return configs;
    }

    public boolean getConfigValueOf(ConfigKeys configKey) {
        Config config = configs.stream()
                .filter(cc -> cc.getConfigKey().equals(configKey))
                .findFirst()
                .orElse(null);

        if (config != null) {
            return config.isEnabled();
        }

        return configKey.getDefaultValue();
    }
}
