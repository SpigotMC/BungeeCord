package net.md_5.bungee.module.cmd.server;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginServer extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandServer() );
    }
}
