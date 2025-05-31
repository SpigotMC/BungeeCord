package net.md_5.bungee.nbt.type;

import com.google.common.base.Preconditions;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.limit.NBTLimiter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntArrayTag implements TypedTag
{

    private int[] value;

    @Override
    public void read(DataInput input, NBTLimiter limiter) throws IOException
    {
        limiter.countBytes( OBJECT_HEADER + ARRAY_HEADER + Integer.BYTES );
        int length = input.readInt();
        limiter.countBytes( length, Integer.BYTES );
        int[] data = new int[ length ];
        for ( int i = 0; i < length; i++ )
        {
            data[i] = input.readInt();
        }
        value = data;
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
        Preconditions.checkNotNull( value, "int array value cannot be null" );
        output.writeInt( value.length );
        for ( int i : value )
        {
            output.writeInt( i );
        }
    }

    @Override
    public byte getId()
    {
        return Tag.INT_ARRAY;
    }
}
