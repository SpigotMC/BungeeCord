package net.md_5.bungee.nbt.type;

import java.io.DataInput;
import java.io.DataOutput;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.limit.NBTLimiter;

@Data
@NoArgsConstructor
public class EndTag implements TypedTag
{

    public static final EndTag INSTANCE = new EndTag();

    @Override
    public void read(DataInput input, NBTLimiter limiter)
    {
        limiter.countBytes( OBJECT_HEADER );
    }

    @Override
    public void write(DataOutput output)
    {
    }

    @Override
    public byte getId()
    {
        return Tag.END;
    }
}
