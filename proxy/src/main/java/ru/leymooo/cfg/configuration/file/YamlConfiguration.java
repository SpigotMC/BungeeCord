package ru.leymooo.cfg.configuration.file;

import ru.leymooo.cfg.configuration.Configuration;
import ru.leymooo.cfg.configuration.ConfigurationSection;
import ru.leymooo.cfg.configuration.InvalidConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

/**
 * An implementation of {@link ru.leymooo.cfg.configuration.Configuration} which saves all files in Yaml.
 * Note that this implementation is not synchronized.
 */
public class YamlConfiguration extends FileConfiguration {
    protected static final String COMMENT_PREFIX = "# ";
    protected static final String BLANK_CONFIG = "{}\n";
    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    /**
     * Creates a new {@link ru.leymooo.cfg.configuration.file.YamlConfiguration}, loading from the given file.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
     * <p>
     * The encoding used may follow the system dependent default.
     *
     * @param file Input file
     * @return Resulting configuration
     * @throws IllegalArgumentException Thrown if file is null
     */
    public static ru.leymooo.cfg.configuration.file.YamlConfiguration loadConfiguration(final File file) {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        final ru.leymooo.cfg.configuration.file.YamlConfiguration config = new ru.leymooo.cfg.configuration.file.YamlConfiguration();

        try {
            config.load(file);
        } catch (InvalidConfigurationException | IOException ex) {
            try {
                file.getAbsolutePath();
                File dest = new File(file.getAbsolutePath() + "_broken");
                int i = 0;
                while (dest.exists()) {
                    dest = new File(file.getAbsolutePath() + "_broken_" + i++);
                }
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("&dCould not read: &7" + file);
                System.out.println("&dRenamed to: &7" + dest.getName());
                System.out.println("&c============ Full stacktrace ============");
                ex.printStackTrace();
                System.out.println("&c=========================================");
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return config;
    }

    /**
     * Creates a new {@link ru.leymooo.cfg.configuration.file.YamlConfiguration}, loading from the given reader.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
     *
     * @param reader input
     * @return resulting configuration
     * @throws IllegalArgumentException Thrown if stream is null
     */
    public static ru.leymooo.cfg.configuration.file.YamlConfiguration loadConfiguration(final Reader reader) {
        if (reader == null) {
            throw new NullPointerException("Reader cannot be null");
        }

        final ru.leymooo.cfg.configuration.file.YamlConfiguration config = new ru.leymooo.cfg.configuration.file.YamlConfiguration();

        try {
            config.load(reader);
        } catch (final IOException | InvalidConfigurationException ex) {
            System.out.println("Cannot load configuration from stream");
            ex.printStackTrace();
        }

        return config;
    }

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final String header = buildHeader();
        String dump = yaml.dump(getValues(false));

        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return header + dump;
    }

    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        if (contents == null) {
            throw new NullPointerException("Contents cannot be null");
        }

        Map<?, ?> input;
        try {
            input = (Map<?, ?>) yaml.load(contents);
        } catch (final YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (final ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        final String header = parseHeader(contents);
        if (!header.isEmpty()) {
            options().header(header);
        }

        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    protected void convertMapsToSections(final Map<?, ?> input, final ConfigurationSection section) {
        for (final Map.Entry<?, ?> entry : input.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    protected String parseHeader(final String input) {
        final String[] lines = input.split("\r?\n", -1);
        final StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        for (int i = 0; (i < lines.length) && readingHeader; i++) {
            final String line = lines[i];

            if (line.startsWith(COMMENT_PREFIX)) {
                if (i > 0) {
                    result.append("\n");
                }

                if (line.length() > COMMENT_PREFIX.length()) {
                    result.append(line.substring(COMMENT_PREFIX.length()));
                }

                foundHeader = true;
            } else if (foundHeader && line.isEmpty()) {
                result.append("\n");
            } else if (foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    @Override
    protected String buildHeader() {
        final String header = options().header();

        if (options().copyHeader()) {
            final Configuration def = getDefaults();

            if (def != null && def instanceof FileConfiguration) {
                final FileConfiguration filedefaults = (FileConfiguration) def;
                final String defaultsHeader = filedefaults.buildHeader();

                if ((defaultsHeader != null) && !defaultsHeader.isEmpty()) {
                    return defaultsHeader;
                }
            }
        }

        if (header == null) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        final String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;

        for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");

            if (startedHeader || !lines[i].isEmpty()) {
                builder.insert(0, lines[i]);
                builder.insert(0, COMMENT_PREFIX);
                startedHeader = true;
            }
        }

        return builder.toString();
    }

    @Override
    public YamlConfigurationOptions options() {
        if (options == null) {
            options = new YamlConfigurationOptions(this);
        }

        return (YamlConfigurationOptions) options;
    }
}
