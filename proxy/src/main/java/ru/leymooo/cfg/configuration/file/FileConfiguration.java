package ru.leymooo.cfg.configuration.file;

import ru.leymooo.cfg.configuration.Configuration;
import ru.leymooo.cfg.configuration.InvalidConfigurationException;
import ru.leymooo.cfg.configuration.MemoryConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * This is a base class for all File based implementations of {@link
 * ru.leymooo.cfg.configuration.Configuration}
 */
public abstract class FileConfiguration extends MemoryConfiguration {

    /**
     * Creates an empty {@link ru.leymooo.cfg.configuration.file.FileConfiguration} with no default values.
     */
    public FileConfiguration() {
    }

    /**
     * Creates an empty {@link ru.leymooo.cfg.configuration.file.FileConfiguration} using the specified {@link
     * ru.leymooo.cfg.configuration.Configuration} as a source for all default values.
     *
     * @param defaults Default value provider
     */
    public FileConfiguration(Configuration defaults) {
        super(defaults);
    }

    /**
     * Saves this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} to the specified location.
     * <p>
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten. If it cannot be overwritten or created, an
     * exception will be thrown.
     * <p>
     * This method will save using the system default encoding, or possibly
     * using UTF8.
     *
     * @param file File to save to.
     * @throws java.io.IOException      Thrown when the given file cannot be written to for
     *                                  any reason.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void save(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }
        file.getParentFile().mkdirs();

        String data = saveToString();

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(data);
        }
    }

    /**
     * Saves this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} to the specified location.
     * <p>
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten. If it cannot be overwritten or created, an
     * exception will be thrown.
     * <p>
     * This method will save using the system default encoding, or possibly
     * using UTF8.
     *
     * @param file File to save to.
     * @throws java.io.IOException      Thrown when the given file cannot be written to for
     *                                  any reason.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void save(String file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        save(new File(file));
    }

    /**
     * Saves this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} to a string, and returns it.
     *
     * @return String containing this configuration.
     */
    public abstract String saveToString();

    /**
     * Loads this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} from the specified location.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given file.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be
     * thrown.
     * <p>
     *
     * @param file File to load from.
     * @throws java.io.FileNotFoundException                               Thrown when the given file cannot be
     *                                                                     opened.
     * @throws java.io.IOException                                         Thrown when the given file cannot be read.
     * @throws ru.leymooo.cfg.configuration.InvalidConfigurationException Thrown when the given file is not
     *                                                                     a valid Configuration.
     * @throws IllegalArgumentException                                    Thrown when file is null.
     */
    public void load(File file) throws IOException, InvalidConfigurationException {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        FileInputStream stream = new FileInputStream(file);

        load(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Loads this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} from the specified reader.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given stream.
     *
     * @param reader the reader to load from
     * @throws java.io.IOException                                         thrown when underlying reader throws an IOException
     * @throws ru.leymooo.cfg.configuration.InvalidConfigurationException thrown when the reader does not
     *                                                                     represent a valid Configuration
     * @throws IllegalArgumentException                                    thrown when reader is null
     */
    public void load(Reader reader) throws IOException, InvalidConfigurationException {

        StringBuilder builder = new StringBuilder();

        try (BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            String line;

            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        }

        loadFromString(builder.toString());
    }

    /**
     * Loads this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} from the specified location.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given file.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be
     * thrown.
     *
     * @param file File to load from.
     * @throws java.io.FileNotFoundException                               Thrown when the given file cannot be
     *                                                                     opened.
     * @throws java.io.IOException                                         Thrown when the given file cannot be read.
     * @throws ru.leymooo.cfg.configuration.InvalidConfigurationException Thrown when the given file is not
     *                                                                     a valid Configuration.
     * @throws IllegalArgumentException                                    Thrown when file is null.
     */
    public void load(String file) throws IOException, InvalidConfigurationException {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        load(new File(file));
    }

    /**
     * Loads this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} from the specified string, as
     * opposed to from file.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given string.
     * <p>
     * If the string is invalid in any way, an exception will be thrown.
     *
     * @param contents Contents of a Configuration to load.
     * @throws ru.leymooo.cfg.configuration.InvalidConfigurationException Thrown if the specified string is
     *                                                                     invalid.
     * @throws IllegalArgumentException                                    Thrown if contents is null.
     */
    public abstract void loadFromString(String contents) throws InvalidConfigurationException;

    /**
     * Compiles the header for this {@link ru.leymooo.cfg.configuration.file.FileConfiguration} and returns the
     * result.
     * <p>
     * This will use the header from {@link #options()} -> {@link
     * ru.leymooo.botfilter.configuration.file.FileConfigurationOptions#header()}, respecting the rules of {@link
     * ru.leymooo.botfilter.configuration.file.FileConfigurationOptions#copyHeader()} if set.
     *
     * @return Compiled header
     */
    protected abstract String buildHeader();

    @Override
    public FileConfigurationOptions options() {
        if (this.options == null) {
            this.options = new FileConfigurationOptions(this);
        }

        return (FileConfigurationOptions) this.options;
    }
}
