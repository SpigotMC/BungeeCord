package net.md_5.bungee.api.connection;

import net.md_5.bungee.api.CommandSender;

/**
 * Represents a player who's connection is being connected to somewhere else,
 * whether it be a remote or embedded server.
 */
public interface ProxiedPlayer extends Connection, CommandSender
{
}
