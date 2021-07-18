package net.md_5.bungee.util;

import java.text.Collator;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeSet;

public class CaseInsensitiveTreeSet extends TreeSet<String>
{

    public CaseInsensitiveTreeSet()
    {
        super( collator() );
    }

    public CaseInsensitiveTreeSet(Collection<? extends String> collection)
    {
        super( collator() );
        super.addAll( collection );
    }

    private static Collator collator()
    {
        Collator c = Collator.getInstance( Locale.ROOT );
        c.setStrength( 1 );
        return c;
    }
}
