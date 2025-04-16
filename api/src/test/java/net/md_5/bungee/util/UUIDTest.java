package net.md_5.bungee.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import net.md_5.bungee.Util;
import org.junit.jupiter.api.Test;

public class UUIDTest
{

    @Test
    public void testSingle()
    {
        UUID uuid = UUID.fromString( "af74a02d-19cb-445b-b07f-6866a861f783" );
        UUID uuid1 = Util.getUUID( "af74a02d19cb445bb07f6866a861f783" );
        assertEquals( uuid, uuid1 );
    }

    @Test
    public void testMany()
    {
        for ( int i = 0; i < 1000; i++ )
        {
            UUID expected = UUID.randomUUID();
            UUID actual = Util.getUUID( expected.toString().replace( "-", "" ) );
            assertEquals( expected, actual, "Could not parse UUID " + expected );
        }
    }
}
