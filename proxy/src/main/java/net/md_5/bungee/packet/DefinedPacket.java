package net.md_5.bungee.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.Delegate;
import net.md_5.bungee.Util;

/**
 * This class represents a packet which has been given a special definition. All
 * subclasses can read and write to the backing byte array which can be
 * retrieved via the {@link #getPacket()} method.
 */
public abstract class DefinedPacket implements DataOutput
{

    private static interface Overriden
    {

        void readUTF();

        void writeUTF(String s);
    }
    private ByteArrayInputStream byteStream;
    private DataInputStream in;
    @Delegate(excludes = Overriden.class)
    private ByteArrayDataOutput out;
    private byte[] buf;

    public DefinedPacket(int id, byte[] buf)
    {
        byteStream = new ByteArrayInputStream( buf );
        in = new DataInputStream( byteStream );
        if ( readUnsignedByte() != id )
        {
            throw new IllegalArgumentException( "Wasn't expecting packet id " + Util.hex( id ) );
        }
        this.buf = buf;
    }

    public DefinedPacket(int id)
    {
        out = ByteStreams.newDataOutput();
        writeByte( id );
    }

    /**
     * Gets the bytes that make up this packet.
     *
     * @return the bytes which make up this packet, either the original byte
     * array or the newly written one.
     */
    public byte[] getPacket()
    {
        return buf == null ? buf = out.toByteArray() : buf;
    }

    @Override
    public void writeUTF(String s)
    {
        writeShort( s.length() );
        writeChars( s );
    }

    public String readUTF()
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
        write( b );
    }

    public byte[] readArray()
    {
        short len = readShort();
        byte[] ret = new byte[ len ];
        readFully( ret );
        return ret;
    }

    public final int available()
    {
        return byteStream.available();
    }

    public final void readFully(byte b[])
    {
        try
        {
            in.readFully( b );
        } catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    public final boolean readBoolean()
    {
        try
        {
            return in.readBoolean();
        } catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    public final byte readByte()
    {
        try
        {
            return in.readByte();
        } catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    public final int readUnsignedByte()
    {
        try
        {
            return in.readUnsignedByte();
        } catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    public final short readShort()
    {
        try
        {
            return in.readShort();
        } catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    public final char readChar()
    {
        try
        {
            return in.readChar();
        } catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    public final int readInt()
    {
        try
        {
            return in.readInt();
        } catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
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
    @SuppressWarnings("unchecked")
    private static Constructor<? extends DefinedPacket>[] consructors = new Constructor[ 256 ];

    public static DefinedPacket packet(byte[] buf)
    {
        int id = buf[0] & 0xFF;
        Class<? extends DefinedPacket> clazz = classes[id];
        DefinedPacket ret = null;
        if ( clazz != null )
        {
            try
            {
                Constructor<? extends DefinedPacket> constructor = consructors[id];
                if ( constructor == null )
                {
                    constructor = clazz.getDeclaredConstructor( byte[].class );
                    consructors[id] = constructor;
                }

                if ( constructor != null )
                {
                    ret = constructor.newInstance( buf );
                }
            } catch ( IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex )
            {
            }
        }

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
