package net.md_5.bungee.module.cmd.alert;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginAlert extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandAlert() );
        getProxy().getPluginManager().registerCommand( this, new CommandAlertRaw() );
    }
}
