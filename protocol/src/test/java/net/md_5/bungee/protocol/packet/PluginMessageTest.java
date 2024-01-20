package net.md_5.bungee.protocol.packet;

import static org.junit.jupiter.api.Assertions.*;
import net.md_5.bungee.protocol.packet.common.PluginMessage;
import org.junit.jupiter.api.Test;

public class PluginMessageTest
{

    @Test
    public void testModerniseChannel()
    {
        assertEquals( "bungeecord:main", PluginMessage.MODERNISE.apply( "BungeeCord" ) );
        assertEquals( "BungeeCord", PluginMessage.MODERNISE.apply( "bungeecord:main" ) );
        assertEquals( "legacy:foo", PluginMessage.MODERNISE.apply( "FoO" ) );
        assertEquals( "foo:bar", PluginMessage.MODERNISE.apply( "foo:bar" ) );
    }
}
