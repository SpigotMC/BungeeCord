package net.md_5.bungee.nbt;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import net.md_5.bungee.nbt.exception.NBTLimitException;
import net.md_5.bungee.nbt.limit.NBTLimiter;
import net.md_5.bungee.nbt.type.ByteArrayTag;
import net.md_5.bungee.nbt.type.ListTag;
import org.junit.jupiter.api.Test;

public class NBTLimiterTest
{

    @Test
    public void testNbtLimiter()
    {
        assertThrows( NBTLimitException.class, () ->
        {
            ByteArrayTag byteArrayTag = new ByteArrayTag( new byte[ 1000 ] );
            byte[] arr = Tag.toByteArray( byteArrayTag );
            Tag.fromByteArray( arr, new NBTLimiter( 100, 1 ) );
        } );

        assertDoesNotThrow( () ->
        {
            ByteArrayTag byteArrayTag = new ByteArrayTag( new byte[ 1000 ] );
            byte[] arr = Tag.toByteArray( byteArrayTag );
            Tag.fromByteArray( arr, new NBTLimiter( 99999999, 1 ) );
        } );
    }

    @Test
    public void testDepth()
    {
        assertThrows( NBTLimitException.class, () ->
        {
            ListTag root = new ListTag( new ArrayList<>(), Tag.LIST );
            root.getValue().add( new ListTag( new ArrayList<>(), Tag.LIST ) );

            byte[] arr = Tag.toByteArray( root );
            Tag.fromByteArray( arr, new NBTLimiter( 100, 1 ) );
        } );

        assertDoesNotThrow( () ->
        {
            ListTag root = new ListTag( new ArrayList<>(), Tag.LIST );
            root.getValue().add( new ListTag( new ArrayList<>(), Tag.LIST ) );

            byte[] arr = Tag.toByteArray( root );
            Tag.fromByteArray( arr, new NBTLimiter( 100, 2 ) );
        } );
    }
}
