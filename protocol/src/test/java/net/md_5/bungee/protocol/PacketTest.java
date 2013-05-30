package net.md_5.bungee.protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.md_5.bungee.protocol.packet.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import org.junit.Assert;
import org.junit.Test;

public class PacketTest
{

    @Test
    public void testPackets() throws Exception
    {
        AbstractPacketHandler handler = new AbstractPacketHandler()
        {
        };

        for ( short i = 0; i < 256; i++ )
        {
            Class<? extends DefinedPacket> clazz = Vanilla.getInstance().getClasses()[ i];
            if ( clazz != null )
            {
                Assert.assertTrue( "Packet " + clazz + " is not public", Modifier.isPublic( clazz.getModifiers() ) );
                DefinedPacket packet = Vanilla.packet( i, Vanilla.getInstance() );
                Assert.assertTrue( "Could not create packet with id " + i + " and class " + clazz, packet != null );
                Assert.assertTrue( "Packet with id " + i + " does not have correct class (expected " + clazz + " but got " + packet.getClass(), packet.getClass() == clazz );
                Assert.assertTrue( "Packet " + clazz + " does not report correct id", packet.getId() == i );
                Assert.assertTrue( "Packet " + clazz + " does not have custom hash code", packet.hashCode() != System.identityHashCode( packet ) );
                Assert.assertTrue( "Packet " + clazz + " does not have custom toString", packet.toString().indexOf( '@' ) == -1 );
                // TODO: Enable this test again in v2
                // Assert.assertTrue( "Packet " + clazz + " does not have private no args constructor", Modifier.isPrivate( clazz.getDeclaredConstructor().getModifiers() ) );

                for ( Field field : clazz.getDeclaredFields() )
                {
                    // TODO: Enable this test again in v2
                    // Assert.assertTrue( "Packet " + clazz + " has non private field " + field, Modifier.isPrivate( field.getModifiers() ) );
                }

                packet.handle( handler ); // Make sure there are no exceptions
            }
        }
    }
}
