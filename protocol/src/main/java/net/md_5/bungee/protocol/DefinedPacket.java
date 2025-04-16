package net.md_5.bungee.protocol;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentStyle;
import se.llbit.nbt.ErrorTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.Tag;

@RequiredArgsConstructor
public abstract class DefinedPacket
{

    public <T> T readNullable(Function<ByteBuf, T> reader, ByteBuf buf)
    {
        return buf.readBoolean() ? reader.apply( buf ) : null;
    }

    public <T> void writeNullable(T t0, BiConsumer<T, ByteBuf> writer, ByteBuf buf)
    {
        if ( t0 != null )
        {
            buf.writeBoolean( true );
            writer.accept( t0, buf );
        } else
        {
            buf.writeBoolean( false );
        }
    }

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

        byte[] b = s.getBytes( StandardCharsets.UTF_8 );
        if ( b.length > maxLength * 3 )
        {
            throw new OverflowPacketException( "Cannot send string longer than " + ( maxLength * 3 ) + " (got " + b.length + " bytes)" );
        }

        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public static <T> T readStringMapKey(ByteBuf buf, Map<String, T> map)
    {
        String string = readString( buf );
        T result = map.get( string );
        Preconditions.checkArgument( result != null, "Unknown string key %s", string );

        return result;
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

        String s = buf.toString( buf.readerIndex(), len, StandardCharsets.UTF_8 );
        buf.readerIndex( buf.readerIndex() + len );

        if ( s.length() > maxLen )
        {
            throw new OverflowPacketException( "Cannot receive string longer than " + maxLen + " (got " + s.length() + " characters)" );
        }

        return s;
    }

    public static Either<String, BaseComponent> readEitherBaseComponent(ByteBuf buf, int protocolVersion, boolean string)
    {
        return ( string ) ? Either.left( readString( buf ) ) : Either.right( readBaseComponent( buf, protocolVersion ) );
    }

    public static BaseComponent readBaseComponent(ByteBuf buf, int protocolVersion)
    {
        return readBaseComponent( buf, Short.MAX_VALUE, protocolVersion );
    }

    public static BaseComponent readBaseComponent(ByteBuf buf, int maxStringLength, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
        {
            SpecificTag nbt = (SpecificTag) readTag( buf, protocolVersion );
            JsonElement json = TagUtil.toJson( nbt );

            return ChatSerializer.forVersion( protocolVersion ).deserialize( json );
        } else
        {
            String string = readString( buf, maxStringLength );

            return ChatSerializer.forVersion( protocolVersion ).deserialize( string );
        }
    }

    public static ComponentStyle readComponentStyle(ByteBuf buf, int protocolVersion)
    {
        SpecificTag nbt = (SpecificTag) readTag( buf, protocolVersion );
        JsonElement json = TagUtil.toJson( nbt );

        return ChatSerializer.forVersion( protocolVersion ).deserializeStyle( json );
    }

    public static void writeEitherBaseComponent(Either<String, BaseComponent> message, ByteBuf buf, int protocolVersion)
    {
        if ( message.isLeft() )
        {
            writeString( message.getLeft(), buf );
        } else
        {
            writeBaseComponent( message.getRight(), buf, protocolVersion );
        }
    }

    public static void writeBaseComponent(BaseComponent message, ByteBuf buf, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
        {
            JsonElement json = ChatSerializer.forVersion( protocolVersion ).toJson( message );
            SpecificTag nbt = TagUtil.fromJson( json );

            writeTag( nbt, buf, protocolVersion );
        } else
        {
            String string = ChatSerializer.forVersion( protocolVersion ).toString( message );

            writeString( string, buf );
        }
    }

