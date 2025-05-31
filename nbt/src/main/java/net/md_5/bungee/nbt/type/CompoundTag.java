package net.md_5.bungee.nbt.type;

import com.google.common.base.Preconditions;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.exception.NBTFormatException;
import net.md_5.bungee.nbt.limit.NBTLimiter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompoundTag implements TypedTag
{

    private static final int MAP_SIZE_IN_BYTES = 48;
    private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
    //
    private Map<String, TypedTag> value;

    @Override
    public void read(DataInput input, NBTLimiter limiter) throws IOException
    {
        limiter.push();
        limiter.countBytes( MAP_SIZE_IN_BYTES );
        Map<String, TypedTag> map = new LinkedHashMap<>();
        for ( byte type; ( type = input.readByte() ) != Tag.END; )
        {
            String name = readString( input, limiter );
            TypedTag tag = Tag.readById( type, input, limiter );
            if ( map.put( name, tag ) == null )
            {
                limiter.countBytes( MAP_ENTRY_SIZE_IN_BYTES + OBJECT_REFERENCE );
            }
        }
        limiter.pop();
        value = map;
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
        Preconditions.checkNotNull( value, "compound tag map cannot be null" );
        for ( Map.Entry<String, TypedTag> entry : value.entrySet() )
        {
            String name = entry.getKey();
            TypedTag tag = entry.getValue();
            output.writeByte( tag.getId() );
            if ( tag.getId() == Tag.END )
            {
                throw new NBTFormatException( "invalid end tag in compound tag" );
            }
            writeString( name, output );
            tag.write( output );
        }
        output.writeByte( 0 );
    }

    @Override
    public byte getId()
    {
        return Tag.COMPOUND;
    }

    public static String readString(DataInput input, NBTLimiter limiter) throws IOException
    {
        limiter.countBytes( STRING_SIZE );
        String string = input.readUTF();
        limiter.countBytes( string.length(), Character.BYTES );
        return string;
    }

    public static void writeString(String string, DataOutput output) throws IOException
    {
        output.writeUTF( string );
    }

    public TypedTag get(String key)
    {
        return value.get( key );
    }

    public void put(String key, TypedTag tag)
    {
        value.put( key, tag );
    }

    public int size()
    {
        return value.size();
    }
}
