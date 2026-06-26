package net.md_5.bungee.protocol.packet;

import static org.junit.jupiter.api.Assertions.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.DataInput;
import java.io.IOException;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import org.junit.jupiter.api.Test;

public class PluginMessageTest
{

    @Test
    public void testModerniseChannel()
    {
        assertEquals( PluginMessage.BUNGEE_CHANNEL_MODERN, PluginMessage.MODERNISE.apply( PluginMessage.BUNGEE_CHANNEL_LEGACY ) );
        assertEquals( PluginMessage.BUNGEE_CHANNEL_LEGACY, PluginMessage.MODERNISE.apply( PluginMessage.BUNGEE_CHANNEL_MODERN ) );
        assertEquals( "legacy:foo", PluginMessage.MODERNISE.apply( "FoO" ) );
        assertEquals( "foo:bar", PluginMessage.MODERNISE.apply( "foo:bar" ) );
    }

    @Test
    public void testReadModernProtocolToServer()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "bungeecord:main", buf );
        buf.writeBytes( new byte[]{1, 2, 3} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "BungeeCord", pm.getTag() );
        assertArrayEquals( new byte[]{1, 2, 3}, pm.getData() );
        buf.release();
    }

    @Test
    public void testReadModernProtocolToClient()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "bungeecord:main", buf );
        buf.writeBytes( new byte[]{4, 5, 6} );
        pm.read( buf, ProtocolConstants.Direction.TO_CLIENT, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "BungeeCord", pm.getTag() );
        assertArrayEquals( new byte[]{4, 5, 6}, pm.getData() );
        buf.release();
    }

    @Test
    public void testReadLegacyProtocol()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "BungeeCord", buf );
        buf.writeBytes( new byte[]{7, 8} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_12_2 );
        assertEquals( "BungeeCord", pm.getTag() );
        assertArrayEquals( new byte[]{7, 8}, pm.getData() );
        buf.release();
    }

    @Test
    public void testReadEmptyData()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "bungeecord:main", buf );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "BungeeCord", pm.getTag() );
        assertArrayEquals( new byte[]{}, pm.getData() );
        buf.release();
    }

    @Test
    public void testReadLegacyChannelModernised()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "BungeeCord", buf );
        buf.writeBytes( new byte[]{0} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "bungeecord:main", pm.getTag() );
        buf.release();
    }

    @Test
    public void testReadCustomChannelModernised()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "MyChannel", buf );
        buf.writeBytes( new byte[]{0} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "legacy:mychannel", pm.getTag() );
        buf.release();
    }

    @Test
    public void testReadChannelWithColonUnchanged()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "minecraft:brand", buf );
        buf.writeBytes( new byte[]{0} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "minecraft:brand", pm.getTag() );
        buf.release();
    }

    @Test
    public void testWriteModernProtocol()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "BungeeCord" );
        pm.setData( new byte[]{10, 20, 30} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "bungeecord:main", DefinedPacket.readString( buf ) );
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes( data );
        assertArrayEquals( new byte[]{10, 20, 30}, data );
        buf.release();
    }

    @Test
    public void testWriteLegacyProtocol()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "BungeeCord" );
        pm.setData( new byte[]{10, 20, 30} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_12_2 );
        assertEquals( "BungeeCord", DefinedPacket.readString( buf ) );
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes( data );
        assertArrayEquals( new byte[]{10, 20, 30}, data );
        buf.release();
    }

    @Test
    public void testWriteModernProtocolToClient()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "bungeecord:main" );
        pm.setData( new byte[]{99} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_CLIENT, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "BungeeCord", DefinedPacket.readString( buf ) );
        buf.release();
    }

    @Test
    public void testWriteCustomChannel()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "legacy:mychannel" );
        pm.setData( new byte[]{0} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "legacy:mychannel", DefinedPacket.readString( buf ) );
        buf.release();
    }

    @Test
    public void testWriteEmptyData()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "test:channel" );
        pm.setData( new byte[]{} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        DefinedPacket.readString( buf );
        assertEquals( 0, buf.readableBytes() );
        buf.release();
    }

    @Test
    public void testRoundTripModern()
    {
        PluginMessage original = new PluginMessage();
        original.setTag( "bungeecord:main" );
        original.setData( new byte[]{1, 2, 3, 4, 5} );

        ByteBuf buf = Unpooled.buffer();
        original.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );

        PluginMessage read = new PluginMessage();
        read.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );

        assertEquals( original.getTag(), read.getTag() );
        assertArrayEquals( original.getData(), read.getData() );
        buf.release();
    }

    @Test
    public void testRoundTripLegacy()
    {
        PluginMessage original = new PluginMessage();
        original.setTag( "BungeeCord" );
        original.setData( new byte[]{100, (byte) 200, -50} );

        ByteBuf buf = Unpooled.buffer();
        original.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_12_2 );

        PluginMessage read = new PluginMessage();
        read.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_12_2 );

        assertEquals( original.getTag(), read.getTag() );
        assertArrayEquals( original.getData(), read.getData() );
        buf.release();
    }

    @Test
    public void testGetStream()
    {
        PluginMessage pm = new PluginMessage();
        pm.setData( new byte[]{0, 65, 0, 66} );
        DataInput stream = pm.getStream();
        try
        {
            assertEquals( 0, stream.readByte() );
            assertEquals( 65, stream.readByte() );
            assertEquals( 0, stream.readByte() );
            assertEquals( 66, stream.readByte() );
        } catch ( IOException e )
        {
            fail( "Should not throw IOException: " + e.getMessage() );
        }
    }

    @Test
    public void testGetStreamEmpty()
    {
        PluginMessage pm = new PluginMessage();
        pm.setData( new byte[]{} );
        DataInput stream = pm.getStream();
        assertThrows( java.io.EOFException.class, () ->
        {
            stream.readByte();
        } );
    }

    @Test
    public void testPayloadTooLargeToServer()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "test:channel", buf );
        byte[] largeData = new byte[Short.MAX_VALUE + 1];
        buf.writeBytes( largeData );
        assertThrows( IllegalArgumentException.class, () ->
        {
            pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        } );
        buf.release();
    }

    @Test
    public void testPayloadTooLargeToClient()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "test:channel", buf );
        byte[] largeData = new byte[0x100001];
        buf.writeBytes( largeData );
        assertThrows( IllegalArgumentException.class, () ->
        {
            pm.read( buf, ProtocolConstants.Direction.TO_CLIENT, ProtocolConstants.MINECRAFT_1_13 );
        } );
        buf.release();
    }

    @Test
    public void testConstructorArgs()
    {
        PluginMessage pm = new PluginMessage( "test:channel", new byte[]{1, 2}, true );
        assertEquals( "test:channel", pm.getTag() );
        assertArrayEquals( new byte[]{1, 2}, pm.getData() );
        assertTrue( pm.isAllowExtendedPacket() );
    }

    @Test
    public void testNoArgsConstructor()
    {
        PluginMessage pm = new PluginMessage();
        assertNull( pm.getTag() );
        assertNull( pm.getData() );
        assertFalse( pm.isAllowExtendedPacket() );
    }

    @Test
    public void testReadAtExactMaxSizeToServer()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "test:channel", buf );
        byte[] exactData = new byte[Short.MAX_VALUE];
        buf.writeBytes( exactData );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( Short.MAX_VALUE, pm.getData().length );
        buf.release();
    }

    @Test
    public void testReadAtExactMaxSizeToClient()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "test:channel", buf );
        byte[] exactData = new byte[0x100000];
        buf.writeBytes( exactData );
        pm.read( buf, ProtocolConstants.Direction.TO_CLIENT, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( 0x100000, pm.getData().length );
        buf.release();
    }

    @Test
    public void testHandleDelegates()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "test:ch" );
        pm.setData( new byte[]{0} );
        assertDoesNotThrow( () ->
        {
            pm.handle( new net.md_5.bungee.protocol.AbstractPacketHandler()
            {
                @Override
                public void handle(PluginMessage packet) throws Exception
                {
                    assertEquals( "test:ch", packet.getTag() );
                }
            } );
        } );
    }

    @Test
    public void testReadBoundaryProtocolVersion()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "BungeeCord", buf );
        buf.writeBytes( new byte[]{1} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "bungeecord:main", pm.getTag() );
        buf.release();
    }

    @Test
    public void testWriteBoundaryProtocolVersion()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "BungeeCord" );
        pm.setData( new byte[]{1} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 );
        assertEquals( "bungeecord:main", DefinedPacket.readString( buf ) );
        buf.release();
    }

    @Test
    public void testReadOneBelowBoundaryProtocolVersion()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "BungeeCord", buf );
        buf.writeBytes( new byte[]{1} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_12_2 );
        assertEquals( "BungeeCord", pm.getTag() );
        buf.release();
    }

    @Test
    public void testWriteOneBelowBoundaryProtocolVersion()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "BungeeCord" );
        pm.setData( new byte[]{1} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_12_2 );
        assertEquals( "BungeeCord", DefinedPacket.readString( buf ) );
        buf.release();
    }

    @Test
    public void testReadOneAboveBoundaryProtocolVersion()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString( "BungeeCord", buf );
        buf.writeBytes( new byte[]{1} );
        pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 + 1 );
        assertEquals( "bungeecord:main", pm.getTag() );
        buf.release();
    }

    @Test
    public void testWriteOneAboveBoundaryProtocolVersion()
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "BungeeCord" );
        pm.setData( new byte[]{1} );
        ByteBuf buf = Unpooled.buffer();
        pm.write( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_13 + 1 );
        assertEquals( "bungeecord:main", DefinedPacket.readString( buf ) );
        buf.release();
    }

    @Test
    public void testHandleInvokesHandler() throws Exception
    {
        PluginMessage pm = new PluginMessage();
        pm.setTag( "test:ch" );
        pm.setData( new byte[]{0} );

        boolean[] called = {false};
        pm.handle( new net.md_5.bungee.protocol.AbstractPacketHandler()
        {
            @Override
            public void handle(PluginMessage packet) throws Exception
            {
                called[0] = true;
            }
        } );
        assertTrue( called[0], "Handler.handle(PluginMessage) should have been invoked" );
    }

    @Test
    public void testReadPre1_13TagExceedsMaxLength()
    {
        PluginMessage pm = new PluginMessage();
        ByteBuf buf = Unpooled.buffer();
        String longTag = "abcdefghijklmnopqrstu"; // 21 characters, exceeds maxLen=20
        DefinedPacket.writeString( longTag, buf );
        buf.writeBytes( new byte[]{1} );
        assertThrows( net.md_5.bungee.protocol.OverflowPacketException.class, () ->
        {
            pm.read( buf, ProtocolConstants.Direction.TO_SERVER, ProtocolConstants.MINECRAFT_1_12_2 );
        } );
        buf.release();
    }
}
