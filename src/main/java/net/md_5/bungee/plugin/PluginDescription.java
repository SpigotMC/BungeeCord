package net.md_5.bungee.plugin;

import java.io.InputStream;
import java.lang.reflect.Field;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

/**
 * File which contains information about a plugin, its authors, and how to load
 * it.
 */
@Data
public class PluginDescription {

    private String name;
    private String main;
    private String version;
    private String author;

    private PluginDescription() {
    }

    public static PluginDescription load(InputStream is) {
        PluginDescription ret = new Yaml().loadAs(is, PluginDescription.class);
        if (ret == null) {
            throw new InvalidPluginException("Could not load plugin description file.");
        }

        for (Field f : PluginDescription.class.getDeclaredFields()) {
            try {
                if (f.get(ret) == null) {
                    throw new InvalidPluginException(f.getName() + " is not set properly in plugin description");
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
            }
        }

        return ret;
    }
}
