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
public class ByteArrayTag implements TypedTag
{

    private byte[] value;

    @Override
    public void read(DataInput input, NBTLimiter limiter) throws IOException
    {
        limiter.countBytes( OBJECT_HEADER + ARRAY_HEADER + Integer.BYTES );
        int length = input.readInt();
        limiter.countBytes( length, Byte.BYTES );
        input.readFully( value = new byte[ length ] );
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
        Preconditions.checkNotNull( value, "byte array value cannot be null" );
        output.writeInt( value.length );
        output.write( value );
    }

    @Override
    public byte getId()
    {
        return Tag.BYTE_ARRAY;
    }
}
