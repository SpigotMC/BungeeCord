package net.md_5.bungee.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;
import net.md_5.bungee.nbt.exception.NbtFormatException;
import net.md_5.bungee.nbt.limit.NbtLimiter;

public interface Tag
{

    int OBJECT_HEADER = 8;
    int ARRAY_HEADER = 12;
    int STRING_SIZE = 28;
    int OBJECT_REFERENCE = 4;

    Supplier<? extends Tag>[] CONSTRUCTORS = new Supplier[]
    {
        EndTag::new,
        ByteTag::new,
        ShortTag::new,
        IntTag::new,
        LongTag::new,
        FloatTag::new,
        DoubleTag::new,
        ByteArrayTag::new,
        StringTag::new,
        ListTag::new,
        CompoundTag::new,
        IntArrayTag::new,
        LongArrayTag::new
    };

    byte END = 0;
    byte BYTE = 1;
    byte SHORT = 2;
    byte INT = 3;
    byte LONG = 4;
    byte FLOAT = 5;
    byte DOUBLE = 6;
    byte BYTE_ARRAY = 7;
    byte STRING = 8;
    byte LIST = 9;
    byte COMPOUND = 10;
    byte INT_ARRAY = 11;
    byte LONG_ARRAY = 12;

    /**
     * Reads the data into this tag
     *
     * @param input   the input to read from
     * @param limiter the limiter for this read operation
     */
    void read(DataInput input, NbtLimiter limiter) throws IOException;

    /**
     * Writes this tag into a {@link DataOutput}
     *
     * @param output the output to write to
     */
    void write(DataOutput output) throws IOException;

    /**
     * Gets the id of this tags type
     *
     * @return the id related to this tags type
     */
    byte getId();

    /**
     * Reads the data of the {@link DataInput} and parses it into a {@link Tag}
     *
     * @param id      the nbt type
     * @param input   input to read from
     * @param limiter limitation of the read data
     * @return the initialized {@link Tag}
     */
    static Tag readById(byte id, DataInput input, NbtLimiter limiter) throws IOException
    {
        if ( id < END || id > LONG_ARRAY )
        {
            throw new NbtFormatException( "Invalid tag id: " + id );
        }
        Tag tag = CONSTRUCTORS[id].get();
        tag.read( input, limiter );
        return tag;
    }

    static byte[] toByteArray(Tag tag) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream( byteArrayOutputStream );
        dataOutputStream.writeByte( tag.getId() );
        tag.write( dataOutputStream );
        return byteArrayOutputStream.toByteArray();
    }

    static Tag fromByteArray(byte[] data) throws IOException
    {
        DataInputStream stream = new DataInputStream( new ByteArrayInputStream( data ) );
        byte type = stream.readByte();
        return Tag.readById( type, stream, NbtLimiter.unlimitedSize() );
    }
}
