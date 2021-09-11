package net.md_5.bungee.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AllowedCharacters
{

    // Section symbols, control sequences, and deletes are not allowed
    public static boolean isChatAllowedCharacter(char character)
    {
        return character != '\u00A7' && character >= ' ' && character != 127;
    }

    private static boolean isNameAllowedCharacter(char c, boolean onlineMode)
    {
        if ( onlineMode )
        {
            return ( c >= 'a' && c <= 'z' ) || ( c >= '0' && c <= '9' ) || ( c >= 'A' && c <= 'Z' ) || c == '_';
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
