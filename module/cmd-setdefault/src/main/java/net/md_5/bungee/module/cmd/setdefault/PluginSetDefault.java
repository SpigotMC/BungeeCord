package net.md_5.bungee.module.cmd.setdefault;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginSetDefault extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandSetDefault() );
    }
}
