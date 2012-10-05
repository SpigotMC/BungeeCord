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
import static net.md_5.bungee.Logger.$;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class Configuration {

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
     * Socket timeout.
     */
    public int timeout = 15000;
    /**
     * All servers.
     */
    public Map<String, String> servers = new HashMap<String, String>() {
        {
            put(defaultServerName, "127.0.0.1");
            put("pvp", "127.0.0.1:1337");
        }
    };
    /**
     * Forced servers.
     */
    public Map<String, String> forcedServers = new HashMap<String, String>() {
        {
            put("pvp.md-5.net", "pvp");
        }
    };
    /**
     * Proxy admins.
     */
    public List<String> admins = new ArrayList<String>() {
        {
            add("md_5");
        }
    };
    /**
     * Proxy moderators.
     */
    public List<String> moderators = new ArrayList<String>() {
        {
            add("mbaxter");
        }
    };
    /**
     * Maximum number of lines to log before old ones are removed.
     */
    public int logNumLines = 1 << 14;

    public void load() {
        try {
            file.createNewFile();
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            yaml = new Yaml(options);

            try (InputStream is = new FileInputStream(file)) {
                config = (Map) yaml.load(is);
            }

            if (config == null) {
                config = new LinkedHashMap<>();
            }

            $().info("-------------- Loading configuration ----------------");
            for (Field field : getClass().getDeclaredFields()) {
                if (!Modifier.isTransient(field.getModifiers())) {
                    String name = Util.normalize(field.getName());
                    try {
                        Object def = field.get(this);
                        Object value = get(name, def);

                        field.set(this, value);

                        $().info(name + ": " + value);
                    } catch (IllegalAccessException ex) {
                        $().severe("Could not get config node: " + name);
                    }
                }
            }
            $().info("-----------------------------------------------------");

            if (servers.get(defaultServerName) == null) {
                throw new IllegalArgumentException("Server '" + defaultServerName + "' not defined");
            }
            for (String server : forcedServers.values()) {
                if (!servers.containsKey(server)) {
                    throw new IllegalArgumentException("Forced server " + server + " is not defined in servers");
                }
            }

            reconnect.createNewFile();
            try (FileInputStream recon = new FileInputStream(reconnect)) {
                reconnectLocations = (Map) yaml.load(recon);
            }
            if (reconnectLocations == null) {
                reconnectLocations = new LinkedHashMap<>();
            }

        } catch (IOException ex) {
            $().severe("Could not load config!");
            ex.printStackTrace();
        }
    }

    private <T> T get(String path, T def) {
        if (!config.containsKey(path)) {
            config.put(path, def);
            save(file, config);
        }
        return (T) config.get(path);
    }

    private void save(File fileToSave, Map toSave) {
        try {
            try (FileWriter wr = new FileWriter(fileToSave)) {
                yaml.dump(toSave, wr);
            }
        } catch (IOException ex) {
            $().severe("Could not save config file " + fileToSave);
            ex.printStackTrace();
        }
    }

    public InetSocketAddress getHostFor(String user, String requestedHost) {
        String entry = user + ";" + requestedHost;

        String hostLine;
        if (forcedServers.containsKey(requestedHost)) {
            hostLine = servers.get(forcedServers.get(requestedHost));
        } else {
            hostLine = reconnectLocations.get(entry);
        }
        if (hostLine == null) {
            hostLine = servers.get(defaultServerName);
        }
        return Util.getAddr(hostLine);
    }

    public void setHostFor(UserConnection user, String host) {
        String entry = user.username + ";" + Util.getAddr(user.handshake.host);
        reconnectLocations.put(entry, host);
    }

    public InetSocketAddress getServer(String name) {
        String hostline = (name == null) ? defaultServerName : name;
        if (hostline != null) {
            return Util.getAddr(hostline);
        } else {
            throw new IllegalArgumentException("No server by name " + name);
        }
    }

    public void saveHosts() {
        save(reconnect, reconnectLocations);
        $().info("Saved reconnect locations to " + reconnect);
    }
}
