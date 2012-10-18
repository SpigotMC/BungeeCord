package net.md_5.bungee.plugin;

import com.google.common.io.PatternFilenameFilter;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	
	private Map<StreamDirection, JavaPlugin[][]> packetSubscribers = new HashMap<StreamDirection, JavaPlugin[][]>();
	
    /**
     * Set of loaded plugins.
     */
    @Getter
    private final Set<JavaPlugin> plugins = new HashSet<>();
    
	public JavaPluginManager() {
		packetSubscribers.put(StreamDirection.DOWN, new JavaPlugin[256][]);
		packetSubscribers.put(StreamDirection.UP, new JavaPlugin[256][]);
	}
    
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
    public void onHandshake(HandshakeEvent event)
    {
        for (JavaPlugin p : plugins)
        {
            p.onHandshake(event);
        }
    }
    
    /**
     * An fast way to see if the packet has any subscribers.
     */
    public boolean packetHasSubscribers(int packetId, StreamDirection direction)
    {
    	return this.packetSubscribers.get(direction)[packetId] != null;
    }
    
    @Override
    public void subscribe(int packetId, StreamDirection direction, JavaPlugin plugin)
    {
    	JavaPlugin[][] packetSubscribers = this.packetSubscribers.get(direction);
    	
    	if(packetSubscribers[packetId] == null)
    		packetSubscribers[packetId] = new JavaPlugin[0];
    	
    	ArrayList<JavaPlugin> plugins = new ArrayList<JavaPlugin>(Arrays.asList(packetSubscribers[packetId]));
    	if(!plugins.contains(plugin)) plugins.add(plugin);
    	packetSubscribers[packetId] = plugins.toArray(new JavaPlugin[0]);
    }
    
    @Override
    public void onReceivePacket(PacketEvent event)
    {
    	JavaPlugin[] plugins = packetSubscribers.get(event.getDirection())[event.getPacketId()];
    	
    	if(plugins == null)
    		return;
    	
    	for(JavaPlugin plugin : plugins)
    	{
    		plugin.onReceivePacket(event);
    	}
    }
}
