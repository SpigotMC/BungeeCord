package net.md_5.bungee.util;

import org.junit.Assert;
import org.junit.Test;

public class CaseInsensitiveTest
{

    @Test
    public void testMaps()
    {
        Object obj = new Object();
        CaseInsensitiveMap<Object> map = new CaseInsensitiveMap<>();

        map.put( "FOO", obj );
        Assert.assertTrue( map.contains( "foo" ) ); // Assert that contains is case insensitive
        Assert.assertTrue( map.entrySet().iterator().next().getKey().equals( "FOO" ) ); // Assert that case is preserved

        // Assert that remove is case insensitive
        map.remove( "FoO" );
        Assert.assertFalse( map.contains( "foo" ) );
    }

    @Test
    public void testSets()
    {
        CaseInsensitiveSet set = new CaseInsensitiveSet();

        set.add( "FOO" );
        Assert.assertTrue( set.contains( "foo" ) ); // Assert that contains is case insensitive
        set.remove( "FoO" );
        Assert.assertFalse( set.contains( "foo" ) ); // Assert that remove is case insensitive
    }
}
