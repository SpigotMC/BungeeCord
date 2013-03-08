package net.md_5.bungee.packet;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ReferenceCounted;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.Delegate;
import net.md_5.bungee.Util;

/**
 * This class represents a packet which has been given a special definition. All
 * subclasses can read and write to the backing byte array which can be
 * retrieved via the {@link #getPacket()} method.
 */
public abstract class DefinedPacket implements ByteBuf
{

    @Delegate(types =
    {
        ByteBuf.class, ReferenceCounted.class
    })
    private ByteBuf out;
    /**
     * Packet id.
     */
    public final int id;

    public DefinedPacket(int id, byte[] buf)
    {
        out = Unpooled.wrappedBuffer( buf );
        if ( readUnsignedByte() != id )
        {
            throw new IllegalArgumentException( "Wasn't expecting packet id " + Util.hex( id ) );
        }
        this.id = id;
    }

    public DefinedPacket(int id)
    {
        out = Unpooled.buffer();
        this.id = id;
        writeByte( id );
    }

    public void writeString(String s)
    {
        writeShort( s.length() );
        for ( char c : s.toCharArray() )
        {
            writeChar( c );
        }
    }

    public String readString()
    {
        short len = readShort();
        char[] chars = new char[ len ];
        for ( int i = 0; i < len; i++ )
        {
            chars[i] = this.readChar();
        }
        return new String( chars );
    }

    public void writeArray(byte[] b)
    {
        writeShort( b.length );
        writeBytes( b );
    }

    public byte[] readArray()
    {
        short len = readShort();
        byte[] ret = new byte[ len ];
        readBytes( ret );
        return ret;
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    public abstract void handle(PacketHandler handler) throws Exception;
    @SuppressWarnings("unchecked")
    private static Class<? extends DefinedPacket>[] classes = new Class[ 256 ];

    public static DefinedPacket packet(ByteBuf buf)
    {
        int id = buf.getUnsignedShort( 0 );
        Class<? extends DefinedPacket> clazz = classes[id];
        DefinedPacket ret = null;
        if ( clazz != null )
        {
            try
            {
                Constructor<? extends DefinedPacket> constructor = clazz.getDeclaredConstructor( byte[].class );
                if ( constructor != null )
                {
                    ret = constructor.newInstance( buf );
                }
            } catch ( IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex )
            {
            }
        } else
        {
            return null;
        }

        Preconditions.checkState( ret != null, "Don't know how to deal with packet ID %s", Util.hex( id ) );
        return ret;
    }

    static
    {
        classes[0x00] = Packet0KeepAlive.class;
        classes[0x01] = Packet1Login.class;
        classes[0x02] = Packet2Handshake.class;
        classes[0x03] = Packet3Chat.class;
        classes[0x09] = Packet9Respawn.class;
        classes[0xC9] = PacketC9PlayerListItem.class;
        classes[0xCD] = PacketCDClientStatus.class;
        classes[0xFA] = PacketFAPluginMessage.class;
        classes[0xFC] = PacketFCEncryptionResponse.class;
        classes[0xFD] = PacketFDEncryptionRequest.class;
        classes[0xFE] = PacketFEPing.class;
        classes[0xFF] = PacketFFKick.class;
    }
}
