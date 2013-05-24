package net.md_5.bungee.util;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import java.util.Map;

public class CaseInsensitiveMap<V> extends TCustomHashMap<String, V>
{

    private static final HashingStrategy<String> hashingStrategy = new HashingStrategy<String>()
    {
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
    };

    public CaseInsensitiveMap()
    {
        super( hashingStrategy );
    }

    public CaseInsensitiveMap(Map<? extends String, ? extends V> map)
    {
        super( hashingStrategy, map );
    }
}
