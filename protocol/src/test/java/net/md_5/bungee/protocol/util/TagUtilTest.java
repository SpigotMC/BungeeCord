package net.md_5.bungee.protocol.util;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.md_5.bungee.nbt.Tag;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.type.ByteArrayTag;
import net.md_5.bungee.nbt.type.CompoundTag;
import net.md_5.bungee.nbt.type.IntArrayTag;
import net.md_5.bungee.nbt.type.IntTag;
import net.md_5.bungee.nbt.type.ListTag;
import net.md_5.bungee.nbt.type.LongArrayTag;
import net.md_5.bungee.nbt.type.StringTag;
import org.junit.jupiter.api.Test;

public class TagUtilTest
{

    private static final Gson GSON = new Gson();

    private static void testDissembleReassemble(String json)
    {
        JsonElement parsedJson = GSON.fromJson( json, JsonElement.class );
        TypedTag nbt = TagUtil.fromJson( parsedJson );
        JsonElement convertedElement = TagUtil.toJson( nbt );

        String convertedJson = GSON.toJson( convertedElement );
        assertEquals( json, convertedJson );
    }

    @Test
    public void testStringLiteral()
    {
        // this test only passes if the CompoundTags are backed by a LinkedHashMap
        testDissembleReassemble( "{\"text\":\"\",\"extra\":[\"hello\",{\"text\":\"there\",\"color\":\"#ff0000\"},{\"text\":\"friend\",\"font\":\"minecraft:default\"}]}" );
    }

    public void testCreateMixedList(JsonArray array)
    {
        TypedTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ListTag.class, tag );
        ListTag list = (ListTag) tag;
        assertEquals( Tag.COMPOUND, list.getListType() );
        assertEquals( array.size(), list.getValue().size() );

        for ( int i = 0; i < list.getValue().size(); i++ )
        {
            assertTrue( i < array.size() );

            Tag element = list.get( i );
            assertInstanceOf( CompoundTag.class, element );
            CompoundTag compound = (CompoundTag) element;

            JsonElement expected = array.get( i );

            if ( expected instanceof JsonObject )
            {
                assertEquals( TagUtil.fromJson( expected ), compound );
            } else
            {
                assertEquals( 1, compound.size() );
                Tag value = compound.get( "" );

                if ( expected instanceof JsonPrimitive )
                {
                    JsonPrimitive primitive = (JsonPrimitive) expected;

                    if ( primitive.isNumber() )
                    {
                        Number number = primitive.getAsNumber();

                        if ( number instanceof Integer )
                        {
                            assertInstanceOf( IntTag.class, value );

                            IntTag integer = (IntTag) value;
                            assertEquals( array.get( i ).getAsInt(), integer.getValue() );
                        }

                    } else if ( primitive.isString() )
                    {
                        assertInstanceOf( StringTag.class, value );

                        StringTag string = (StringTag) value;
                        assertEquals( array.get( i ).getAsString(), string.getValue() );
                    }
                }
            }
        }
    }

    @Test
    public void testMixedListWithInt()
    {
        JsonArray array = new JsonArray();
        array.add( 1 );
        array.add( "a" );

        testCreateMixedList( array );
    }

    @Test
    public void testMixedListWithString()
    {
        JsonArray array = new JsonArray();
        array.add( "a" );
        array.add( 1L );

        testCreateMixedList( array );
    }

    @Test
    public void testMixedListWithObject()
    {
        JsonObject compound = new JsonObject();
        compound.addProperty( "a", "b" );

        JsonArray array = new JsonArray();
        array.add( compound );
        array.add( 1L );

        testCreateMixedList( array );
    }

    @Test
    public void testCreateEmptyList()
    {
        JsonArray array = new JsonArray();
        TypedTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ListTag.class, tag );
        ListTag list = (ListTag) tag;
        assertEquals( 0, list.size() );
        assertEquals( Tag.END, list.getListType() );
    }

    @Test
    public void testCreateByteArray()
    {
        JsonArray array = new JsonArray();
        array.add( ( (byte) 1 ) );
        array.add( ( (byte) 2 ) );

        TypedTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ByteArrayTag.class, tag );
        ByteArrayTag byteArray = (ByteArrayTag) tag;
        assertEquals( 2, byteArray.getValue().length );

        for ( int i = 0; i < byteArray.getValue().length; i++ )
        {
            assertTrue( i < array.size() );

            byte item = byteArray.getValue()[i];
            assertEquals( array.get( i ).getAsByte(), item );
        }
    }

    @Test
    public void testCreateIntArray()
    {
        JsonArray array = new JsonArray();
        array.add( 1 );
        array.add( 2 );

        TypedTag tag = TagUtil.fromJson( array );
        assertInstanceOf( IntArrayTag.class, tag );
        IntArrayTag intArray = (IntArrayTag) tag;
        assertEquals( 2, intArray.getValue().length );

        for ( int i = 0; i < intArray.getValue().length; i++ )
        {
            assertTrue( i < array.size() );

            int item = intArray.getValue()[i];
            assertEquals( array.get( i ).getAsInt(), item );
        }
    }

    @Test
    public void testCreateLongArray()
    {
        JsonArray array = new JsonArray();
        array.add( 1L );
        array.add( 2L );

        TypedTag tag = TagUtil.fromJson( array );
        assertInstanceOf( LongArrayTag.class, tag );
        LongArrayTag intArray = (LongArrayTag) tag;
        assertEquals( 2, intArray.getValue().length );

        for ( int i = 0; i < intArray.getValue().length; i++ )
        {
            assertTrue( i < array.size() );

            long item = intArray.getValue()[i];
            assertEquals( array.get( i ).getAsLong(), item );
        }
    }

    @Test
    public void testCreateStringList()
    {
        JsonArray array = new JsonArray();
        array.add( "a" );
        array.add( "b" );

        TypedTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ListTag.class, tag );
        ListTag list = (ListTag) tag;
        assertEquals( Tag.STRING, list.getListType() );
        assertEquals( 2, list.size() );

        for ( int i = 0; i < list.size(); i++ )
        {
            assertTrue( i < array.size() );

            Tag item = list.get( i );
            assertInstanceOf( StringTag.class, item );

            StringTag string = (StringTag) item;
            assertEquals( array.get( i ).getAsString(), string.getValue() );
        }
    }

    @Test
    public void testRecursive()
    {
        JsonElement jsonElement = JsonParser.parseString( "{\"extra\":[{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"}],\"text\":\"\"},{\"extra\":[{\"extra\":[{\"color\":\"#FF0000\",\"text\":\"f\"},{\"color\":\"#00FFFF\",\"text\":\"<\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}],\"text\":\"\"}\n" );

        long start = System.currentTimeMillis();
        TagUtil.fromJson( jsonElement );
        long end = System.currentTimeMillis();

        System.out.println( "Time passed: " + ( end - start ) + "ms" );
    }
}
