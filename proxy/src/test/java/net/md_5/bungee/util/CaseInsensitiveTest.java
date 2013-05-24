package net.md_5.bungee.util;

import org.junit.Test;
import org.junit.Assert;

public class CaseInsensitiveTest
{

    @Test
    public void testMaps()
    {
        Object obj = new Object();
        CaseInsensitiveMap<Object> map = new CaseInsensitiveMap<>();

        map.put( "FOO", obj );
        Assert.assertTrue( map.contains( "foo" ) ); // Assert that it is case insensitive
        Assert.assertTrue( map.entrySet().iterator().next().getKey().equals( "FOO" ) ); // Asert that case is preserved
    }

    @Test
    public void testSets()
    {
        CaseInsensitiveSet set = new CaseInsensitiveSet();

        set.add( "FOO" );
        Assert.assertTrue( set.contains( "foo" ) );
    }
}
