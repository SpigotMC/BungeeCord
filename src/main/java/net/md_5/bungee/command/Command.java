package net.md_5.bungee.command;

import net.md_5.bungee.ChatColor;

public abstract class Command {

    /**
     * Execute this command.
     *
     * @param sender the sender executing this command
     * @param args the parameters to this command, does not include the '/' or
     * the original command.
     */
    public abstract void execute(CommandSender sender, String[] args);

    /**
     * Check if the arg count is at least the specified amount
     *
     * @param sender sender to send message to if unsuccessful
     * @param args to check
     * @param count to compare
     * @return if the arguments are valid
     */
    public final boolean testArgs(CommandSender sender, String[] args, int count) {
        boolean valid = args.length >= count;
        if (!valid) {
            sender.sendMessage(ChatColor.RED + "Please review your argument count");
        }
        return valid;
    }
}
