package net.md_5.bungee.module.reconnect.yaml;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginYaml extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().setReconnectHandler( new YamlReconnectHandler() );
    }
}
