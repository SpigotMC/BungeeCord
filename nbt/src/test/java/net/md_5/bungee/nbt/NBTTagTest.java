package net.md_5.bungee.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.md_5.bungee.nbt.exception.NBTFormatException;
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
import org.junit.jupiter.api.Test;

public class NBTTagTest
{

    @Test
    public void testByteTag() throws IOException
    {
        byte[] tests = new byte[]
        {
            0, Byte.MAX_VALUE, Byte.MIN_VALUE
        };
        for ( byte value : tests )
        {
            ByteTag byteTag = new ByteTag( value );
            byte[] deserialized = Tag.toByteArray( byteTag );
            ByteTag reSerialized = (ByteTag) Tag.fromByteArray( deserialized );
            assertEquals( byteTag, reSerialized );
        }
    }

    @Test
    public void testShortTag() throws IOException
    {
        short[] tests = new short[]
        {
            0, Short.MAX_VALUE, Short.MIN_VALUE
        };
        for ( short value : tests )
        {
            ShortTag tag = new ShortTag( value );
            byte[] deserialized = Tag.toByteArray( tag );
            ShortTag reSerialized = (ShortTag) Tag.fromByteArray( deserialized );
            assertEquals( tag, reSerialized );
        }
    }

    @Test
    public void testIntTag() throws IOException
    {
        int[] tests = new int[]
        {
            0, Integer.MAX_VALUE, Integer.MIN_VALUE
        };
        for ( int value : tests )
        {
            IntTag tag = new IntTag( value );
            byte[] deserialized = Tag.toByteArray( tag );
            IntTag reSerialized = (IntTag) Tag.fromByteArray( deserialized );
            assertEquals( tag, reSerialized );
        }
    }

    @Test
    public void testLongTag() throws IOException
    {
        long[] tests = new long[]
        {
            0, Long.MAX_VALUE, Long.MIN_VALUE
        };
        for ( long value : tests )
        {
            LongTag tag = new LongTag( value );
            byte[] deserialized = Tag.toByteArray( tag );
            LongTag reSerialized = (LongTag) Tag.fromByteArray( deserialized );
            assertEquals( tag, reSerialized );
        }
    }

    @Test
    public void testDoubleTag() throws IOException
    {
        double[] tests = new double[]
        {
            0, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        };
        for ( double value : tests )
        {
            DoubleTag tag = new DoubleTag( value );
            byte[] deserialized = Tag.toByteArray( tag );
            DoubleTag reSerialized = (DoubleTag) Tag.fromByteArray( deserialized );
            assertEquals( tag, reSerialized );
        }
    }

    @Test
    public void testFloatTag() throws IOException
    {
        float[] tests = new float[]
        {
            0, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY
        };
        for ( float value : tests )
        {
            FloatTag tag = new FloatTag( value );
            byte[] deserialized = Tag.toByteArray( tag );
            FloatTag reSerialized = (FloatTag) Tag.fromByteArray( deserialized );
            assertEquals( tag, reSerialized );
        }
    }

    @Test
    public void testStringTag() throws IOException
    {
        String[] tests = new String[]
        {
            "Outfluencer", "", String.valueOf( System.currentTimeMillis() ), "BungeeCord", new Object().toString()
        };
        for ( String value : tests )
        {
            StringTag tag = new StringTag( value );
            byte[] deserialized = Tag.toByteArray( tag );
            StringTag reSerialized = (StringTag) Tag.fromByteArray( deserialized );
            assertEquals( tag, reSerialized );
        }
    }

    @Test
    public void testByteArrayTag() throws IOException
    {
        byte[] value = new byte[ 1024 ];
        ThreadLocalRandom.current().nextBytes( value );
        ByteArrayTag tag = new ByteArrayTag( value );
        byte[] deserialized = Tag.toByteArray( tag );
        ByteArrayTag reSerialized = (ByteArrayTag) Tag.fromByteArray( deserialized );
        assertEquals( tag, reSerialized );

        value = new byte[ 0 ];
        ThreadLocalRandom.current().nextBytes( value );
        tag = new ByteArrayTag( value );
        deserialized = Tag.toByteArray( tag );
        reSerialized = (ByteArrayTag) Tag.fromByteArray( deserialized );
        assertEquals( tag, reSerialized );
    }

