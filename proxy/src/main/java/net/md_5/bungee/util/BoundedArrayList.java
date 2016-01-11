package net.md_5.bungee.util;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;

public class BoundedArrayList<E> extends ArrayList<E>
{

    private final int maxSize;

    public BoundedArrayList(int maxSize)
    {
        this.maxSize = maxSize;
    }

    private void checkSize(int increment)
    {
        Preconditions.checkState( size() + increment <= maxSize, "Adding %s elements would exceed capacity of %s", increment, maxSize );
    }

    @Override
    public boolean add(E e)
    {
        checkSize( 1 );
        return super.add( e );
    }

    @Override
    public void add(int index, E element)
    {
        checkSize( 1 );
        super.add( index, element );
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        checkSize( c.size() );
        return super.addAll( c );
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        checkSize( c.size() );
        return super.addAll( index, c );
    }
}
