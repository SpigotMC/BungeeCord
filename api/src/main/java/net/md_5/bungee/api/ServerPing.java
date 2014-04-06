package net.md_5.bungee.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the standard list data returned by opening a server in the
 * Minecraft client server list, or hitting it with a packet 0xFE.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerPing
{

    private Protocol version;

    @Data
    @AllArgsConstructor
    public static class Protocol
    {

        private String name;
        private int protocol;
    }
    private Players players;

    @Data
    @AllArgsConstructor
    public static class Players
    {

        private int max;
        private int online;
        private PlayerInfo[] sample;
    }

    @Data
    @AllArgsConstructor
    public static class PlayerInfo
    {

        private String name;
        private String id;
    }

    public void setFavicon(Favicon icon)
    {
        this.favicon = icon.getIcon();
    }

    public void setFavicon(String icon)
    {
        this.favicon = icon;
    }

    @java.beans.ConstructorProperties({"version", "players", "description", "favicon"})
    public ServerPing(final Protocol version, final Players players, final String description, final Favicon favicon)
    {
        this.version = version;
        this.players = players;
        this.description = description;
        this.favicon = favicon.getIcon();
    }
    
    private String description;
    private String favicon;
}
