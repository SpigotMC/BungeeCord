package net.md_5.bungee.util;

import java.util.UUID;
import net.md_5.bungee.Util;
import org.junit.Assert;
import org.junit.Test;

public class UUIDTest
{

    @Test
    public void testSingle()
    {
        UUID uuid = UUID.fromString( "af74a02d-19cb-445b-b07f-6866a861f783" );
        UUID uuid1 = Util.getUUID( "af74a02d19cb445bb07f6866a861f783" );
        Assert.assertEquals( uuid, uuid1 );
    }

    @Test
    public void testParseMany()
    {
        for ( int i = 0; i < 1000; i++ )
        {
            UUID expected = UUID.randomUUID();
            UUID actual = Util.getUUID( expected.toString().replace( "-", "" ) );
            Assert.assertEquals( "Could not parse UUID " + expected, expected, actual );
        }
    }

    @Test
    public void testUndashMany()
    {
        for ( int i = 0; i < 1000; i++ )
        {
            UUID uuid = UUID.randomUUID();
            String expected = uuid.toString().replace( "-", "" );
            String actual = Util.undashUUID( uuid );
            Assert.assertEquals( "Could not undash UUID " + expected, expected, actual );
        }
    }
}
