package net.md_5.bungee.plugin;

import com.google.common.io.PatternFilenameFilter;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import lombok.Getter;
import static net.md_5.bungee.Logger.$;

public class JavaPluginManager extends JavaPlugin {

    @Getter
    private final Set<JavaPlugin> plugins = new HashSet<>();

    public void loadPlugins() {
        File dir = new File("plugins");
        dir.mkdir();

        for (File file : dir.listFiles(new PatternFilenameFilter(".*\\.jar"))) {
            try {
                JarFile jar = new JarFile(file);
                ZipEntry entry = jar.getEntry("plugin.yml");
                if (entry == null) {
                    throw new InvalidPluginException("Jar does not contain a plugin.yml");
                }

                PluginDescription description;
                try (InputStream is = jar.getInputStream(entry)) {
                    description = PluginDescription.load(is);
                }
                URLClassLoader classloader = new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());
                Class<?> clazz = Class.forName(description.getMain(), true, classloader);
                Class<? extends JavaPlugin> subClazz = clazz.asSubclass(JavaPlugin.class);
                JavaPlugin plugin = subClazz.getDeclaredConstructor().newInstance();

                plugin.description = description;
                plugin.onEnable();
                plugins.add(plugin);

                $().info("Loaded plugin: " + plugin.description.getName());
            } catch (Exception ex) {
                $().severe("Could not load plugin: " + file);
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        for (JavaPlugin p : plugins) {
            p.onDisable();
        }
    }

    @Override
    public void onConnect(ConnectEvent event) {
        for (JavaPlugin p : plugins) {
            p.onConnect(event);
        }
    }
}
