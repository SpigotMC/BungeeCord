package net.md_5.bungee.protocol;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import se.llbit.nbt.ByteArrayTag;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntArrayTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.LongArrayTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

public class TagUtilTest
{

    private static final Gson GSON = new Gson();

    private static void testDissembleReassemble(String json)
    {
        JsonElement parsedJson = GSON.fromJson( json, JsonElement.class );
        SpecificTag nbt = TagUtil.fromJson( parsedJson );
        JsonElement convertedElement = TagUtil.toJson( nbt );

        String convertedJson = GSON.toJson( convertedElement );
        assertEquals( json, convertedJson );
    }

    @Test
    public void testStringLiteral()
    {
        testDissembleReassemble( "{\"text\":\"\",\"extra\":[\"hello\",{\"text\":\"there\",\"color\":\"#ff0000\"},{\"text\":\"friend\",\"font\":\"minecraft:default\"}]}" );
    }

    public void testCreateMixedList(JsonArray array)
    {
        SpecificTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ListTag.class, tag );
        ListTag list = (ListTag) tag;
        assertEquals( SpecificTag.TAG_COMPOUND, list.getType() );
        assertEquals( array.size(), list.size() );

        for ( int i = 0; i < list.size(); i++ )
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
                            assertEquals( array.get( i ).getAsInt(), integer.getData() );
                        }

                    } else if ( primitive.isString() )
                    {
                        assertInstanceOf( StringTag.class, value );

                        StringTag string = (StringTag) value;
                        assertEquals( array.get( i ).getAsString(), string.getData() );
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
        SpecificTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ListTag.class, tag );
        ListTag list = (ListTag) tag;
        assertEquals( 0, list.size() );
        assertEquals( Tag.TAG_END, list.getType() );
    }

    @Test
    public void testCreateByteArray()
    {
        JsonArray array = new JsonArray();
        array.add( ( (byte) 1 ) );
        array.add( ( (byte) 2 ) );

        SpecificTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ByteArrayTag.class, tag );
        ByteArrayTag byteArray = (ByteArrayTag) tag;
        assertEquals( 2, byteArray.getData().length );

        for ( int i = 0; i < byteArray.getData().length; i++ )
        {
            assertTrue( i < array.size() );

            byte item = byteArray.getData()[i];
            assertEquals( array.get( i ).getAsByte(), item );
        }
    }

    @Test
    public void testCreateIntArray()
    {
        JsonArray array = new JsonArray();
        array.add( 1 );
        array.add( 2 );

        SpecificTag tag = TagUtil.fromJson( array );
        assertInstanceOf( IntArrayTag.class, tag );
        IntArrayTag intArray = (IntArrayTag) tag;
        assertEquals( 2, intArray.getData().length );

        for ( int i = 0; i < intArray.getData().length; i++ )
        {
            assertTrue( i < array.size() );

            int item = intArray.getData()[i];
            assertEquals( array.get( i ).getAsInt(), item );
        }
    }

    @Test
    public void testCreateLongArray()
    {
        JsonArray array = new JsonArray();
        array.add( 1L );
        array.add( 2L );

        SpecificTag tag = TagUtil.fromJson( array );
        assertInstanceOf( LongArrayTag.class, tag );
        LongArrayTag intArray = (LongArrayTag) tag;
        assertEquals( 2, intArray.getData().length );

        for ( int i = 0; i < intArray.getData().length; i++ )
        {
            assertTrue( i < array.size() );

            long item = intArray.getData()[i];
            assertEquals( array.get( i ).getAsLong(), item );
        }
    }

    @Test
    public void testCreateStringList()
    {
        JsonArray array = new JsonArray();
        array.add( "a" );
        array.add( "b" );

        SpecificTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ListTag.class, tag );
        ListTag list = (ListTag) tag;
        assertEquals( SpecificTag.TAG_STRING, list.getType() );
        assertEquals( 2, list.size() );

        for ( int i = 0; i < list.size(); i++ )
        {
            assertTrue( i < array.size() );

            Tag item = list.get( i );
            assertInstanceOf( StringTag.class, item );

            StringTag string = (StringTag) item;
            assertEquals( array.get( i ).getAsString(), string.getData() );
        }
    }
}
