package net.md_5.bungee.nbt.type;

import com.google.common.base.Preconditions;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.exception.NbtFormatException;
import net.md_5.bungee.nbt.limit.NbtLimiter;

@Data
public class ListTag implements TypedTag
{
    public static final int LIST_HEADER = 12;

    private List<TypedTag> value;
    private byte listType;

    public ListTag(List<TypedTag> value, byte listType)
    {
        this.value = value;
        this.listType = listType;
    }

    public ListTag()
    {
        this( new ArrayList<>(), Tag.END );
    }

    @Override
    public void read(DataInput input, NbtLimiter limiter) throws IOException
    {
        limiter.push();
        limiter.countBytes( OBJECT_HEADER + LIST_HEADER + ARRAY_HEADER + Byte.BYTES + Integer.BYTES );
        listType = input.readByte();
        int length = input.readInt();

        if ( listType == Tag.END && length > 0 )
        {
            throw new NbtFormatException( "Missing type in ListTag" );
        }

        limiter.countBytes( length, OBJECT_REFERENCE );
        List<TypedTag> tagList = new ArrayList<>( length );
        for ( int i = 0; i < length; i++ )
        {
            tagList.add( Tag.readById( listType, input, limiter ) );
        }
        limiter.pop();

        value = tagList;
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
        Preconditions.checkNotNull( value, "list value cannot be null" );
        if ( listType == Tag.END && !value.isEmpty() )
        {
            throw new NbtFormatException( "Missing type in ListTag" );
        }
        output.writeByte( listType );
        output.writeInt( value.size() );
        for ( TypedTag tag : value )
        {
            if ( tag.getId() != listType )
            {
                throw new NbtFormatException( "ListTag type mismatch" );
            }
            tag.write( output );
        }
    }

    @Override
    public byte getId()
    {
        return Tag.LIST;
    }

    public TypedTag get(int index)
    {
        return value.get( index );
    }

    public int size()
    {
        return value.size();
    }
}
