package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface TabExecutor
{

    public Iterable<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}
