package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;

import java.util.List;

public interface TabExecutor
{

    public List<String> onTabComplete(CommandSender sender, String[] args);
}
