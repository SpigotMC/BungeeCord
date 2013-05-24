package net.md_5.bungee.util;

import gnu.trove.strategy.HashingStrategy;

class CaseInsensitiveHashingStrategy implements HashingStrategy<String>
{

    static final CaseInsensitiveHashingStrategy INSTANCE = new CaseInsensitiveHashingStrategy();

    @Override
    public int computeHashCode(String object)
    {
        return object.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(String o1, String o2)
    {
        return o1.toLowerCase().equals( o2.toLowerCase() );
    }
}
