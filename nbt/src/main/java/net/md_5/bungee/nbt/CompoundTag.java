package net.md_5.bungee.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.exception.NbtFormatException;
import net.md_5.bungee.nbt.limit.NbtLimiter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompoundTag implements Tag
{
    private static final int MAP_SIZE_IN_BYTES = 48;
    private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;

    private Map<String, Tag> value;

    @Override
    public void read(DataInput input, NbtLimiter limiter) throws IOException
    {
        limiter.push();
        limiter.countBytes( MAP_SIZE_IN_BYTES );
        Map<String, Tag> map = new HashMap<>();
        for ( byte type; ( type = input.readByte() ) != Tag.END; )
        {
            String name = readString( input, limiter );
            Tag tag = Tag.readById( type, input, limiter );
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
        for ( Map.Entry<String, Tag> entry : value.entrySet() )
        {
            String name = entry.getKey();
            Tag tag = entry.getValue();
            output.writeByte( tag.getId() );
            if ( tag.getId() == Tag.END )
            {
                throw new NbtFormatException( "invalid end tag in compound tag" );
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

    static String readString(DataInput input, NbtLimiter limiter) throws IOException
    {
        limiter.countBytes( STRING_SIZE );
        String string = input.readUTF();
        limiter.countBytes( string.length(), Character.BYTES );
        return string;
    }

    static void writeString(String string, DataOutput output) throws IOException
    {
        output.writeUTF( string );
    }
}
