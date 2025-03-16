package net.md_5.bungee.nbt;

import net.md_5.bungee.nbt.exception.NbtFormatException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NbtIOTest
{

    @Test
    public void testByteTag() throws IOException
    {
        {
            int value = 0;
            ByteTag byteTag = new ByteTag( (byte) value );
            byte[] deserialized = Tag.toByteArray( byteTag );
            ByteTag reSerialized = (ByteTag) Tag.fromByteArray(deserialized);
            assertEquals(byteTag, reSerialized);
        }
        {
            int value = Byte.MAX_VALUE;
            ByteTag byteTag = new ByteTag((byte) value);
            byte[] deserialized = Tag.toByteArray(byteTag);
            ByteTag reSerialized = (ByteTag) Tag.fromByteArray(deserialized);
            assertEquals(byteTag, reSerialized);
        }
        {
            int value = Byte.MIN_VALUE;
            ByteTag byteTag = new ByteTag((byte) value);
            byte[] deserialized = Tag.toByteArray(byteTag);
            ByteTag reSerialized = (ByteTag) Tag.fromByteArray(deserialized);
            assertEquals(byteTag, reSerialized);
        }
    }

    @Test
    public void testShortTag() throws IOException {
        {
            int value = 0;
            ShortTag tag = new ShortTag((short) value);
            byte[] deserialized = Tag.toByteArray(tag);
            ShortTag reSerialized = (ShortTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            int value = Short.MAX_VALUE;
            ShortTag tag = new ShortTag((short) value);
            byte[] deserialized = Tag.toByteArray(tag);
            ShortTag reSerialized = (ShortTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            int value = Short.MIN_VALUE;
            ShortTag tag = new ShortTag((short) value);
            byte[] deserialized = Tag.toByteArray(tag);
            ShortTag reSerialized = (ShortTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testIntTag() throws IOException {
        {
            int value = 0;
            IntTag tag = new IntTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            IntTag reSerialized = (IntTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            int value = Integer.MAX_VALUE;
            IntTag tag = new IntTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            IntTag reSerialized = (IntTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            int value = Integer.MIN_VALUE;
            IntTag tag = new IntTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            IntTag reSerialized = (IntTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testLongTag() throws IOException {
        {
            long value = 0;
            LongTag tag = new LongTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            LongTag reSerialized = (LongTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            long value = Long.MAX_VALUE;
            LongTag tag = new LongTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            LongTag reSerialized = (LongTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            long value = Long.MIN_VALUE;
            LongTag tag = new LongTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            LongTag reSerialized = (LongTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testDoubleTag() throws IOException {
        {
            double value = 0d;
            DoubleTag tag = new DoubleTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            DoubleTag reSerialized = (DoubleTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            double value = Double.MAX_VALUE;
            DoubleTag tag = new DoubleTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            DoubleTag reSerialized = (DoubleTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            double value = Double.MIN_VALUE;
            DoubleTag tag = new DoubleTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            DoubleTag reSerialized = (DoubleTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            double value = Double.NaN;
            DoubleTag tag = new DoubleTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            DoubleTag reSerialized = (DoubleTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            double value = Double.POSITIVE_INFINITY;
            DoubleTag tag = new DoubleTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            DoubleTag reSerialized = (DoubleTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            double value = Double.NEGATIVE_INFINITY;
            DoubleTag tag = new DoubleTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            DoubleTag reSerialized = (DoubleTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testFloatTag() throws IOException {
        {
            float value = 0f;
            FloatTag tag = new FloatTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            FloatTag reSerialized = (FloatTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            float value = Float.MAX_VALUE;
            FloatTag tag = new FloatTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            FloatTag reSerialized = (FloatTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            float value = Float.MIN_VALUE;
            FloatTag tag = new FloatTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            FloatTag reSerialized = (FloatTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            float value = Float.NaN;
            FloatTag tag = new FloatTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            FloatTag reSerialized = (FloatTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            float value = Float.POSITIVE_INFINITY;
            FloatTag tag = new FloatTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            FloatTag reSerialized = (FloatTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            float value = Float.NEGATIVE_INFINITY;
            FloatTag tag = new FloatTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            FloatTag reSerialized = (FloatTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testStringTag() throws IOException {
        {
            String value = "Outfluencer";
            StringTag tag = new StringTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            StringTag reSerialized = (StringTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            String value = "";
            StringTag tag = new StringTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            StringTag reSerialized = (StringTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            String value = String.valueOf(System.currentTimeMillis());
            StringTag tag = new StringTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            StringTag reSerialized = (StringTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testByteArrayTag() throws IOException {
        {
            byte[] value = new byte[1 << 20];
            ThreadLocalRandom.current().nextBytes(value);
            ByteArrayTag tag = new ByteArrayTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            ByteArrayTag reSerialized = (ByteArrayTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            byte[] value = new byte[0];
            ThreadLocalRandom.current().nextBytes(value);
            ByteArrayTag tag = new ByteArrayTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            ByteArrayTag reSerialized = (ByteArrayTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testIntArrayTag() throws IOException {
        {
            int[] value = new int[1 << 20];
            for (int i = 0; i < value.length; i++) {
                value[i] = ThreadLocalRandom.current().nextInt();
            }
            IntArrayTag tag = new IntArrayTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            IntArrayTag reSerialized = (IntArrayTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            int[] value = new int[0];
            IntArrayTag tag = new IntArrayTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            IntArrayTag reSerialized = (IntArrayTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testLongArrayTag() throws IOException {
        {
            long[] value = new long[1 << 20];
            for (int i = 0; i < value.length; i++) {
                value[i] = ThreadLocalRandom.current().nextLong();
            }
            LongArrayTag tag = new LongArrayTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            LongArrayTag reSerialized = (LongArrayTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
        {
            long[] value = new long[0];
            LongArrayTag tag = new LongArrayTag(value);
            byte[] deserialized = Tag.toByteArray(tag);
            LongArrayTag reSerialized = (LongArrayTag) Tag.fromByteArray(deserialized);
            assertEquals(tag, reSerialized);
        }
    }

    @Test
    public void testListTag() throws IOException {
        {
            List<Tag> tags = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                tags.add(new IntTag(i));
            }
            ListTag listTag = new ListTag(tags, Tag.INT);
            byte[] deserialized = Tag.toByteArray(listTag);
            ListTag reSerialized = (ListTag) Tag.fromByteArray(deserialized);
            assertEquals(reSerialized.getValue(), tags);
        }

        {
            List<Tag> tags = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                tags.add(new IntTag(i));
            }
            tags.add(new ByteTag(Byte.MIN_VALUE));
            assertThrows(NbtFormatException.class, () -> Tag.toByteArray(new ListTag(tags, Tag.INT)));
        }
        assertThrows(NbtFormatException.class, () -> Tag.toByteArray(new ListTag(Collections.singletonList(new EndTag()), Tag.END)));
    }

    @Test
    public void testCompoundTag() throws IOException {
        {
            Map<String, Tag> map = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                map.put("" + i, new IntTag(i));
                map.put("a" + i, new ByteTag((byte) i));
                map.put("b" + i, new ShortTag((short) i));
                map.put("c" + i, new LongTag(i));
                map.put("f" + i, new FloatTag(i));
                map.put("d" + i, new DoubleTag(i));
            }
            CompoundTag compoundTag = new CompoundTag(map);
            byte[] deserialized = Tag.toByteArray(compoundTag);
            CompoundTag reSerialized = (CompoundTag) Tag.fromByteArray(deserialized);
            assertEquals(reSerialized, compoundTag);
        }
        {
            Map<String, Tag> map = new HashMap<>();
            map.put("", new EndTag());
            CompoundTag compoundTag = new CompoundTag(map);
            assertThrows(NbtFormatException.class, () -> Tag.toByteArray(compoundTag));
        }
    }
}