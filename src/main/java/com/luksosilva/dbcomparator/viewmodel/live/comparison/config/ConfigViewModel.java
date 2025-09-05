package com.luksosilva.dbcomparator.viewmodel.live.comparison.config;

import com.luksosilva.dbcomparator.model.live.comparison.config.Config;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class ConfigViewModel {

    private Config model;
    private final SimpleStringProperty configKey = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final SimpleBooleanProperty configValue = new SimpleBooleanProperty();

    public ConfigViewModel() { }

    public ConfigViewModel(Config model) {
        this.model = model;
        this.configKey.set(model.getConfigKey().toString());
        this.description.set(model.getDescription());
        this.configValue.set(model.isEnabled());

    }

    public Config getModel() {
        return model;
    }

    public String getConfigKey() {
        return configKey.get();
    }

    public SimpleStringProperty configKeyProperty() {
        return configKey;
    }

    public String getDescription() {
        return description.get() == null ? "" : description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public boolean isConfigValue() {
        return configValue.get();
    }

    public SimpleBooleanProperty configValueProperty() {
        return configValue;
    }



    ///

    public void setConfigValue(boolean newConfigValue) {
        configValue.set(newConfigValue);
    }

    public boolean hasChanged() {
        return configValue.get() != model.isEnabled();
    }

    public void commit() {
        model.setConfigValue(this.configValue.get() ? "Y" : "N");
    }
}
