package net.md_5.bungee.nbt.type;

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
public class LongTag implements TypedTag
{

    private long value;

    @Override
    public void read(DataInput input, NBTLimiter limiter) throws IOException
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
