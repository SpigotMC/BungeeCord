package ru.leymooo.botfilter.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;

/**
 *
 * @author Leymooo
 */
public class IPUtils
{
    public static InetAddress getAddress(UserConnection userCon)
    {
        return userCon.getAddress().getAddress();
    }

    public static InetAddress getAddress(String ip)
    {
        try
        {
            return InetAddress.getByName( ip );
        } catch ( UnknownHostException ex )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not get InetAddress for " + ip, ex );
        }
        return null;
    }

}
