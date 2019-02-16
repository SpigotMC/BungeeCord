package net.md_5.bungee.config;

import java.util.HashMap;
import java.util.Map;

public final class ConfigurationManager {

    private static ConfigurationManager instance;

    /**
     * Gets the instance of this class
     * @deprecated Use ProxyServer.getConfigurationManager
     * @return a instance of this class
     */
    @Deprecated
    public static ConfigurationManager getInstance(){
        return instance;
    }

    public ConfigurationManager(){
        if(instance == null)
            instance = this;

        this.configurationProviderMap.put(YamlConfiguration.class, new YamlConfiguration());
    }

    private final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> configurationProviderMap = new HashMap<>();

    /**
     * Registers a new configuration provider
     * @param clazz The class of the configuration provider
     * @param configurationProvider The object of the configuration provider
     */
    public void registerConfigurationProvider(Class<? extends ConfigurationProvider> clazz, ConfigurationProvider configurationProvider){
        configurationProviderMap.put(clazz, configurationProvider);
    }

    /**
     * Returns a registered provider by class
     * @param clazz The class of the object
     * @return The object of the configuration class
     */
    public ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> clazz) throws ConfigurationProviderNotRegisteredException {
        if(configurationProviderMap.containsKey(clazz))
            return configurationProviderMap.get(clazz);
        else
            throw new ConfigurationProviderNotRegisteredException();
    }

}
