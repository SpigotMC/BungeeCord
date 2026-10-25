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
public class StringTag implements TypedTag
{

    private String value;

    @Override
    public void read(DataInput input, NBTLimiter limiter) throws IOException
    {
        limiter.countBytes( OBJECT_HEADER + STRING_SIZE );
        String string = input.readUTF();
        limiter.countBytes( string.length(), Character.BYTES );
        value = string;
    }

    @Override
    public void write(DataOutput output) throws IOException
    {
        Preconditions.checkNotNull( value, "string value cannot be null" );

        output.writeUTF( value );
    }

    @Override
    public byte getId()
    {
        return Tag.STRING;
    }
}
