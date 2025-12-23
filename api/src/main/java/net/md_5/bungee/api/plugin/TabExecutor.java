package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;

import org.jetbrains.annotations.NotNull;

public interface TabExecutor
{

    public @NotNull Iterable<String> onTabComplete(CommandSender sender, String[] args);
}
