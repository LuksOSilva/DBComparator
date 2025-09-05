package com.luksosilva.dbcomparator.model.live.comparison.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.luksosilva.dbcomparator.enums.ConfigKeys;

public class Config {

    private ConfigKeys configKey;
    private String description;
    private String configValue;

    public Config() { }

    public Config(ConfigKeys configKey, String description, String configValue) {
        this.configKey = configKey;
        this.description = description;
        this.configValue = configValue;
    }

    public ConfigKeys getConfigKey() {
        return configKey;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public String getConfigValue() {
        return configValue;
    }

    @JsonIgnore
    public boolean isEnabled() {
        return "Y".equalsIgnoreCase(configValue);
    }


}
