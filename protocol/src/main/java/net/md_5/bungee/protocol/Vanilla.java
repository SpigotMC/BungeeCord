package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.Getter;
import static net.md_5.bungee.protocol.OpCode.*;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import net.md_5.bungee.protocol.packet.Packet0KeepAlive;
import net.md_5.bungee.protocol.packet.Packet1Login;
import net.md_5.bungee.protocol.packet.Packet2CEntityProperties;
import net.md_5.bungee.protocol.packet.Packet2Handshake;
import net.md_5.bungee.protocol.packet.Packet3Chat;
import net.md_5.bungee.protocol.packet.Packet9Respawn;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;
import net.md_5.bungee.protocol.packet.PacketCBTabComplete;
import net.md_5.bungee.protocol.packet.PacketCCSettings;
import net.md_5.bungee.protocol.packet.PacketCDClientStatus;
import net.md_5.bungee.protocol.packet.PacketCEScoreboardObjective;
import net.md_5.bungee.protocol.packet.PacketCFScoreboardScore;
import net.md_5.bungee.protocol.packet.PacketD0DisplayScoreboard;
import net.md_5.bungee.protocol.packet.PacketD1Team;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.protocol.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.protocol.packet.PacketFEPing;
import net.md_5.bungee.protocol.packet.PacketFFKick;
import net.md_5.bungee.protocol.skip.PacketReader;

public class Vanilla implements Protocol
{

    public static final byte PROTOCOL_VERSION = 80;
    public static final String GAME_VERSION = "13w39b";
    @Getter
    private static final Vanilla instance = new Vanilla();
    /*========================================================================*/
    @Getter
    private final OpCode[][] opCodes = new OpCode[ 256 ][];
    @SuppressWarnings("unchecked")
    @Getter
    protected Class<? extends DefinedPacket>[] classes = new Class[ 256 ];
    @SuppressWarnings("unchecked")
    @Getter
    private Constructor<? extends DefinedPacket>[] constructors = new Constructor[ 256 ];
    @Getter
    protected PacketReader skipper;
    /*========================================================================*/

    public Vanilla()
    {
        classes[0x00] = Packet0KeepAlive.class;
        classes[0x01] = Packet1Login.class;
        classes[0x02] = Packet2Handshake.class;
        classes[0x03] = Packet3Chat.class;
        classes[0x09] = Packet9Respawn.class;
        classes[0xC9] = PacketC9PlayerListItem.class;
        classes[0x2C] = Packet2CEntityProperties.class;
        classes[0xCC] = PacketCCSettings.class;
        classes[0xCB] = PacketCBTabComplete.class;
        classes[0xCD] = PacketCDClientStatus.class;
        classes[0xCE] = PacketCEScoreboardObjective.class;
        classes[0xCF] = PacketCFScoreboardScore.class;
        classes[0xD0] = PacketD0DisplayScoreboard.class;
        classes[0xD1] = PacketD1Team.class;
        classes[0xFA] = PacketFAPluginMessage.class;
        classes[0xFC] = PacketFCEncryptionResponse.class;
        classes[0xFD] = PacketFDEncryptionRequest.class;
        classes[0xFE] = PacketFEPing.class;
        classes[0xFF] = PacketFFKick.class;
        skipper = new PacketReader( this );
    }

    @Override
    public DefinedPacket read(short packetId, ByteBuf buf)
    {
        int start = buf.readerIndex();
        DefinedPacket packet = read( packetId, buf, this );
        if ( buf.readerIndex() == start )
        {
            throw new BadPacketException( "Unknown packet id " + packetId );
        }
        return packet;
    }

    public static DefinedPacket read(short id, ByteBuf buf, Protocol protocol)
    {
        DefinedPacket packet = packet( id, protocol );
        if ( packet != null )
        {
            packet.read( buf );
            return packet;
        }
        protocol.getSkipper().tryRead( id, buf );
        return null;
    }

