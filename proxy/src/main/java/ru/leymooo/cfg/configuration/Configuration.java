package ru.leymooo.cfg.configuration;

import java.util.Map;

/**
 * Represents a source of configurable options and settings
 */
public interface Configuration extends ConfigurationSection {
    /**
     * Sets the default value of the given path as provided.
     * <p>
     * If no source {@link ru.leymooo.cfg.configuration.Configuration} was provided as a default
     * collection, then a new {@link ru.leymooo.cfg.configuration.MemoryConfiguration} will be created to
     * hold the new default value.
     * <p>
     * If value is null, the value will be removed from the default
     * Configuration source.
     *
     * @param path  Path of the value to set.
     * @param value Value to set the default to.
     * @throws IllegalArgumentException Thrown if path is null.
     */
    @Override
    void addDefault(final String path, final Object value);

    /**
     * Sets the default values of the given paths as provided.
     * <p>
     * If no source {@link ru.leymooo.cfg.configuration.Configuration} was provided as a default
     * collection, then a new {@link ru.leymooo.cfg.configuration.MemoryConfiguration} will be created to
     * hold the new default values.
     *
     * @param defaults A map of Path->Values to add to defaults.
     * @throws IllegalArgumentException Thrown if defaults is null.
     */
    void addDefaults(final Map<String, Object> defaults);

    /**
     * Sets the default values of the given paths as provided.
     * <p>
     * If no source {@link ru.leymooo.cfg.configuration.Configuration} was provided as a default
     * collection, then a new {@link ru.leymooo.cfg.configuration.MemoryConfiguration} will be created to
     * hold the new default value.
     * <p>
     * This method will not hold a reference to the specified Configuration,
     * nor will it automatically update if that Configuration ever changes. If
     * you require this, you should set the default source with {@link
     * #setDefaults(ru.leymooo.botfilter.configuration.Configuration)}.
     *
     * @param defaults A configuration holding a list of defaults to copy.
     * @throws IllegalArgumentException Thrown if defaults is null or this.
     */
    void addDefaults(final ru.leymooo.cfg.configuration.Configuration defaults);

    /**
     * Sets the source of all default values for this {@link ru.leymooo.cfg.configuration.Configuration}.
     * <p>
     * If a previous source was set, or previous default values were defined,
     * then they will not be copied to the new source.
     *
     * @param defaults New source of default values for this configuration.
     * @throws IllegalArgumentException Thrown if defaults is null or this.
     */
    void setDefaults(final ru.leymooo.cfg.configuration.Configuration defaults);

    /**
     * Gets the source {@link ru.leymooo.cfg.configuration.Configuration} for this configuration.
     * <p>
     * If no configuration source was set, but default values were added, then
     * a {@link ru.leymooo.cfg.configuration.MemoryConfiguration} will be returned. If no source was set
     * and no defaults were set, then this method will return null.
     *
     * @return Configuration source for default values, or null if none exist.
     */
    ru.leymooo.cfg.configuration.Configuration getDefaults();

    /**
     * Gets the {@link ru.leymooo.cfg.configuration.ConfigurationOptions} for this {@link ru.leymooo.cfg.configuration.Configuration}.
     * <p>
     * All setters through this method are chainable.
     *
     * @return Options for this configuration
     */
    ConfigurationOptions options();
}
