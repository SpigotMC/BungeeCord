package net.md_5.bungee.protocol.packet;

import org.junit.Assert;
import org.junit.Test;

public class PluginMessageTest
{

    @Test
    public void testModerniseChannel()
    {
        Assert.assertEquals( "bungeecord:main", PluginMessage.MODERNISE.apply( "BungeeCord" ) );
        Assert.assertEquals( "BungeeCord", PluginMessage.MODERNISE.apply( "bungeecord:main" ) );
        Assert.assertEquals( "legacy:foo", PluginMessage.MODERNISE.apply( "FoO" ) );
        Assert.assertEquals( "foo:bar", PluginMessage.MODERNISE.apply( "foo:bar" ) );
    }
}
