package net.md_5.bungee.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Map;

public class CaseInsensitiveMap<V> extends Object2ObjectOpenCustomHashMap<String, V>
{

    public CaseInsensitiveMap()
    {
        super( CaseInsensitiveHashingStrategy.INSTANCE );
    }

    public CaseInsensitiveMap(Map<? extends String, ? extends V> map)
    {
        super( map, CaseInsensitiveHashingStrategy.INSTANCE );
    }
}
