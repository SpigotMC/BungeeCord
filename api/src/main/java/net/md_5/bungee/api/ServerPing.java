package net.md_5.bungee.api;

import lombok.Data;

/**
 * Represents the standard list data returned by opening a server in the
 * Minecraft client server list, or hitting it with a packet 0xFE.
 */
@Data
public class ServerPing
{

    private final byte protocolVersion;
    private final String gameVersion;
    private final String motd;
    private final String currentPlayers;
    private final String maxPlayers;
}
