package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;

public class CommandEnd extends Command {

    @Override
    public void execute(CommandSender sender, String[] args) {
        BungeeCord.instance.stop();
    }
}
