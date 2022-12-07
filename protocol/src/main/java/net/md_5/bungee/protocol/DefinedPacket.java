package net.md_5.bungee.protocol;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.Tag;

@RequiredArgsConstructor
public abstract class DefinedPacket
{

    public static void writeString(String s, ByteBuf buf)
    {
        writeString( s, buf, Short.MAX_VALUE );
    }

    public static void writeString(String s, ByteBuf buf, int maxLength)
    {
        if ( s.length() > maxLength )
        {
            throw new OverflowPacketException( "Cannot send string longer than " + maxLength + " (got " + s.length() + " characters)" );
        }

        byte[] b = s.getBytes( Charsets.UTF_8 );
        if ( b.length > maxLength * 3 )
        {
            throw new OverflowPacketException( "Cannot send string longer than " + ( maxLength * 3 ) + " (got " + b.length + " bytes)" );
        }

        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public static String readString(ByteBuf buf)
    {
        return readString( buf, Short.MAX_VALUE );
    }

    public static String readString(ByteBuf buf, int maxLen)
    {
        int len = readVarInt( buf );
        if ( len > maxLen * 3 )
        {
            throw new OverflowPacketException( "Cannot receive string longer than " + maxLen * 3 + " (got " + len + " bytes)" );
        }

        String s = buf.toString( buf.readerIndex(), len, Charsets.UTF_8 );
        buf.readerIndex( buf.readerIndex() + len );

        if ( s.length() > maxLen )
        {
            throw new OverflowPacketException( "Cannot receive string longer than " + maxLen + " (got " + s.length() + " characters)" );
        }

        return s;
    }

    public static void writeArray(byte[] b, ByteBuf buf)
    {
        if ( b.length > Short.MAX_VALUE )
        {
            throw new OverflowPacketException( "Cannot send byte array longer than Short.MAX_VALUE (got " + b.length + " bytes)" );
        }
        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public static byte[] toArray(ByteBuf buf)
    {
        byte[] ret = new byte[ buf.readableBytes() ];
        buf.readBytes( ret );

        return ret;
    }

    public static byte[] readArray(ByteBuf buf)
    {
        return readArray( buf, buf.readableBytes() );
    }

    public static byte[] readArray(ByteBuf buf, int limit)
    {
        int len = readVarInt( buf );
        if ( len > limit )
        {
            throw new OverflowPacketException( "Cannot receive byte array longer than " + limit + " (got " + len + " bytes)" );
        }
        byte[] ret = new byte[ len ];
        buf.readBytes( ret );
        return ret;
    }

    public static int[] readVarIntArray(ByteBuf buf)
    {
        int len = readVarInt( buf );
        int[] ret = new int[ len ];

        for ( int i = 0; i < len; i++ )
        {
            ret[i] = readVarInt( buf );
        }

        return ret;
    }

    public static void writeStringArray(List<String> s, ByteBuf buf)
    {
        writeVarInt( s.size(), buf );
        for ( String str : s )
        {
            writeString( str, buf );
        }
    }

    public static List<String> readStringArray(ByteBuf buf)
    {
        int len = readVarInt( buf );
        List<String> ret = new ArrayList<>( len );
        for ( int i = 0; i < len; i++ )
        {
            ret.add( readString( buf ) );
        }
        return ret;
    }

    public static int readVarInt(ByteBuf input)
    {
        return readVarInt( input, 5 );
    }

    public static int readVarInt(ByteBuf input, int maxBytes)
    {
        int out = 0;
        int bytes = 0;
        byte in;
        while ( true )
        {
            in = input.readByte();

            out |= ( in & 0x7F ) << ( bytes++ * 7 );

            if ( bytes > maxBytes )
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

    public static int readVarShort(ByteBuf buf)
    {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ( ( low & 0x8000 ) != 0 )
        {
            low = low & 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return ( ( high & 0xFF ) << 15 ) | low;
    }

    public static void writeVarShort(ByteBuf buf, int toWrite)
    {
        int low = toWrite & 0x7FFF;
        int high = ( toWrite & 0x7F8000 ) >> 15;
        if ( high != 0 )
        {
            low = low | 0x8000;
        }
        buf.writeShort( low );
        if ( high != 0 )
        {
            buf.writeByte( high );
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

    public static void writeProperties(Property[] properties, ByteBuf buf)
    {
        if ( properties == null )
        {
            writeVarInt( 0, buf );
            return;
        }

        writeVarInt( properties.length, buf );
        for ( Property prop : properties )
        {
            writeString( prop.getName(), buf );
            writeString( prop.getValue(), buf );
            if ( prop.getSignature() != null )
            {
                buf.writeBoolean( true );
                writeString( prop.getSignature(), buf );
            } else
            {
                buf.writeBoolean( false );
            }
        }
    }

    public static Property[] readProperties(ByteBuf buf)
    {
        Property[] properties = new Property[ DefinedPacket.readVarInt( buf ) ];
        for ( int j = 0; j < properties.length; j++ )
        {
            String name = readString( buf );
            String value = readString( buf );
            if ( buf.readBoolean() )
            {
                properties[j] = new Property( name, value, DefinedPacket.readString( buf ) );
            } else
            {
                properties[j] = new Property( name, value );
            }
        }

        return properties;
    }

    public static void writePublicKey(PlayerPublicKey publicKey, ByteBuf buf)
    {
        if ( publicKey != null )
        {
            buf.writeBoolean( true );
            buf.writeLong( publicKey.getExpiry() );
            writeArray( publicKey.getKey(), buf );
            writeArray( publicKey.getSignature(), buf );
        } else
        {
            buf.writeBoolean( false );
        }
    }

    public static PlayerPublicKey readPublicKey(ByteBuf buf)
    {
        if ( buf.readBoolean() )
        {
            return new PlayerPublicKey( buf.readLong(), readArray( buf, 512 ), readArray( buf, 4096 ) );
        }

        return null;
    }

    public static Tag readTag(ByteBuf input)
    {
        Tag tag = NamedTag.read( new DataInputStream( new ByteBufInputStream( input ) ) );
        Preconditions.checkArgument( !tag.isError(), "Error reading tag: %s", tag.error() );
        return tag;
    }

    public static void writeTag(Tag tag, ByteBuf output)
    {
        try
        {
            tag.write( new DataOutputStream( new ByteBufOutputStream( output ) ) );
        } catch ( IOException ex )
        {
            throw new RuntimeException( "Exception writing tag", ex );
        }
    }

    public static <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumset, Class<E> oclass, ByteBuf buf)
    {
        E[] enums = oclass.getEnumConstants();
        BitSet bits = new BitSet( enums.length );

        for ( int i = 0; i < enums.length; ++i )
        {
            bits.set( i, enumset.contains( enums[i] ) );
        }

        writeFixedBitSet( bits, enums.length, buf );
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> oclass, ByteBuf buf)
    {
        E[] enums = oclass.getEnumConstants();
        BitSet bits = readFixedBitSet( enums.length, buf );
        EnumSet<E> set = EnumSet.noneOf( oclass );

        for ( int i = 0; i < enums.length; ++i )
        {
            if ( bits.get( i ) )
            {
                set.add( enums[i] );
            }
        }

        return set;
    }

    public static BitSet readFixedBitSet(int i, ByteBuf buf)
    {
        byte[] bits = new byte[ ( i + 8 ) >> 3 ];
        buf.readBytes( bits );

        return BitSet.valueOf( bits );
    }

    public static void writeFixedBitSet(BitSet bits, int size, ByteBuf buf)
    {
        if ( bits.length() > size )
        {
            throw new OverflowPacketException( "BitSet too large (expected " + size + " got " + bits.size() + ")" );
        }
        buf.writeBytes( Arrays.copyOf( bits.toByteArray(), ( size + 8 ) >> 3 ) );
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
