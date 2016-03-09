package net.md_5.bungee.api.plugin;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DependencyLoaderTest
{

    @Test
    public void testLoadSimple() throws DependencyLoader.DependencyException
    {
        SimplePlugin test = new SimplePlugin( "Test" );
        assertFalse( test.loaded );
        tryLoad( test );
        assertTrue( test.loaded );
    }

    @Test
    public void testLoadDepend() throws DependencyLoader.DependencyException
    {
        SimplePlugin plugin = new SimplePlugin( "Plugin", "Dependency" );
        SimplePlugin dependency = new SimplePlugin( "Dependency" );
        assertFalse( plugin.loaded );
        assertFalse( dependency.loaded );
        makeLoader( plugin, dependency ).loadPlugin0( plugin );
        assertTrue( plugin.loaded );
        assertTrue( dependency.loaded );
    }

    @Test(expected = DependencyLoader.DependencyException.class)
    public void testCircularDepend() throws DependencyLoader.DependencyException
    {
        SimplePlugin plugin = new SimplePlugin( "Plugin", "Dependency" );
        SimplePlugin dependency = new SimplePlugin( "Dependency", "Plugin" );
        makeLoader( plugin, dependency ).loadPlugin0( plugin );
    }

    /**
     * Make a loader with the given plugins and load all of them.
     */
    private DependencyLoader tryLoad(AvailablePluginWrapper... plugins) throws DependencyLoader.DependencyException
    {
        DependencyLoader loader = makeLoader( plugins );
        for ( AvailablePluginWrapper plugin : plugins )
        {
            loader.loadPlugin0( plugin );
        }
        return loader;
    }

    /**
     * Make a loader with the given plugins.
     */
    private DependencyLoader makeLoader(AvailablePluginWrapper... plugins)
    {
        Map<String, AvailablePluginWrapper> pluginMap = new HashMap<>();
        for ( AvailablePluginWrapper plugin : plugins )
        {
            pluginMap.put( plugin.getDescription().getName(), plugin );
        }
        return new DependencyLoader( null, pluginMap );
    }

    private static class SimplePlugin extends AvailablePluginWrapper
    {

        private boolean loaded = false;

        public SimplePlugin(String name, String... dependencies)
        {
            super( makePluginDescription( name, dependencies ) );
        }

        private static PluginDescription makePluginDescription(String name, String[] dependencies)
        {
            PluginDescription description = new PluginDescription();
            description.setName( name );
            description.setDepends( Sets.newHashSet( dependencies ) );
            return description;
        }

        @Override
        public Plugin loadPlugin(PluginManager manager) throws Exception
        {
            return new Plugin()
            {
                @Override
                public void onLoad()
                {
                    loaded = true;
                }
            };
        }
    }
}
