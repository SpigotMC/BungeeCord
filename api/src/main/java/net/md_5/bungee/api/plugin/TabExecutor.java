package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;

import lombok.NonNull;

public interface TabExecutor
{

    public @NonNull Iterable<String> onTabComplete(CommandSender sender, String[] args);
}
