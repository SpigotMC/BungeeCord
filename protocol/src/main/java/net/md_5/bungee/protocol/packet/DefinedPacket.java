package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class DefinedPacket
{

    @SuppressWarnings("unchecked")
    public static Class<? extends DefinedPacket>[] classes = new Class[ 256 ];
    @SuppressWarnings("unchecked")
    private static Constructor<? extends DefinedPacket>[] consructors = new Constructor[ 256 ];
    private final int id;


    public static DefinedPacket packet(ByteBuf buf)
    {
        DefinedPacket ret = null;
        int id = buf.readUnsignedByte();
        Class<? extends DefinedPacket> clazz = classes[id];

        if ( clazz != null )
        {
            try
            {
                Constructor<? extends DefinedPacket> constructor = consructors[id];
                if ( constructor == null )
                {
                    constructor = clazz.getDeclaredConstructor();
                    consructors[id] = constructor;
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

    public final int getId()
    {
        return id;
    }

    public void writeString(String s, ByteBuf buf)
    {
        // TODO: Check len - use Guava?
        buf.writeShort( s.length() );
        for ( char c : s.toCharArray() )
        {
            buf.writeChar( c );
        }
    }

    public String readString(ByteBuf buf)
    {
        // TODO: Check len - use Guava?
        short len = buf.readShort();
        char[] chars = new char[ len ];
        for ( int i = 0; i < len; i++ )
        {
            chars[i] = buf.readChar();
        }
        return new String( chars );
    }

    public void writeArray(byte[] b, ByteBuf buf)
    {
        // TODO: Check len - use Guava?
        buf.writeByte( b.length );
        buf.writeBytes( b );
    }

    public byte[] readArray(ByteBuf buf)
    {
        // TODO: Check len - use Guava?
        short len = buf.readShort();
        byte[] ret = new byte[ len ];
        buf.readBytes( ret );
        return ret;
    }

    public abstract void read(ByteBuf buf);

    public abstract void write(ByteBuf buf);

    public abstract void handle(PacketHandler handler) throws Exception;

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    static
    {
        classes[0x00] = Packet0KeepAlive.class;
        classes[0x01] = Packet1Login.class;
        classes[0x02] = Packet2Handshake.class;
        classes[0x03] = Packet3Chat.class;
        classes[0x09] = Packet9Respawn.class;
        classes[0xC9] = PacketC9PlayerListItem.class;
        classes[0xCC] = PacketCCSettings.class;
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
    }
}