    public static DefinedPacket packet(short id, Protocol protocol)
    {
        DefinedPacket ret = null;
        Class<? extends DefinedPacket> clazz = protocol.getClasses()[id];

        if ( clazz != null )
        {
            try
            {
                Constructor<? extends DefinedPacket> constructor = protocol.getConstructors()[id];
                if ( constructor == null )
                {
                    constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible( true );
                    protocol.getConstructors()[id] = constructor;
                }

                if ( constructor != null )
                {
                    ret = constructor.newInstance();
                }
            } catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
            {
            }
        }

        return ret;
    }

    
    {
        opCodes[0x04] = new OpCode[]
        {
            LONG, LONG
        };
        opCodes[0x05] = new OpCode[]
        {
            INT, SHORT, ITEM
        };
        opCodes[0x06] = new OpCode[]
        {
            INT, INT, INT
        };
        opCodes[0x07] = new OpCode[]
        {
            INT, INT, BOOLEAN
        };
        opCodes[0x08] = new OpCode[]
        {
            FLOAT, SHORT, FLOAT
        };
        opCodes[0x0A] = new OpCode[]
        {
            BOOLEAN
        };
        opCodes[0x0B] = new OpCode[]
        {
            DOUBLE, DOUBLE, DOUBLE, DOUBLE, BOOLEAN
        };
        opCodes[0x0C] = new OpCode[]
        {
            FLOAT, FLOAT, BOOLEAN
        };
        opCodes[0x0D] = new OpCode[]
        {
            DOUBLE, DOUBLE, DOUBLE, DOUBLE, FLOAT, FLOAT, BOOLEAN
        };
        opCodes[0x0E] = new OpCode[]
        {
            BYTE, INT, BYTE, INT, BYTE
        };
        opCodes[0x0F] = new OpCode[]
        {
            INT, BYTE, INT, BYTE, ITEM, BYTE, BYTE, BYTE
        };
        opCodes[0x10] = new OpCode[]
        {
            SHORT
        };
        opCodes[0x11] = new OpCode[]
        {
            INT, BYTE, INT, BYTE, INT
        };
        opCodes[0x12] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x13] = new OpCode[]
        {
            INT, BYTE, INT
        };
        opCodes[0x14] = new OpCode[]
        {
            INT, STRING, STRING, INT, INT, INT, BYTE, BYTE, SHORT, METADATA
        };
        opCodes[0x16] = new OpCode[]
        {
            INT, INT
        };
        opCodes[0x17] = new OpCode[]
        {
            INT, BYTE, INT, INT, INT, BYTE, BYTE, OPTIONAL_MOTION
        };
        opCodes[0x18] = new OpCode[]
        {
            INT, BYTE, INT, INT, INT, BYTE, BYTE, BYTE, SHORT, SHORT, SHORT, METADATA
        };
        opCodes[0x19] = new OpCode[]
        {
            INT, STRING, INT, INT, INT, INT
        };
        opCodes[0x1A] = new OpCode[]
        {
            INT, INT, INT, INT, SHORT
        };
        opCodes[0x1B] = new OpCode[]
        {
            FLOAT, FLOAT, BOOLEAN, BOOLEAN
        };
        opCodes[0x1C] = new OpCode[]
        {
            INT, SHORT, SHORT, SHORT
        };
        opCodes[0x1D] = new OpCode[]
        {
            BYTE_INT
        };
        opCodes[0x1E] = new OpCode[]
        {
            INT
        };
        opCodes[0x1F] = new OpCode[]
        {
            INT, BYTE, BYTE, BYTE
        };
        opCodes[0x20] = new OpCode[]
        {
            INT, BYTE, BYTE
        };
        opCodes[0x21] = new OpCode[]
        {
            INT, BYTE, BYTE, BYTE, BYTE, BYTE
        };
        opCodes[0x22] = new OpCode[]
        {
            INT, INT, INT, INT, BYTE, BYTE
        };
        opCodes[0x23] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x26] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x27] = new OpCode[]
        {
            INT, INT, BOOLEAN
        };
        opCodes[0x28] = new OpCode[]
        {
            INT, METADATA
        };
        opCodes[0x29] = new OpCode[]
        {
            INT, BYTE, BYTE, SHORT
        };
        opCodes[0x2A] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x2B] = new OpCode[]
        {
            FLOAT, SHORT, SHORT
        };
        opCodes[0x33] = new OpCode[]
        {
            INT, INT, BOOLEAN, SHORT, SHORT, INT_BYTE
        };
        opCodes[0x34] = new OpCode[]
        {
            INT, INT, SHORT, INT_BYTE
        };
        opCodes[0x35] = new OpCode[]
        {
            INT, BYTE, INT, SHORT, BYTE
        };
        opCodes[0x36] = new OpCode[]
        {
            INT, SHORT, INT, BYTE, BYTE, SHORT
        };
        opCodes[0x37] = new OpCode[]
        {
            INT, INT, INT, INT, BYTE
        };
        opCodes[0x38] = new OpCode[]
        {
            BULK_CHUNK
        };
        opCodes[0x3C] = new OpCode[]
        {
            DOUBLE, DOUBLE, DOUBLE, FLOAT, INT_3, FLOAT, FLOAT, FLOAT
        };
        opCodes[0x3D] = new OpCode[]
        {
            INT, INT, BYTE, INT, INT, BOOLEAN
        };
        opCodes[0x3E] = new OpCode[]
        {
            STRING, INT, INT, INT, FLOAT, BYTE, BYTE
        };
        opCodes[0x3F] = new OpCode[]
        {
            STRING, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, INT
        };
        opCodes[0x46] = new OpCode[]
        {
            BYTE, FLOAT
        };
        opCodes[0x47] = new OpCode[]
        {
            INT, BYTE, INT, INT, INT
        };
        opCodes[0x64] = new OpCode[]
        {
            OPTIONAL_WINDOW
        };
        opCodes[0x65] = new OpCode[]
        {
            BYTE
        };
        opCodes[0x66] = new OpCode[]
        {
            BYTE, SHORT, BYTE, SHORT, BOOLEAN, ITEM
        };
        opCodes[0x67] = new OpCode[]
        {
            BYTE, SHORT, ITEM
        };
        opCodes[0x68] = new OpCode[]
        {
            BYTE, SHORT_ITEM
        };
        opCodes[0x69] = new OpCode[]
        {
            BYTE, SHORT, SHORT
        };
        opCodes[0x6A] = new OpCode[]
        {
            BYTE, SHORT, BOOLEAN
        };
        opCodes[0x6B] = new OpCode[]
        {
            SHORT, ITEM
        };
        opCodes[0x6C] = new OpCode[]
        {
            BYTE, BYTE
        };
        opCodes[0x82] = new OpCode[]
        {
            INT, SHORT, INT, STRING, STRING, STRING, STRING
        };
        opCodes[0x83] = new OpCode[]
        {
            SHORT, SHORT, USHORT_BYTE
        };
        opCodes[0x84] = new OpCode[]
        {
            INT, SHORT, INT, BYTE, SHORT_BYTE
        };
        opCodes[0x85] = new OpCode[]
        {
            BYTE, INT, INT, INT
        };
        opCodes[0xC3] = new OpCode[]
        {
            SHORT, SHORT, INT_BYTE
        };
        opCodes[0xCA] = new OpCode[]
        {
            BYTE, FLOAT, FLOAT
        };
    }
}
