package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;

/**
 * Class to manage bridging between plugin duties and implementation duties, for
 * example event handling and plugin management.
 */
public class PluginManager
{

    private final EventBus eventBus = new EventBus();
    private final Map<String, Plugin> plugins = new HashMap<>();

    /**
     * Returns the {@link Plugin} objects corresponding to all loaded plugins.
     *
     * @return the set of loaded plugins
     */
    public Collection<Plugin> getPlugins()
    {
        return plugins.values();
    }

    /**
     * Returns a loaded plugin identified by the specified name.
     *
     * @param name of the plugin to retrieve
     * @return the retrieved plugin or null if not loaded
     */
    public Plugin getPlugin(String name)
    {
        return plugins.get(name);
    }

    /**
     * Load a plugin from the specified file. This file must be in jar format.
     *
     * @param file the file to load from
     * @throws Exception Any exceptions encountered when loading a plugin from
     * this file.
     */
    public void load(File file) throws Exception
    {
        Preconditions.checkNotNull(file, "file");
        Preconditions.checkArgument(file.isFile(), "Must load from file");

        try (JarFile jar = new JarFile(file))
        {
            JarEntry pdf = jar.getJarEntry("plugin.yml");
            try (InputStream in = jar.getInputStream(pdf))
            {
                PluginDescription desc = ProxyServer.getInstance().getYaml().loadAs(in, PluginDescription.class);
                URLClassLoader loader = new URLClassLoader(new URL[]
                        {
                            file.toURI().toURL()
                        });
                Class<?> main = loader.loadClass(desc.getMain());
                Plugin plugin = (Plugin) main.getDeclaredConstructor().newInstance();

                plugin.init(desc);
                plugins.put(pdf.getName(), plugin);
                plugin.onEnable();
            }
        }
    }

    /**
     * Load all plugins from the specified folder.
     *
     * @param folder the folder to search for plugins in
     */
    public void loadAll(File folder)
    {
        Preconditions.checkNotNull(folder, "folder");
        Preconditions.checkArgument(folder.isDirectory(), "Must load from a directory");

        for (File file : folder.listFiles())
        {
            if (file.getName().endsWith(".jar"))
            {
                try
                {
                    load(file);
                } catch (Exception ex)
                {
                    ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not load plugin from file " + file, ex);
                }
            }
        }
    }

    /**
     * Dispatch an event to all subscribed listeners and return the event once
     * it has been handled by these listeners.
     *
     * @param <T> the type bounds, must be a class which extends event
     * @param event the event to call
     * @return the called event
     */
    public <T extends Event> T callEvent(T event)
    {
        eventBus.post(event);
        return event;
    }

    /**
     * Register a {@link Listener} for receiving called events. Methods in this
     * Object which wish to receive events must be annotated with the
     * {@link Subscribe} annotation.
     *
     * @param listener the listener to register events for
     */
    public void registerListener(Listener listener)
    {
        eventBus.register(listener);
    }
}