    @Test
    public void testIntArrayTag() throws IOException
    {
        int[] value = new int[ 1024 ];
        for ( int i = 0; i < value.length; i++ )
        {
            value[i] = ThreadLocalRandom.current().nextInt();
        }
        IntArrayTag tag = new IntArrayTag( value );
        byte[] deserialized = Tag.toByteArray( tag );
        IntArrayTag reSerialized = (IntArrayTag) Tag.fromByteArray( deserialized );
        assertEquals( tag, reSerialized );

        value = new int[ 0 ];
        tag = new IntArrayTag( value );
        deserialized = Tag.toByteArray( tag );
        reSerialized = (IntArrayTag) Tag.fromByteArray( deserialized );
        assertEquals( tag, reSerialized );
    }

    @Test
    public void testLongArrayTag() throws IOException
    {
        long[] value = new long[ 1024 ];
        for ( int i = 0; i < value.length; i++ )
        {
            value[i] = ThreadLocalRandom.current().nextLong();
        }
        LongArrayTag tag = new LongArrayTag( value );
        byte[] deserialized = Tag.toByteArray( tag );
        LongArrayTag reSerialized = (LongArrayTag) Tag.fromByteArray( deserialized );
        assertEquals( tag, reSerialized );

        value = new long[ 0 ];
        tag = new LongArrayTag( value );
        deserialized = Tag.toByteArray( tag );
        reSerialized = (LongArrayTag) Tag.fromByteArray( deserialized );
        assertEquals( tag, reSerialized );

    }

    @Test
    public void testListTag() throws IOException
    {
        List<TypedTag> tags = new ArrayList<>();
        for ( int i = 0; i < 100; i++ )
        {
            tags.add( new IntTag( i ) );
        }
        ListTag listTag = new ListTag( tags, Tag.INT );
        byte[] deserialized = Tag.toByteArray( listTag );
        ListTag reSerialized = (ListTag) Tag.fromByteArray( deserialized );
        assertEquals( reSerialized.getValue(), tags );
        List<TypedTag> tags2 = new ArrayList<>();
        for ( int i = 0; i < 100; i++ )
        {
            tags2.add( new IntTag( i ) );
        }
        tags2.add( new ByteTag( Byte.MIN_VALUE ) );
        assertThrows( NBTFormatException.class, () -> Tag.toByteArray( new ListTag( tags2, Tag.INT ) ) );
        assertThrows( NBTFormatException.class, () -> Tag.toByteArray( new ListTag( Collections.singletonList( new EndTag() ), Tag.END ) ) );
    }

    @Test
    public void testCompoundTag() throws IOException
    {
        Map<String, TypedTag> map = new HashMap<>();
        for ( int i = 0; i < 100; i++ )
        {
            map.put( "" + i, new IntTag( i ) );
            map.put( "a" + i, new ByteTag( (byte) i ) );
            map.put( "b" + i, new ShortTag( (short) i ) );
            map.put( "c" + i, new LongTag( i ) );
            map.put( "f" + i, new FloatTag( i ) );
            map.put( "d" + i, new DoubleTag( i ) );
        }
        CompoundTag compoundTag = new CompoundTag( map );
        byte[] deserialized = Tag.toByteArray( compoundTag );
        CompoundTag reSerialized = (CompoundTag) Tag.fromByteArray( deserialized );
        assertEquals( reSerialized, compoundTag );
        Map<String, TypedTag> map2 = new LinkedHashMap<>();
        map2.put( "", new EndTag() );
        CompoundTag compoundTag2 = new CompoundTag( map2 );
        assertThrows( NBTFormatException.class, () -> Tag.toByteArray( compoundTag2 ) );
    }
}
