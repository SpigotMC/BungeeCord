package net.md_5.bungee.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllowedCharacters
{

    public static boolean isChatAllowedCharacter(char character)
    {
        // Section symbols, control sequences, and deletes are not allowed
        return character != '\u00A7' && character >= ' ' && character != 127;
    }

    private static boolean isNameAllowedCharacter(char c, boolean onlineMode)
    {
        if ( onlineMode )
        {
            return ( c >= 'a' && c <= 'z' ) || ( c >= '0' && c <= '9' ) || ( c >= 'A' && c <= 'Z' ) || c == '_' || c == '.' || c == '-';
        } else
        {
            // Don't allow spaces, Yaml config doesn't support them
            return isChatAllowedCharacter( c ) && c != ' ';
        }
    }

    public static boolean isValidName(String name, boolean onlineMode)
    {
        for ( int index = 0, len = name.length(); index < len; index++ )
        {
            if ( !isNameAllowedCharacter( name.charAt( index ), onlineMode ) )
            {
                return false;
            }
        }
        return true;
    }
}
