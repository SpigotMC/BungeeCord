package net.md_5.bungee.protocol;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.lang.reflect.Constructor;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.TabComplete;
import net.md_5.bungee.protocol.packet.Team;

public enum Protocol
{

    // Undef
    SERVER_HANDSHAKE
    {
        
        {
            registerPacket( 0x00, Handshake.class );
        }
    },
    // 0
    CLIENT_GAME
    {
        
        {
            registerPacket( 0x00, KeepAlive.class );
            registerPacket( 0x01, Login.class );
            registerPacket( 0x02, Chat.class );
            registerPacket( 0x07, Respawn.class );
            registerPacket( 0x3B, PlayerListItem.class );
            registerPacket( 0x3D, TabComplete.class );
            registerPacket( 0x3E, ScoreboardObjective.class );
            registerPacket( 0x3F, ScoreboardScore.class );
            registerPacket( 0x40, ScoreboardDisplay.class );
            registerPacket( 0x41, Team.class );
            registerPacket( 0x42, PluginMessage.class );
            registerPacket( 0x43, Kick.class );
        }
    },
    // 0
    SERVER_GAME
    {
        
        {
            registerPacket( 0x00, KeepAlive.class );
            registerPacket( 0x14, TabComplete.class );
            registerPacket( 0x15, ClientSettings.class );
            registerPacket( 0x17, PluginMessage.class );
        }
    },
    // 1
    CLIENT_STATUS
    {
        
        {
        }
    },
    // 1
    SERVER_STATUS
    {
        
        {
        }
    },
    // 2
    CLIENT_LOGIN
    {
        
        {
            registerPacket( 0x00, Kick.class );
            registerPacket( 0x01, EncryptionResponse.class );
            registerPacket( 0x02, LoginSuccess.class );
        }
    },
    // 2
    SERVER_LOGIN
    {
        
        {
            registerPacket( 0x00, LoginRequest.class );
        }
    };
    /*========================================================================*/
    public static final int MAX_PACKET_ID = 0xFF;
    /*========================================================================*/
    private final TObjectIntMap<Class<? extends DefinedPacket>> packetMap = new TObjectIntHashMap<>( MAX_PACKET_ID );
    private final Class<? extends DefinedPacket>[] packetClasses = new Class[ MAX_PACKET_ID ];
    private final Constructor<? extends DefinedPacket>[] packetConstructors = new Constructor[ MAX_PACKET_ID ];

    public final DefinedPacket createPacket(int id)
    {
        if ( id > MAX_PACKET_ID )
        {
            throw new BadPacketException( "Packet with id " + id + " outside of range " );
        }
        if ( packetConstructors[id] == null )
        {
            throw new BadPacketException( "No packet with id " + id );
        }

        try
        {
            return packetClasses[id].newInstance();
        } catch ( ReflectiveOperationException ex )
        {
            throw new BadPacketException( "Could not construct packet with id " + id, ex );
        }
    }

    protected final void registerPacket(int id, Class<? extends DefinedPacket> packetClass)
    {
        try
        {
            packetConstructors[id] = packetClass.getDeclaredConstructor();
        } catch ( NoSuchMethodException ex )
        {
            throw new BadPacketException( "No NoArgsConstructor for packet class " + packetClass );
        }
        packetClasses[id] = packetClass;
        packetMap.put( packetClass, id );
    }

    protected final void unregisterPacket(int id)
    {
        packetMap.remove( packetClasses[id] );
        packetClasses[id] = null;
        packetConstructors[id] = null;
    }

    final int getId(Class<? extends DefinedPacket> packet)
    {
        return packetMap.get( packet );
    }
}
