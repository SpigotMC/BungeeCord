package net.md_5.bungee.protocol;

import net.md_5.bungee.protocol.forge.ByteBufUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class DefinedPacket
{

    public static void writeString(String s, ByteBuf buf)
    {
        Preconditions.checkArgument( s.length() <= Short.MAX_VALUE, "Cannot send string longer than Short.MAX_VALUE (got %s characters)", s.length() );

        byte[] b = s.getBytes( Charsets.UTF_8 );
        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public static String readString(ByteBuf buf)
    {
        int len = readVarInt( buf );
        Preconditions.checkArgument( len <= Short.MAX_VALUE, "Cannot receive string longer than Short.MAX_VALUE (got %s characters)", len );

        byte[] b = new byte[ len ];
        buf.readBytes( b );

        return new String( b, Charsets.UTF_8 );
    }

    public static void writeArrayLegacy(byte[] b, ByteBuf buf, boolean allowExtended)
    {
        // (Integer.MAX_VALUE & 0x1FFF9A ) = 2097050 - Forge's current upper limit
        if (allowExtended) {
            Preconditions.checkArgument( b.length <= (Integer.MAX_VALUE & 0x1FFF9A ), "Cannot send array longer than 2097050 (got %s bytes)", b.length );
        } else {
            Preconditions.checkArgument( b.length <= Short.MAX_VALUE, "Cannot send array longer than Short.MAX_VALUE (got %s bytes)", b.length );
        }
        // Write a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        ByteBufUtils.writeVarShort(buf, b.length);
        buf.writeBytes( b );
    }

    public static byte[] readArrayLegacy(ByteBuf buf)
    {
        // Read in a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        int len = ByteBufUtils.readVarShort(buf);

        // (Integer.MAX_VALUE & 0x1FFF9A ) = 2097050 - Forge's current upper limit
        Preconditions.checkArgument( len <= (Integer.MAX_VALUE & 0x1FFF9A), "Cannot receive array longer than 2097050 (got %s bytes)", len );

        byte[] ret = new byte[ len ];
        buf.readBytes( ret );
        return ret;
    }

    public static void writeArray(byte[] b, ByteBuf buf)
    {
        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public static byte[] readArray(ByteBuf buf)
    {
        byte[] ret = new byte[ readVarInt( buf ) ];
        buf.readBytes( ret );
        return ret;
    }

    public static void writeStringArray(String[] s, ByteBuf buf)
    {
        writeVarInt( s.length, buf );
        for ( String str : s )
        {
            writeString( str, buf );
        }
    }

    public static String[] readStringArray(ByteBuf buf)
    {
        int len = readVarInt( buf );
        String[] ret = new String[ len ];
        for ( int i = 0; i < ret.length; i++ )
        {
            ret[i] = readString( buf );
        }
        return ret;
    }

    public static int readVarInt(ByteBuf input)
    {
        int out = 0;
        int bytes = 0;
        byte in;
        while ( true )
        {
            in = input.readByte();

            out |= ( in & 0x7F ) << ( bytes++ * 7 );

            if ( bytes > 5 )
            {
                throw new RuntimeException( "VarInt too big" );
            }

            if ( ( in & 0x80 ) != 0x80 )
            {
                break;
            }
        }

        return out;
    }

    public static void writeVarInt(int value, ByteBuf output)
    {
        int part;
        while ( true )
        {
            part = value & 0x7F;

            value >>>= 7;
            if ( value != 0 )
            {
                part |= 0x80;
            }

            output.writeByte( part );

            if ( value == 0 )
            {
                break;
            }
        }
    }

    public static void writeUUID(UUID value, ByteBuf output)
    {
        output.writeLong( value.getMostSignificantBits() );
        output.writeLong( value.getLeastSignificantBits() );
    }

    public static UUID readUUID(ByteBuf input)
    {
        return new UUID( input.readLong(), input.readLong() );
    }

    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Packet must implement read method" );
    }

    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        read( buf );
    }

    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Packet must implement write method" );
    }

    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        write( buf );
    }

    public abstract void handle(AbstractPacketHandler handler) throws Exception;

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
