package com.luksosilva.dbcomparator.viewmodel.live.comparison.config;

import com.luksosilva.dbcomparator.enums.ConfigKeys;
import com.luksosilva.dbcomparator.model.live.comparison.config.Config;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;

import java.util.List;

public class ConfigRegistryViewModel {

    private ConfigRegistry model;
    private List<ConfigViewModel> configViewModels;

    public ConfigRegistryViewModel() { }

    public ConfigRegistryViewModel(ConfigRegistry model) {
        this.model = model;
    }

    public void setConfigViewModels(List<ConfigViewModel> configViewModels) {
        this.configViewModels = configViewModels;
    }

    public List<ConfigViewModel> getConfigViewModels() {
        return configViewModels;
    }

    public ConfigRegistry getModel() {
        return model;
    }
}
