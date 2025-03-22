package net.md_5.bungee.nbt;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import net.md_5.bungee.nbt.exception.NbtLimitException;
import net.md_5.bungee.nbt.limit.NbtLimiter;
import net.md_5.bungee.nbt.type.ByteArrayTag;
import net.md_5.bungee.nbt.type.ListTag;
import org.junit.jupiter.api.Test;

public class NbtLimiterTest
{
    @Test
    public void testNbtLimiter()
    {
        assertThrows( NbtLimitException.class, () ->
        {
            ByteArrayTag byteArrayTag = new ByteArrayTag( new byte[1000] );
            byte[] arr = Tag.toByteArray( byteArrayTag );
            Tag.fromByteArray( arr, new NbtLimiter( 100, 1 ) );
        } );

        assertDoesNotThrow( () ->
        {
            ByteArrayTag byteArrayTag = new ByteArrayTag( new byte[1000] );
            byte[] arr = Tag.toByteArray( byteArrayTag );
            Tag.fromByteArray( arr, new NbtLimiter( 99999999, 1 ) );
        } );
    }

    @Test
    public void testDepth()
    {
        assertThrows( NbtLimitException.class, () ->
        {
            ListTag root = new ListTag( new ArrayList<>(), Tag.LIST );
            root.getValue().add( new ListTag( new ArrayList<>(), Tag.LIST ) );

            byte[] arr = Tag.toByteArray( root );
            Tag.fromByteArray( arr, new NbtLimiter( 100, 1 ) );
        } );

        assertDoesNotThrow( () ->
        {
            ListTag root = new ListTag( new ArrayList<>(), Tag.LIST );
            root.getValue().add( new ListTag( new ArrayList<>(), Tag.LIST ) );

            byte[] arr = Tag.toByteArray( root );
            Tag.fromByteArray( arr, new NbtLimiter( 100, 2 ) );
        } );
    }
}
