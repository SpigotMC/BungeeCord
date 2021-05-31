package net.md_5.bungee.api.plugin;

import java.net.URL;

/**
 * Interface each plugin's class loader will implement.
 */
public interface ProxyPluginClassLoader
{
    /**
     * The the description of the plugin this class loader is associated with
     *
     * @return the plugin's description
     */
    PluginDescription getDescription();

    /**
     * Appends the specified URL to the list of URLs to search for
     * classes and resources.
     * <p>
     * If the URL specified is {@code null} or is already in the
     * list of URLs, or if this loader is closed, then invoking this
     * method has no effect.
     *
     * @param url the URL to be added to the search path of URLs
     * @see java.net.URLClassLoader#addURL(java.net.URL)
     */
    void addURL(URL url);
}
