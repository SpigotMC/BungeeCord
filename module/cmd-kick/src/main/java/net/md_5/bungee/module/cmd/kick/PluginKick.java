package net.md_5.bungee.module.cmd.kick;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginKick extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandKick() );
    }
}
