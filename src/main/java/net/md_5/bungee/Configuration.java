package net.md_5.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static net.md_5.bungee.Logger.$;
import net.md_5.bungee.command.CommandSender;
import net.md_5.bungee.command.ConsoleCommandSender;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Core configuration for the proxy.
 */
public class Configuration
{

    /**
     * Reconnect locations file.
     */
    private transient File reconnect = new File("locations.yml");
    /**
     * Loaded reconnect locations.
     */
    private transient Map<String, String> reconnectLocations;
    /**
     * Config file.
     */
    private transient File file = new File("config.yml");
    /**
     * Yaml instance.
     */
    private transient Yaml yaml;
    /**
     * Loaded config.
     */
    private transient Map<String, Object> config;
    /**
     * Bind host.
     */
    public String bindHost = "0.0.0.0:25577";
    /**
     * Server ping motd.
     */
    public String motd = "BungeeCord Proxy Instance";
    /**
     * Name of default server.
     */
    public String defaultServerName = "default";
    /**
     * Max players as displayed in list ping. Soft limit.
     */
    public int maxPlayers = 1;
    /**
     * Tab list 1: For a tab list that is global over all server (using their
     * Minecraft name) and updating their ping frequently 2: Same as 1 but does
     * not update their ping frequently, just once, 3: Makes the individual
     * servers handle the tab list (server unique).
     */
    public int tabList = 1;
    /**
     * Socket timeout.
     */
    public int timeout = 15000;
    /**
     * All servers.
     */
    public Map<String, String> servers = new HashMap<String, String>()
    {

        {
            put(defaultServerName, "127.0.0.1:1338");
            put("pvp", "127.0.0.1:1337");
        }
    };
    /**
     * Forced servers.
     */
    public Map<String, String> forcedServers = new HashMap<String, String>()
    {

        {
            put("pvp.md-5.net", "pvp");
        }
    };
    /**
     * Proxy admins.
     */
    public List<String> admins = new ArrayList<String>()
    {

        {
            add("Insert Admins Here");
        }
    };
    /**
     * Proxy moderators.
     */
    public List<String> moderators = new ArrayList<String>()
    {

        {
            add("Insert Moderators Here");
        }
    };
    /**
     * Commands which will be blocked completely.
     */
    public List<String> disabledCommands = new ArrayList<String>()
    {

        {
            add("glist");
        }
    };
    /**
     * Maximum number of lines to log before old ones are removed.
     */
    public int logNumLines = 1 << 14;
    /**
     * UUID for Metrics.
     */
    public String statsUuid = UUID.randomUUID().toString();

    /**
     * Load the configuration and save default values.
     */
    public void load()
    {
        try
        {
            file.createNewFile();
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            yaml = new Yaml(options);

            try (InputStream is = new FileInputStream(file))
            {
                config = (Map) yaml.load(is);
            }

            if (config == null)
            {
                config = new LinkedHashMap<>();
            }

            $().info("-------------- Loading configuration ----------------");
            for (Field field : getClass().getDeclaredFields())
            {
                if (!Modifier.isTransient(field.getModifiers()))
                {
                    String name = Util.normalize(field.getName());
                    try
                    {
                        Object def = field.get(this);
                        Object value = get(name, def);

                        field.set(this, value);

                        $().info(name + ": " + value);
                    } catch (IllegalAccessException ex)
                    {
                        $().severe("Could not get config node: " + name);
                    }
                }
            }
            $().info("-----------------------------------------------------");

            if (servers.get(defaultServerName) == null)
            {
                throw new IllegalArgumentException("Server '" + defaultServerName + "' not defined");
            }

            if (forcedServers != null)
            {
                for (String server : forcedServers.values())
                {
                    if (!servers.containsKey(server))
                    {
                        throw new IllegalArgumentException("Forced server " + server + " is not defined in servers");
                    }
                }
            }

            motd = ChatColor.translateAlternateColorCodes('&', motd);

            reconnect.createNewFile();
            try (FileInputStream recon = new FileInputStream(reconnect))
            {
                reconnectLocations = (Map) yaml.load(recon);
            }
            if (reconnectLocations == null)
            {
                reconnectLocations = new LinkedHashMap<>();
            }

        } catch (IOException ex)
        {
            $().severe("Could not load config!");
            ex.printStackTrace();
        }
    }

    private <T> T get(String path, T def)
    {
        if (!config.containsKey(path))
        {
            config.put(path, def);
            save(file, config);
        }
        return (T) config.get(path);
    }

    private void save(File fileToSave, Map toSave)
    {
        try
        {
            try (FileWriter wr = new FileWriter(fileToSave))
            {
                yaml.dump(toSave, wr);
            }
        } catch (IOException ex)
        {
            $().severe("Could not save config file " + fileToSave);
            ex.printStackTrace();
        }
    }

    /**
     * Get which server a user should be connected to, taking into account their
     * name and virtual host.
     *
     * @param user to get a server for
     * @param requestedHost the host which they connected to
     * @return the name of the server which they should be connected to.
     */
    public String getServer(String user, String requestedHost)
    {
        String server = (forcedServers == null) ? null : forcedServers.get(requestedHost);
        if (server == null)
        {
            server = reconnectLocations.get(user);
        }
        if (server == null)
        {
            server = servers.get(defaultServerName);
        }
        return server;
    }

    /**
     * Save the last server which the user was on.
     *
     * @param user the name of the user
     * @param server which they were last on
     */
    public void setServer(UserConnection user, String server)
    {
        reconnectLocations.put(user.username, server);
    }

    /**
     * Gets the connectable address of a server defined in the configuration.
     *
     * @param name the friendly name of a server
     * @return the usable {@link InetSocketAddress} mapped to this server
     */
    public InetSocketAddress getServer(String name)
    {
        String server = servers.get((name == null) ? defaultServerName : name);
        return (server != null) ? Util.getAddr(server) : getServer(null);
    }

    /**
     * Save the current mappings of users to servers.
     */
    public void saveHosts()
    {
        save(reconnect, reconnectLocations);
        $().info("Saved reconnect locations to " + reconnect);
    }

    /**
     * Get the highest permission a player has.
     *
     * @param sender to get permissions of
     * @return their permission
     */
    public Permission getPermission(CommandSender sender)
    {
        Permission permission = Permission.DEFAULT;
        if (admins.contains(sender.getName()) || sender instanceof ConsoleCommandSender)
        {
            permission = Permission.ADMIN;
        } else if (moderators.contains(sender.getName()))
        {
            permission = Permission.MODERATOR;
        }
        return permission;
    }
}
