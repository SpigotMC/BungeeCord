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
public class LongTag implements Tag
{
    private long value;

    @Override
    public void read(DataInput input, NbtLimiter limiter) throws IOException
    {
        limiter.countBytes( OBJECT_HEADER + Long.BYTES );
        value = input.readLong();
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
        output.writeLong( value );
    }

    @Override
    public byte getId()
    {
        return Tag.LONG;
    }
}
