package net.md_5.bungee.protocol;

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
        // (Integer.MAX_VALUE & 0x7FFFFF) = 8388607
        if (allowExtended) {
            Preconditions.checkArgument( b.length <= (Integer.MAX_VALUE & 0x7FFFFF), "Cannot send array longer than 8388607 (got %s bytes)", b.length );
        } else {
            Preconditions.checkArgument( b.length <= Short.MAX_VALUE, "Cannot send array longer than Short.MAX_VALUE (got %s bytes)", b.length );
        }
        // Write a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        writeVarShort(buf, b.length);
        buf.writeBytes( b );
    }

    public static byte[] readArrayLegacy(ByteBuf buf)
    {
        // Read in a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        int len = readVarShort(buf);

        // (Integer.MAX_VALUE & 0x7FFFFF) = 8388607
        Preconditions.checkArgument( len <= (Integer.MAX_VALUE & 0x7FFFFF), "Cannot receive array longer than 8388607 (got %s bytes)", len );

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
    
    /**
     * Reads an extended length short from the packet. 3 byte shorts are used by Forge custom payload packets to extend size (>32kiB).
     * 
     * See Forge for the original implementation: https://github.com/MinecraftForge/FML/blob/master/src/main/java/cpw/mods/fml/common/network/ByteBufUtils.java#L64
     * (as of 19th March 2014). 
     * 
     * @param buf The {@link ByteBuf} to read the variable length Short from. 
     * @return The length of the buffer.
     */
    public static int readVarShort(ByteBuf buf)
    {
        int low = buf.readUnsignedShort();
        int high = 0;
        
        // If the 16th bit is a 1, then we have an extended short. Consume one more byte.
        if ((low & 0x8000) != 0)
        {
            // Remove the 16th bit that we are not interested in.
            low = low & 0x7FFF;

            // Get the high byte.
            high = buf.readUnsignedByte();
        }

        // Shift the high byte left 15 bits, bitwise OR it with the lower short, then you have your length!
        return ((high & 0xFF) << 15) | low;
    }

    /**
     * Writes a 2 or 3 byte short to the packet to indicate length of packet. 3 byte shorts are used by Forge custom payload packets to extend size (>32kiB).
     * 
     * See Forge for the original implementation: https://github.com/MinecraftForge/FML/blob/master/src/main/java/cpw/mods/fml/common/network/ByteBufUtils.java#L76
     * (as of 19th March 2014)
     * 
     * @param buf The {@link ByteBuf} to write the variable length Short to. 
     * @param toWrite The length of the packet to write.
     */
    public static void writeVarShort(ByteBuf buf, int toWrite)
    {
        // Get the first 15 bits of the integer (> 32767).
        int low = toWrite & 0x7FFF;

        // Get the next byte's worth of information, shift it down to the lowest byte.
        int high = ( toWrite & 0x7F8000 ) >> 15;

        // If we have a significant number in the "high" bit, then we need an extra bit.
        if (high != 0)
        {
            // We do this by turning the low bit into something the same size as a short,
            // using a bitwise OR to insert a 1 in bit 16 , which is used as the indicator
            // for an "extended short".
            low = low | 0x8000;
        }

        // We now write whatever is in the low bit.
        buf.writeShort(low);
        if (high != 0)
        {
            // If we had the high byte, then write it.
            buf.writeByte(high);
        }
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
