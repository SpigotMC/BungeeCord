package net.md_5.bungee.util;

import java.text.Collator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CaseInsensitiveTreeMap<V> extends TreeMap<String, V>
{

    public CaseInsensitiveTreeMap()
    {
        super( new TreeMap<>( collator() ) );
    }

    public CaseInsensitiveTreeMap(Map<? extends String, ? extends V> map)
    {
        super( collator() );
        super.putAll( map );
    }

    private static Collator collator()
    {
        Collator c = Collator.getInstance( Locale.ROOT );
        c.setStrength( 1 );
        return c;
    }
}
