package net.md_5.bungee.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;
import net.md_5.bungee.nbt.exception.NBTFormatException;
import net.md_5.bungee.nbt.limit.NBTLimiter;
import net.md_5.bungee.nbt.type.ByteArrayTag;
import net.md_5.bungee.nbt.type.ByteTag;
import net.md_5.bungee.nbt.type.CompoundTag;
import net.md_5.bungee.nbt.type.DoubleTag;
import net.md_5.bungee.nbt.type.EndTag;
import net.md_5.bungee.nbt.type.FloatTag;
import net.md_5.bungee.nbt.type.IntArrayTag;
import net.md_5.bungee.nbt.type.IntTag;
import net.md_5.bungee.nbt.type.ListTag;
import net.md_5.bungee.nbt.type.LongArrayTag;
import net.md_5.bungee.nbt.type.LongTag;
import net.md_5.bungee.nbt.type.ShortTag;
import net.md_5.bungee.nbt.type.StringTag;

public interface Tag
{

    int OBJECT_HEADER = 8;
    int ARRAY_HEADER = 12;
    int STRING_SIZE = 28;
    int OBJECT_REFERENCE = 4;

    Supplier<? extends TypedTag>[] CONSTRUCTORS = new Supplier[]
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
     * Reads the data into this tag.
     *
     * @param input the input to read from
     * @param limiter the limiter for this read operation
     * @throws IOException if an exception occurs during io operations
     */
    void read(DataInput input, NBTLimiter limiter) throws IOException;

    /**
     * Writes this tag into a {@link DataOutput}.
     *
     * @param output the output to write to
     * @throws IOException if an exception occurs during io operations
     */
    void write(DataOutput output) throws IOException;

    /**
     * Reads a {@link Tag} from the given {@link DataInput}, based on the
     * specified tag type, with limitations of the {@link NBTLimiter}.
     *
     * @param id the nbt type
     * @param input the input to read from
     * @param limiter the limiter for this read operation
     * @return the deserialized {@link Tag}
     * @throws IOException if an exception occurs during io operations
     */
    static TypedTag readById(byte id, DataInput input, NBTLimiter limiter) throws IOException
    {
        if ( id < END || id > LONG_ARRAY )
        {
            throw new NBTFormatException( "Invalid tag id: " + id );
        }

        TypedTag tag = CONSTRUCTORS[id].get();
        tag.read( input, limiter );
        return tag;
    }

    /**
     * Reads a {@link NamedTag} from the given {@link DataInput}, with
     * limitations of the {@link NBTLimiter}.
     *
     * @param input the data input to read from
     * @param limiter the limiter for this read operation
     * @return the deserialized {@link NamedTag}
     * @throws IOException if an exception occurs during io operations
     */
    static NamedTag readNamedTag(DataInput input, NBTLimiter limiter) throws IOException
    {
        NamedTag namedTag = new NamedTag();
        namedTag.read( input, limiter );
        return namedTag;
    }

    /**
     * Serializes the given {@link TypedTag} into a byte array. This is the
     * inverse operation of {@link #fromByteArray(byte[])}.
     *
     * @param tag the tag to convert
     * @return the serialized byte array
     * @throws IOException if an exception occurs during io operations
     */
    static byte[] toByteArray(TypedTag tag) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream( byteArrayOutputStream );
        dataOutputStream.writeByte( tag.getId() );
        tag.write( dataOutputStream );
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserializes the given byte array into a {@link TypedTag}. This is the
     * inverse operation of {@link #toByteArray(TypedTag)}.
     *
     * @param data the byte array to read from
     * @return the deserialized {@link TypedTag}
     * @throws IOException if an exception occurs during io operations
     */
    static TypedTag fromByteArray(byte[] data) throws IOException
    {
        return fromByteArray( data, NBTLimiter.unlimitedSize() );
    }

    /**
     * Deserializes the given byte array into a {@link TypedTag}, with
     * limitations of the {@link NBTLimiter}.
     *
     * @param data the byte array to read from
     * @param limiter the limiter for this read operation
     * @return the deserialized {@link TypedTag}
     * @throws IOException if an exception occurs during io operations
     */
    static TypedTag fromByteArray(byte[] data, NBTLimiter limiter) throws IOException
    {
        DataInputStream stream = new DataInputStream( new ByteArrayInputStream( data ) );
        byte type = stream.readByte();
        return readById( type, stream, limiter );
    }
}
