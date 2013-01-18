package net.md_5.bungee.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlConfig implements ConfigurationAdapter
{

    private boolean loaded;
    private Yaml yaml;
    private Map config;

    public void load()
    {
        try
        {
            File file = new File("config.yml");
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

            loaded = true;
        } catch (IOException ex)
        {
            throw new RuntimeException("Could not load configuration!", ex);
        }
    }

    private <T> T get(String path, T def)
    {
        return get(path, def, config);
    }

    private <T> T get(String path, T def, Map submap)
    {
        if (!loaded)
        {
            load();
        }

        int index = path.indexOf('.');
        if (index == -1)
        {
            Object val = submap.get(path);
            return (val != null) ? (T) val : def;
        } else
        {
            String first = path.substring(0, index);
            String second = path.substring(index, path.length());
            Map sub = (Map) submap.get(first);
            return (sub != null) ? get(second, def, sub) : def;
        }
    }

    @Override
    public int getInt(String path, int def)
    {
        return get(path, def);
    }

    @Override
    public String getString(String path, String def)
    {
        return get(path, def);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ServerInfo> getServers()
    {
        Map<String, Map<String, Object>> base = get("servers", Collections.EMPTY_MAP);
        Map<String, ServerInfo> ret = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : base.entrySet())
        {
            Map<String, Object> val = entry.getValue();
            String name = get("name", null, val);
            String permission = get("permission", null, val);
            String addr = get("address", null, val);
            InetSocketAddress address = Util.getAddr(addr);
            ServerInfo info = new ServerInfo(name, address, permission);
            ret.put(name, info);
        }

        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ListenerInfo> getListeners()
    {
        Map<String, Map<String, Object>> base = get("listeners", Collections.EMPTY_MAP);
        Collection<ListenerInfo> ret = new HashSet<>();

        for (Map.Entry<String, Map<String, Object>> entry : base.entrySet())
        {
            Map<String, Object> val = entry.getValue();
            String motd = get("motd", null, val);
            int maxPlayers = get("motd", null, val);
            String defaultServer = get("default", null, val);
            boolean forceDefault = get("force_default", null, val);
            String host = get("host", null, val);
            InetSocketAddress address = Util.getAddr(host);
            ListenerInfo info = new ListenerInfo(address, motd, maxPlayers, defaultServer, forceDefault);
            ret.add(info);
        }

        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getGroups(String player)
    {
        return get("groups." + player, Collections.EMPTY_SET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getPermissions(String group)
    {
        return get("permissions." + group, Collections.EMPTY_SET);
    }
}