    public static void writeComponentStyle(ComponentStyle style, ByteBuf buf, int protocolVersion)
    {
        JsonElement json = ChatSerializer.forVersion( protocolVersion ).toJson( style );
        SpecificTag nbt = TagUtil.fromJson( json );

        writeTag( nbt, buf, protocolVersion );
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
                throw new OverflowPacketException( "VarInt too big (max " + maxBytes + ")" );
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

    public static void setVarInt(int value, ByteBuf output, int pos, int len)
    {
        switch ( len )
        {
            case 1:
                output.setByte( pos, value );
                break;
            case 2:
                output.setShort( pos, ( value & 0x7F | 0x80 ) << 8 | ( value >>> 7 & 0x7F ) );
                break;
            case 3:
                output.setMedium( pos, ( value & 0x7F | 0x80 ) << 16 | ( value >>> 7 & 0x7F | 0x80 ) << 8 | ( value >>> 14 & 0x7F ) );
                break;
            case 4:
                output.setInt( pos, ( value & 0x7F | 0x80 ) << 24 | ( value >>> 7 & 0x7F | 0x80 ) << 16 | ( value >>> 14 & 0x7F | 0x80 ) << 8 | ( value >>> 21 & 0x7F ) );
                break;
            case 5:
                output.setInt( pos, ( value & 0x7F | 0x80 ) << 24 | ( value >>> 7 & 0x7F | 0x80 ) << 16 | ( value >>> 14 & 0x7F | 0x80 ) << 8 | ( value >>> 21 & 0x7F | 0x80 ) );
                output.setByte( pos + 4, value >>> 28 );
                break;
            default:
                throw new IllegalArgumentException( "Invalid varint len: " + len );
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

    public static void writeNumberFormat(NumberFormat format, ByteBuf buf, int protocolVersion)
    {
        writeVarInt( format.getType().ordinal(), buf );
        switch ( format.getType() )
        {
            case BLANK:
                break;
            case STYLED:
                writeComponentStyle( (ComponentStyle) format.getValue(), buf, protocolVersion );
                break;
            case FIXED:
                writeBaseComponent( (BaseComponent) format.getValue(), buf, protocolVersion );
                break;
        }
    }

    public static NumberFormat readNumberFormat(ByteBuf buf, int protocolVersion)
    {
        int format = readVarInt( buf );
        switch ( format )
        {
            case 0:
                return new NumberFormat( NumberFormat.Type.BLANK, null );
            case 1:
                return new NumberFormat( NumberFormat.Type.STYLED, readComponentStyle( buf, protocolVersion ) );
            case 2:
                return new NumberFormat( NumberFormat.Type.FIXED, readBaseComponent( buf, protocolVersion ) );
            default:
                throw new IllegalArgumentException( "Unknown number format " + format );
        }
    }

    public static Tag readTag(ByteBuf input, int protocolVersion)
    {
        DataInputStream in = new DataInputStream( new ByteBufInputStream( input ) );
        Tag tag;
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_2 )
        {
            try
            {
                byte type = in.readByte();
                if ( type == 0 )
                {
                    return Tag.END;
                } else
                {
                    tag = SpecificTag.read( type, in );
                }
            } catch ( IOException ex )
            {
                tag = new ErrorTag( "IOException while reading tag type:\n" + ex.getMessage() );
            }
        } else
        {
            tag = NamedTag.read( in );
        }
        Preconditions.checkArgument( !tag.isError(), "Error reading tag: %s", tag.error() );
        return tag;
    }

    public static void writeTag(Tag tag, ByteBuf output, int protocolVersion)
    {
        DataOutputStream out = new DataOutputStream( new ByteBufOutputStream( output ) );
        try
        {
            if ( tag instanceof SpecificTag )
            {
                SpecificTag specificTag = (SpecificTag) tag;
                specificTag.writeType( out );
                specificTag.write( out );
            } else
            {
                tag.write( out );
            }
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
        byte[] bits = new byte[ ( i + 7 ) >> 3 ];
        buf.readBytes( bits );

        return BitSet.valueOf( bits );
    }

    public static void writeFixedBitSet(BitSet bits, int size, ByteBuf buf)
    {
        if ( bits.length() > size )
        {
            throw new OverflowPacketException( "BitSet too large (expected " + size + " got " + bits.size() + ")" );
        }
        buf.writeBytes( Arrays.copyOf( bits.toByteArray(), ( size + 7 ) >> 3 ) );
    }

    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Packet must implement read method" );
    }

    public void read(ByteBuf buf, Protocol protocol, ProtocolConstants.Direction direction, int protocolVersion)
    {
        read( buf, direction, protocolVersion );
    }

    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        read( buf );
    }

    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Packet must implement write method" );
    }

    public void write(ByteBuf buf, Protocol protocol, ProtocolConstants.Direction direction, int protocolVersion)
    {
        write( buf, direction, protocolVersion );
    }

    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        write( buf );
    }

    public Protocol nextProtocol()
    {
        return null;
    }

    public abstract void handle(AbstractPacketHandler handler) throws Exception;

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
