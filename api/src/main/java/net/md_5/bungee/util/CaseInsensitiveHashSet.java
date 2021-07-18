package net.md_5.bungee.util;

import gnu.trove.set.hash.TCustomHashSet;
import java.util.Collection;

public class CaseInsensitiveHashSet extends TCustomHashSet<String>
{

    public CaseInsensitiveHashSet()
    {
        super( CaseInsensitiveHashingStrategy.INSTANCE );
    }

    public CaseInsensitiveHashSet(Collection<? extends String> collection)
    {
        super( CaseInsensitiveHashingStrategy.INSTANCE, collection );
    }
}
