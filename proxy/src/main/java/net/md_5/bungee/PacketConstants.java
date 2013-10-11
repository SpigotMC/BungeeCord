package net.md_5.bungee;

import net.md_5.bungee.protocol.game.Packet7Respawn;
import net.md_5.bungee.protocol.game.Packet16ClientStatus;
import net.md_5.bungee.protocol.game.Packet42PluginMessage;

public class PacketConstants
{

    public static final Packet7Respawn DIM1_SWITCH = new Packet7Respawn( (byte) 1, (byte) 0, (byte) 0, (short) 256, "DEFAULT" );
    public static final Packet7Respawn DIM2_SWITCH = new Packet7Respawn( (byte) -1, (byte) 0, (byte) 0, (short) 256, "DEFAULT" );
    public static final Packet16ClientStatus CLIENT_LOGIN = new Packet16ClientStatus( (byte) 0 );
    public static final Packet42PluginMessage FORGE_MOD_REQUEST = new Packet42PluginMessage( "FML", new byte[]
    {
        0, 0, 0, 0, 0, 2
    } );
    public static final Packet42PluginMessage I_AM_BUNGEE = new Packet42PluginMessage( "BungeeCord", new byte[ 0 ] );
}
