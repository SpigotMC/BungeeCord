package net.md_5.bungee.protocol;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.lang.reflect.Constructor;
import net.md_5.bungee.protocol.handshake.HandshakeProtocol;

public class Protocol
{

    private static final int MAX_PACKET_ID = 0xFF;
    private static final int MAX_PROTOCOLS = 0xF;
    /*========================================================================*/
    private static final Protocol[] protocols = new Protocol[ MAX_PROTOCOLS ];
    /*========================================================================*/
    private final TObjectIntMap<Class<? extends DefinedPacket>> packetMap = new TObjectIntHashMap<>( MAX_PACKET_ID );
    private final Class<? extends DefinedPacket>[] packetClasses = new Class[ MAX_PACKET_ID ];
    private final Constructor<? extends DefinedPacket>[] packetConstructors = new Constructor[ MAX_PACKET_ID ];

    static
    {

    }

    public Protocol(int protocolId)
        {
    }

    public static Protocol getProtocol(int id)
    {
        return protocols[id];
    }

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
