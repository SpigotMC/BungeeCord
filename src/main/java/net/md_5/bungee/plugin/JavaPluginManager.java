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

/**
 * Plugin manager to handle loading and saving other JavaPlugin's. This class is
 * itself a plugin for ease of use.
 */
public class JavaPluginManager extends JavaPlugin
{

    /**
     * Set of loaded plugins.
     */
    @Getter
    private final Set<JavaPlugin> plugins = new HashSet<>();

    /**
     * Load all plugins from the plugins folder. This method must only be called
     * once per instance.
     */
    public void loadPlugins()
    {
        File dir = new File("plugins");
        dir.mkdir();

        for (File file : dir.listFiles(new PatternFilenameFilter(".*\\.jar")))
        {
            try
            {
                JarFile jar = new JarFile(file);
                ZipEntry entry = jar.getEntry("plugin.yml");
                if (entry == null)
                {
                    throw new InvalidPluginException("Jar does not contain a plugin.yml");
                }

                PluginDescription description;
                try (InputStream is = jar.getInputStream(entry))
                {
                    description = PluginDescription.load(is);
                }
                URLClassLoader classloader = new URLClassLoader(new URL[]
                        {
                            file.toURI().toURL()
                        }, getClass().getClassLoader());
                Class<?> clazz = Class.forName(description.getMain(), true, classloader);
                Class<? extends JavaPlugin> subClazz = clazz.asSubclass(JavaPlugin.class);
                JavaPlugin plugin = subClazz.getDeclaredConstructor().newInstance();

                plugin.description = description;
                plugin.onEnable();
                plugins.add(plugin);

                $().info("Loaded plugin: " + plugin.description.getName());
            } catch (Exception ex)
            {
                $().severe("Could not load plugin: " + file);
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable()
    {
        for (JavaPlugin p : plugins)
        {
            p.onDisable();
        }
    }

    @Override
    public void onHandshake(LoginEvent event)
    {
        for (JavaPlugin p : plugins)
        {
            p.onHandshake(event);
        }
    }

    @Override
    public void onLogin(LoginEvent event)
    {
        for (JavaPlugin p : plugins)
        {
            p.onLogin(event);
        }
    }

    @Override
    public void onServerConnect(ServerConnectEvent event)
    {
        for (JavaPlugin p : plugins)
        {
            p.onServerConnect(event);
        }
    }

    @Override
    public void onPluginMessage(PluginMessageEvent event)
    {
        for (JavaPlugin p : plugins)
        {
            p.onPluginMessage(event);
        }
    }

    @Override
    public void onChat(ChatEvent event)
    {
        for (JavaPlugin p : plugins)
        {
            p.onChat(event);
        }
    }
}
