package net.md_5.bungee.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.limit.NbtLimiter;

@Data
@NoArgsConstructor
public class EndTag implements Tag
{
    public static final EndTag INSTANCE = new EndTag();

    @Override
    public void read(DataInput input, NbtLimiter limiter)
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
