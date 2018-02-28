package ru.leymooo.cfg.configuration;

import java.util.Map;

/**
 * This is a {@link ru.leymooo.cfg.configuration.Configuration} implementation that does not save or load
 * from any source, and stores all values in memory only.
 * This is useful for temporary Configurations for providing defaults.
 */
public class MemoryConfiguration extends MemorySection implements Configuration {
    protected Configuration defaults;
    protected MemoryConfigurationOptions options;

    /**
     * Creates an empty {@link ru.leymooo.cfg.configuration.MemoryConfiguration} with no default values.
     */
    public MemoryConfiguration() {
    }

    /**
     * Creates an empty {@link ru.leymooo.cfg.configuration.MemoryConfiguration} using the specified {@link
     * ru.leymooo.cfg.configuration.Configuration} as a source for all default values.
     *
     * @param defaults Default value provider
     * @throws IllegalArgumentException Thrown if defaults is null
     */
    public MemoryConfiguration(final Configuration defaults) {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(final String path, final Object value) {
        if (path == null) {
            throw new NullPointerException("Path may not be null");
        }
        if (defaults == null) {
            defaults = new ru.leymooo.cfg.configuration.MemoryConfiguration();
        }

        defaults.set(path, value);
    }

    @Override
    public void addDefaults(final Map<String, Object> defaults) {
        if (defaults == null) {
            throw new NullPointerException("Defaults may not be null");
        }

        for (final Map.Entry<String, Object> entry : defaults.entrySet()) {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addDefaults(final Configuration defaults) {
        if (defaults == null) {
            throw new NullPointerException("Defaults may not be null");
        }

        addDefaults(defaults.getValues(true));
    }

    @Override
    public void setDefaults(final Configuration defaults) {
        if (defaults == null) {
            throw new NullPointerException("Defaults may not be null");
        }

        this.defaults = defaults;
    }

    @Override
    public Configuration getDefaults() {
        return defaults;
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override
    public MemoryConfigurationOptions options() {
        if (options == null) {
            options = new MemoryConfigurationOptions(this);
        }

        return options;
    }
}
