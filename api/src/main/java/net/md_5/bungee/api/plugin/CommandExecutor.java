package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;

// Full supported with Lambda coding (Java8 FTW)...
public interface CommandExecutor
{

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    void execute(CommandSender sender, String[] args);

}
