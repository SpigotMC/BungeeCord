package net.md_5.bungee.protocol;

import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtocolTest
{

    @Test
    public void testHeaderFooter() throws Exception
    {
        Protocol.DirectionData protocol = Protocol.GAME.TO_CLIENT;
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_8), 0x47);
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_9), 0x48);
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_9_1), 0x48);
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_9_2), 0x48);
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_9_4), 0x47);
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_10), 0x47);
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_11), 0x47);
        assertEquals("Incorrect packet id", protocol.getId(PlayerListHeaderFooter.class, ProtocolConstants.MINECRAFT_1_11_1), 0x47);
    }
}
