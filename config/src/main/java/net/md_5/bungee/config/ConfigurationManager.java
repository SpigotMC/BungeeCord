package net.md_5.bungee.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class ConfigurationManager {

    // this is a singleton, to support older bungeecord versions
    private static ConfigurationManager instance;

    public ConfigurationManager() {
        if(instance == null)
            instance = this;
    }



    /**
     *
     * @return the instance of this class
     */
    @Deprecated
    static ConfigurationManager getInstance() {
        return instance;
    }

    /**
     * Returns a registered provider by class
     * @param clazz The class of the object
     * @return The object of the configuration class
     */
    public ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor = clazz.getConstructor();
        return (ConfigurationProvider) constructor.newInstance();
    }

}
