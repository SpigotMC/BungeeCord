package net.md_5.bungee.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.nbt.limit.NbtLimiter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamedTag
{
    private String name;
    private Tag tag;

    /**
     * Reads the data of the {@link DataInput} and parses it into a {@link NamedTag}
     *
     * @param input input to read from
     * @param limiter limitation of the read data
     * @return the initialized {@link Tag}
     */
    public static NamedTag read(@NonNull DataInput input, @NonNull NbtLimiter limiter) throws IOException
    {
        byte type = input.readByte();
        return new NamedTag( CompoundTag.readString( input, limiter ), Tag.readById( type, input, limiter ) );
    }

    /**
     * Write ta {@link NamedTag} into a {@link DataOutput}
     * @param tag the NamedTag to write
     * @param output the output to write to
     */
    public static void write(@NonNull NamedTag tag, @NonNull DataOutput output) throws IOException
    {
        output.writeByte( tag.getTag().getId() );
        CompoundTag.writeString( tag.getName(), output );
        tag.tag.write( output );
    }
}
