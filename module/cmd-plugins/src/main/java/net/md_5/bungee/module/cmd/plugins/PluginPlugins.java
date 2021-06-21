package net.md_5.bungee.module.cmd.plugins;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginPlugins extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandPlugins() );
    }
}
