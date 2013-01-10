package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * Load a plugin from the specified file. This file must be in jar or zip
     * format.
     *
     * @param file the file to load from
     */
    public void load(File file)
    {
        Preconditions.checkNotNull(file, "file");
        Preconditions.checkArgument(file.isFile(), "Must load from file");
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
                load(file);
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
