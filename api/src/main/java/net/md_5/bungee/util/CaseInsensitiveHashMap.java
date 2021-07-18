package net.md_5.bungee.util;

import gnu.trove.map.hash.TCustomHashMap;
import java.util.Map;

public class CaseInsensitiveHashMap<V> extends TCustomHashMap<String, V>
{

    public CaseInsensitiveHashMap()
    {
        super( CaseInsensitiveHashingStrategy.INSTANCE );
    }

    public CaseInsensitiveHashMap(Map<? extends String, ? extends V> map)
    {
        super( CaseInsensitiveHashingStrategy.INSTANCE, map );
    }
}
