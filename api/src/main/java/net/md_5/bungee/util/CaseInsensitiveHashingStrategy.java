package net.md_5.bungee.util;

import gnu.trove.strategy.HashingStrategy;
import java.util.Locale;

class CaseInsensitiveHashingStrategy implements HashingStrategy
{

    static final CaseInsensitiveHashingStrategy INSTANCE = new CaseInsensitiveHashingStrategy();

    @Override
    public int computeHashCode(Object object)
    {
        return ( (String) object ).toLowerCase( Locale.ROOT ).hashCode();
    }

    @Override
    public boolean equals(Object o1, Object o2)
    {
        return o1.equals( o2 ) || ( o1 instanceof String && o2 instanceof String && ( (String) o1 ).toLowerCase( Locale.ROOT ).equals( ( (String) o2 ).toLowerCase( Locale.ROOT ) ) );
    }
}
