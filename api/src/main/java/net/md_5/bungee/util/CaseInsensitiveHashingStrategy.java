package net.md_5.bungee.util;

import it.unimi.dsi.fastutil.Hash;
import java.util.Locale;

class CaseInsensitiveHashingStrategy implements Hash.Strategy<String>
{

    static final CaseInsensitiveHashingStrategy INSTANCE = new CaseInsensitiveHashingStrategy();

    @Override
    public int hashCode(String object)
    {
        if ( object == null )
        {
            return 0;
        }

        return object.toLowerCase( Locale.ROOT ).hashCode();
    }

    @Override
    public boolean equals(String o1, String o2)
    {
        if ( o1 == o2 )
        {
            return true;
        }

        if ( o1 == null || o2 == null )
        {
            return false;
        }

        return o1.equals( o2 ) || o1.toLowerCase( Locale.ROOT ).equals( o2.toLowerCase( Locale.ROOT ) );
    }
}
