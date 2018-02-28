package ru.leymooo.cfg.configuration.file;

/**
 * Various settings for controlling the input and output of a {@link
 * ru.leymooo.cfg.configuration.file.YamlConfiguration}
 */
public class YamlConfigurationOptions extends FileConfigurationOptions {
    private int indent = 2;

    protected YamlConfigurationOptions(final YamlConfiguration configuration) {
        super(configuration);
    }

    @Override
    public YamlConfiguration configuration() {
        return (YamlConfiguration) super.configuration();
    }

    @Override
    public ru.leymooo.cfg.configuration.file.YamlConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public ru.leymooo.cfg.configuration.file.YamlConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public ru.leymooo.cfg.configuration.file.YamlConfigurationOptions header(final String value) {
        super.header(value);
        return this;
    }

    @Override
    public ru.leymooo.cfg.configuration.file.YamlConfigurationOptions copyHeader(final boolean value) {
        super.copyHeader(value);
        return this;
    }

    /**
     * Gets how much spaces should be used to indent each line.
     * <p>
     * The minimum value this may be is 2, and the maximum is 9.
     *
     * @return How much to indent by
     */
    public int indent() {
        return indent;
    }

    /**
     * Sets how much spaces should be used to indent each line.
     * <p>
     * The minimum value this may be is 2, and the maximum is 9.
     *
     * @param value New indent
     * @return This object, for chaining
     */
    public ru.leymooo.cfg.configuration.file.YamlConfigurationOptions indent(final int value) {
        if (value < 2) {
            throw new IllegalArgumentException("Indent must be at least 2 characters");
        }
        if (value > 9) {
            throw new IllegalArgumentException("Indent cannot be greater than 9 characters");
        }

        indent = value;
        return this;
    }
}
