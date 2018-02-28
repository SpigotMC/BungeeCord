package ru.leymooo.cfg.configuration;

/**
 * Various settings for controlling the input and output of a {@link
 * ru.leymooo.cfg.configuration.MemoryConfiguration}
 */
public class MemoryConfigurationOptions extends ConfigurationOptions {
    protected MemoryConfigurationOptions(final MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public MemoryConfiguration configuration() {
        return (MemoryConfiguration) super.configuration();
    }

    @Override
    public ru.leymooo.cfg.configuration.MemoryConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public ru.leymooo.cfg.configuration.MemoryConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }
}
