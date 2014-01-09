package net.md_5.bungee.module.cmd.find;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginFind extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandFind() );
    }
}
