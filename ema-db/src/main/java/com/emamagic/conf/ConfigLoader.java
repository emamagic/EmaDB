package com.emamagic.conf;

import com.emamagic.annotation.Config;
import com.emamagic.exception.ConfigNotFoundException;
import com.emamagic.exception.InstanceOfUtilityClassException;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class ConfigLoader {

    private ConfigLoader() {
        throw new InstanceOfUtilityClassException(getClass().getSimpleName());
    }

    public static Map<DB, ConfigData> loadConfigs() throws ConfigNotFoundException {
        Map<DB, ConfigData> configMap = new HashMap<>();
        ServiceLoader<IConfig> loader = ServiceLoader.load(IConfig.class);
        int configCount = 0;
        for (IConfig config : loader) {
            configCount++;
            Class<?> configClass = config.getClass();
            if (configClass.isAnnotationPresent(Config.class)) {
                Config cfg = configClass.getAnnotation(Config.class);
                var db = cfg.db();
                configMap.put(db, new ConfigData(config, db));
            }
        }

        if (configCount != 0 && configCount == configMap.size()) return configMap;
        throw new ConfigNotFoundException("Configuration not found for database");
    }


}