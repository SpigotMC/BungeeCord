package net.md_5.bungee.nbt;

import com.google.common.base.Preconditions;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.limit.NBTLimiter;
import net.md_5.bungee.nbt.type.CompoundTag;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamedTag implements Tag
{

    private String name;
    private TypedTag tag;

    /**
     * Reads the data of the {@link DataInput} and parses it into this
     * {@link NamedTag}.
     *
     * @param input input to read from
     * @param limiter limitation of the read data
     */
    @Override
    public void read(DataInput input, NBTLimiter limiter) throws IOException
    {
        byte type = input.readByte();
        name = CompoundTag.readString( input, limiter );
        tag = Tag.readById( type, input, limiter );
    }

    /**
     * Write this {@link NamedTag} into a {@link DataOutput}.
     *
     * @param output the output to write to
     */
    @Override
    public void write(DataOutput output) throws IOException
    {
        Preconditions.checkNotNull( name, "name cannot be null" );
        Preconditions.checkNotNull( tag, "tag cannot be null" );

        output.writeByte( tag.getId() );
        CompoundTag.writeString( name, output );
        tag.write( output );
    }
}
