package net.md_5.bungee.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PlayerNameUtil
{

    public static boolean isValid(boolean onlineMode, String name)
    {
        if ( onlineMode )
        {
            for ( int i = 0; i < name.length(); i++ )
            {
                char c = name.charAt( i );
                if ( ( c >= '0' && c <= '9' ) || ( c >= 'A' && c <= 'Z' ) || ( c >= 'a' && c <= 'z' ) || c == '_' )
                {
                    continue;
                }
                return false;
            }
        } else
        {
            for ( int i = 0; i < name.length(); i++ )
            {
                char c = name.charAt( i );
                // Section symbol, control sequences & space, and delete
                if ( c == '\u00A7' || c <= ' ' || c == 127 )
                {
                    return false;
                }
            }
        }
        return true;
    }

}
