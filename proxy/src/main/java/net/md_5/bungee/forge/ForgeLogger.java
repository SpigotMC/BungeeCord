package net.md_5.bungee.forge;

import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.packet.PluginMessage;

class ForgeLogger
{

    static void logServer(LogDirection direction, String stateName, PluginMessage message)
    {
        String dir = direction == LogDirection.SENDING ? "Server -> Bungee" : "Server <- Bungee";
        String log = "[" + stateName + " " + dir + "][" + direction.name() + ": " + getNameFromDiscriminator( message.getTag(), message ) + "]";
        BungeeCord.getInstance().getLogger().log( Level.FINE, log );
    }

    static void logClient(LogDirection direction, String stateName, PluginMessage message)
    {
        String dir = direction == LogDirection.SENDING ? "Client -> Bungee" : "Client <- Bungee";
        String log = "[" + stateName + " " + dir + "][" + direction.name() + ": " + getNameFromDiscriminator( message.getTag(), message ) + "]";
        BungeeCord.getInstance().getLogger().log( Level.FINE, log );
    }

    private static String getNameFromDiscriminator(String channel, PluginMessage message)
    {
        byte discrim = message.getData()[0];
        if ( channel.equals( ForgeConstants.FML_HANDSHAKE_TAG ) )
        {
            switch ( discrim )
            {
                case -2:
                    return "Reset";
                case -1:
                    return "HandshakeAck";
                case 0:
                    return "ServerHello";
                case 1:
                    return "ClientHello";
                case 2:
                    return "ModList";
                case 3:
                    return "ModIdData";
                default:
                    return "Unknown";
            }
        } else if ( channel.equals( ForgeConstants.FORGE_REGISTER ) )
        {
            switch ( discrim )
            {
                case 1:
                    return "DimensionRegister";
                case 2:
                    return "FluidIdMap";
                default:
                    return "Unknown";
            }
        }
        return "UnknownChannel";
    }

    public enum LogDirection
    {

        SENDING,
        RECEIVED
    }

    private ForgeLogger()
    { // Don't allow instantiations
    }
}
