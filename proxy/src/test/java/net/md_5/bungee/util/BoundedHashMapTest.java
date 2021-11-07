package net.md_5.bungee.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class BoundedHashMapTest
{
    @Test
    public void testGoodAdd() throws Exception
    {
        BoundedHashSet<Object> set = new BoundedHashSet<>( 2 );
        set.add( new Object() );
        set.add( new Object() );
    }

    @Test
    public void testSizeOneAdd() throws Exception
    {
        BoundedHashSet<Object> set = new BoundedHashSet<>( 1 );
        set.add( new Object() );
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAdd() throws Exception
    {
        BoundedHashSet<Object> set = new BoundedHashSet<>( 0 );
        set.add( new Object() );
    }

    @Test
    public void testGoodAddAll() throws Exception
    {
        BoundedHashSet<Object> set = new BoundedHashSet<>( 1 );
        set.addAll( ImmutableList.of( new Object() ) );
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAddAll() throws Exception
    {
        BoundedHashSet<Object> set = new BoundedHashSet<>( 0 );
        set.addAll( ImmutableList.of( new Object() ) );
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAddAll1() throws Exception
    {
        BoundedHashSet<Object> set = new BoundedHashSet<>( 1 );
        set.add( new Object() );
        set.addAll( ImmutableList.of( new Object() ) );
    }
}
