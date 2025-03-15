package net.md_5.bungee.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.limit.NbtLimiter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntArrayTag implements Tag
{
    private int[] value;

    @Override
    public void read(DataInput input, NbtLimiter limiter) throws IOException
    {
        limiter.countBytes( OBJECT_HEADER + ARRAY_HEADER + Integer.BYTES );
        int length = input.readInt();
        limiter.countBytes( length, Integer.BYTES );
        int[] data = new int[length];
        for ( int i = 0; i < length; i++ )
        {
            data[i] = input.readInt();
        }
        value = data;
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
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
