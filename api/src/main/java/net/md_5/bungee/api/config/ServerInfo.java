package net.md_5.bungee.api.config;

import java.net.InetSocketAddress;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class used to represent a server to connect to.
 */
@Data
@AllArgsConstructor
public class ServerInfo
{

    /**
     * Name this server displays as.
     */
    private final String name;
    /**
     * Connectable address of this server.
     */
    private final InetSocketAddress address;
    /**
     * Permission node required to access this server.
     */
    private String permission;
}
