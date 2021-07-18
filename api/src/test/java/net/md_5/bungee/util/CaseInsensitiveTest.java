package net.md_5.bungee.util;

import org.junit.Assert;
import org.junit.Test;

public class CaseInsensitiveTest
{

    @Test
    public void testMaps()
    {
        Object obj = new Object();
        CaseInsensitiveHashMap<Object> map = new CaseInsensitiveHashMap<>();

        map.put( "FOO", obj );
        Assert.assertTrue( map.containsKey( "foo" ) ); // Assert that contains is case insensitive
        Assert.assertTrue( map.entrySet().iterator().next().getKey().equals( "FOO" ) ); // Assert that case is preserved

        // Assert that remove is case insensitive
        map.remove( "FoO" );
        Assert.assertFalse( map.containsKey( "foo" ) );

        map.put( "abc", obj );
        CaseInsensitiveTreeMap<Object> map2 = new CaseInsensitiveTreeMap<>( map );
        map2.put( "aaa", obj );

        Assert.assertEquals( map2.size(), 2 );
        Assert.assertTrue( map2.firstKey().equals( "aaa" ) );
        Assert.assertTrue( map2.lastKey().equals( "abc" ) );
        Assert.assertTrue( map2.containsKey( "AaA" ) );
    }

    @Test
    public void testSets()
    {
        CaseInsensitiveHashSet set = new CaseInsensitiveHashSet();

        set.add( "FOO" );
        Assert.assertTrue( set.contains( "foo" ) ); // Assert that contains is case insensitive
        set.remove( "FoO" );
        Assert.assertFalse( set.contains( "foo" ) ); // Assert that remove is case insensitive

        set.add( "ABC" );
        CaseInsensitiveTreeSet set2 = new CaseInsensitiveTreeSet( set );
        set2.add( "aaa" );

        Assert.assertEquals( set2.size(), 2 );
        Assert.assertTrue( set2.first().equals( "aaa" ) );
        Assert.assertTrue( set2.last().equals( "ABC" ) );
        Assert.assertTrue( set2.contains( "abc" ) );
    }
}
