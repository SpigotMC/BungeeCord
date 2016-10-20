package net.md_5.bungee.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class BoundedArrayListTest
{

    @Test
    public void testGoodAdd() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 2 );
        list.add( new Object() );
        list.add( new Object() );
    }

    @Test
    public void testSizeOneAdd() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 1 );
        list.add( new Object() );
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAdd() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 0 );
        list.add( new Object() );
    }

    @Test
    public void testGoodAdd1() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 2 );
        list.add( new Object() );
        list.add( 0, new Object() );
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAdd1() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 1 );
        list.add( new Object() );
        list.add( 0, new Object() );
    }

    @Test
    public void testGoodAddAll() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 1 );
        list.addAll( ImmutableList.of( new Object() ) );
    }

    @Test
    public void testGoodAddAll1() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 2 );
        list.add( new Object() );
        list.addAll( 0, ImmutableList.of( new Object() ) );
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAddAll() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 0 );
        list.addAll( ImmutableList.of( new Object() ) );
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAddAll1() throws Exception
    {
        BoundedArrayList<Object> list = new BoundedArrayList<>( 1 );
        list.add( new Object() );
        list.addAll( ImmutableList.of( new Object() ) );
    }
}
