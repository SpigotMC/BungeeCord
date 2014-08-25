package net.md_5.bungee.forge;

import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.packet.PluginMessage;

class ForgeLogger
{
    static void logServer(LogDirection direction, String stateName, PluginMessage message) {
        // [Server START][STATE: LOGIN][SENDING: FMLHandshakeMessage.ServerHello]
        String arrow = direction == LogDirection.RECIEVED ? "->" : "<-";
        String log = "[Bungee " + arrow + " Server " + stateName + "][" + direction.name() + ": " + getNameFromDiscriminator(message) + "]";
        BungeeCord.getInstance().getLogger().log( Level.INFO, log );
    }
    
    static void logClient(LogDirection direction, String stateName, PluginMessage message) {
        // [Server START][STATE: LOGIN][SENDING: FMLHandshakeMessage.ServerHello]
        String arrow = direction == LogDirection.SENDING ? "->" : "<-";
        String log = "[Bungee " + arrow + " Client " + stateName + "][" + direction.name() + ": " + getNameFromDiscriminator(message) + "]";
        BungeeCord.getInstance().getLogger().log( Level.INFO, log );
    }

    private static String getNameFromDiscriminator(PluginMessage message) {
        byte discrim = message.getData()[0];
        switch (discrim)
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
    }
    
    public enum LogDirection
    {
        SENDING,
        RECIEVED
    }
    
    private ForgeLogger() { }
}
