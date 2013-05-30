package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import org.junit.Assert;
import org.junit.Test;

public class PacketTest
{

    @Test
    public void testPackets() throws NoSuchMethodException
    {
        for ( short i = 0; i < 256; i++ )
        {
            ByteBuf buf = Unpooled.wrappedBuffer( new byte[]
            {
                (byte) i
            } );
            Class<? extends DefinedPacket> clazz = DefinedPacket.classes[i];
            if ( clazz != null )
            {
                Assert.assertTrue( "Packet " + clazz + " is not public", Modifier.isPublic( clazz.getModifiers() ) );
                DefinedPacket packet = DefinedPacket.packet( buf );
                Assert.assertTrue( "Could not create packet with id " + i + " and class " + clazz, packet != null );
                Assert.assertTrue( "Packet with id " + i + " does not have correct class (expected " + clazz + " but got " + packet.getClass(), packet.getClass() == clazz );
                Assert.assertTrue( "Packet " + clazz + " does not report correct id", packet.getId() == i );
                Assert.assertTrue( "Packet " + clazz + " does not have custom hash code", packet.hashCode() != System.identityHashCode( packet ) );
                Assert.assertTrue( "Packet " + clazz + " does not have custom toString", packet.toString().indexOf( '@' ) == -1 );
                Assert.assertTrue( "Packet " + clazz + " does not have private no args constructor", Modifier.isPrivate( clazz.getDeclaredConstructor().getModifiers() ) );

                for ( Field field : clazz.getDeclaredFields() )
                {
                    Assert.assertTrue( "Packet " + clazz + " has non private field " + field, Modifier.isPrivate( field.getModifiers() ) );
                }
            }
        }
    }
}
