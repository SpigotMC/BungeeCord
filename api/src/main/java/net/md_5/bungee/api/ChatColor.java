package net.md_5.bungee.api;

import java.util.regex.Pattern;

/**
 * Simplistic enumeration of all supported color values for chat.
 */
public enum ChatColor
{

    /**
     * Represents black.
     */
    BLACK( '0' ),
    /**
     * Represents dark blue.
     */
    DARK_BLUE( '1' ),
    /**
     * Represents dark green.
     */
    DARK_GREEN( '2' ),
    /**
     * Represents dark blue (aqua).
     */
    DARK_AQUA( '3' ),
    /**
     * Represents dark red.
     */
    DARK_RED( '4' ),
    /**
     * Represents dark purple.
     */
    DARK_PURPLE( '5' ),
    /**
     * Represents gold.
     */
    GOLD( '6' ),
    /**
     * Represents gray.
     */
    GRAY( '7' ),
    /**
     * Represents dark gray.
     */
    DARK_GRAY( '8' ),
    /**
     * Represents blue.
     */
    BLUE( '9' ),
    /**
     * Represents green.
     */
    GREEN( 'a' ),
    /**
     * Represents aqua.
     */
    AQUA( 'b' ),
    /**
     * Represents red.
     */
    RED( 'c' ),
    /**
     * Represents light purple.
     */
    LIGHT_PURPLE( 'd' ),
    /**
     * Represents yellow.
     */
    YELLOW( 'e' ),
    /**
     * Represents white.
     */
    WHITE( 'f' ),
    /**
     * Represents magical characters that change around randomly.
     */
    MAGIC( 'k' ),
    /**
     * Makes the text bold.
     */
    BOLD( 'l' ),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH( 'm' ),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINE( 'n' ),
    /**
     * Makes the text italic.
     */
    ITALIC( 'o' ),
    /**
     * Resets all previous chat colors or formats.
     */
    RESET( 'r' );
    /**
     * The special character which prefixes all chat colour codes. Use this if
     * you need to dynamically convert colour codes from your custom format.
     */
    public static final char COLOR_CHAR = '\u00A7';
    /**
     * Pattern to remove all colour codes.
     */
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile( "(?i)" + String.valueOf( COLOR_CHAR ) + "[0-9A-FK-OR]" );
    /**
     * This colour's colour char prefixed by the {@link #COLOR_CHAR}.
     */
    private final String toString;

    private ChatColor(char code)
    {
        this.toString = new String( new char[]
        {
            COLOR_CHAR, code
        } );
    }

    @Override
    public String toString()
    {
        return toString;
    }

    /**
     * Strips the given message of all color codes
     *
     * @param input String to strip of color
     * @return A copy of the input string, without any coloring
     */
    public static String stripColor(final String input)
    {
        if ( input == null )
        {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher( input ).replaceAll( "" );
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate)
    {
        char[] b = textToTranslate.toCharArray();
        for ( int i = 0; i < b.length - 1; i++ )
        {
            if ( b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf( b[i + 1] ) > -1 )
            {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase( b[i + 1] );
            }
        }
        return new String( b );
    }
}
