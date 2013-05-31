package net.md_5.bungee.protocol;

import org.junit.Assert;
import org.junit.Test;

public class ProtocolTest
{

    @Test
    public void testProtocol()
    {
        Assert.assertFalse( "Protocols should have different login packet", Vanilla.getInstance().getClasses()[0x01] == Forge.getInstance().classes[0x01] );
    }
}
