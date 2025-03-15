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
public class ByteTag implements Tag
{
    private byte value;

    @Override
    public void read(DataInput input, NbtLimiter limiter) throws IOException
    {
        limiter.countBytes( OBJECT_HEADER + Byte.BYTES );
        value = input.readByte();
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
        output.write( value );
    }

    @Override
    public byte getId()
    {
        return Tag.BYTE;
    }
}
