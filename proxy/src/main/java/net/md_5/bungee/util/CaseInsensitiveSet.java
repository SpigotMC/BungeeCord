package net.md_5.bungee.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Collection;

public class CaseInsensitiveSet extends ObjectOpenCustomHashSet<String>
{

    public CaseInsensitiveSet()
    {
        super( CaseInsensitiveHashingStrategy.INSTANCE );
    }

    public CaseInsensitiveSet(Collection<? extends String> collection)
    {
        super( collection, CaseInsensitiveHashingStrategy.INSTANCE );
    }
}
