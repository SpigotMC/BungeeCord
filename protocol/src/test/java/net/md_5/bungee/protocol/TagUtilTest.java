package net.md_5.bungee.protocol;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import se.llbit.nbt.*;

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

    @Test
    public void testCreateStringList()
    {
        JsonArray array = new JsonArray();
        array.add( "a" );
        array.add( "b" );

        SpecificTag tag = TagUtil.fromJson( array );
        assertInstanceOf( ListTag.class, tag );
        ListTag list = (ListTag) tag;
        assertEquals( SpecificTag.TAG_COMPOUND, list.getType() );
        assertEquals( 2, list.size() );

        for ( int i = 0; i < list.size(); i++ )
        {
            assertTrue( i < array.size() );

            Tag element = list.get( i );
            assertInstanceOf( CompoundTag.class, element );
            CompoundTag compound = (CompoundTag) element;
            assertEquals( 1, compound.size() );

            Tag value = compound.get( "" );
            assertInstanceOf( StringTag.class, value );

            StringTag string = (StringTag) value;
            assertEquals( array.get( i ).getAsString(), string.getData() );
        }
    }
}
