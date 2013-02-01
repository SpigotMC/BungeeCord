package net.md_5.bungee.api.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class PluginClassloader extends URLClassLoader
{

    private static final Set<PluginClassloader> allLoaders = new HashSet<>();

    public PluginClassloader(URL[] urls)
    {
        super(urls);
        allLoaders.add(this);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        for (PluginClassloader loader : allLoaders)
        {
            if (loader != this)
            {
                try
                {
                    return loader.loadClass(name);
                } catch (ClassNotFoundException ex)
                {
                }
            }
        }
        throw new ClassNotFoundException();
    }
}
