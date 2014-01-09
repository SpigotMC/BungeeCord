package net.md_5.bungee.module.cmd.list;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginList extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandList() );
    }
}
