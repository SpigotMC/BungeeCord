package net.md_5.bungee.api.chat;

import net.md_5.bungee.api.ChatColor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TextComponentTest
{

    @Test
    public void testInvalidColorCodes()
    {
        StringBuilder allInvalidColorCodes = new StringBuilder();

        // collect all invalid color codes (e.g. §z, §g, ...)
        for ( char alphChar : "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray() )
        {
            if ( ChatColor.ALL_CODES.indexOf( alphChar ) == -1 )
            {
                allInvalidColorCodes.append( ChatColor.COLOR_CHAR );
                allInvalidColorCodes.append( alphChar );
            }
        }

        // last char is a single '§'
        allInvalidColorCodes.append( ChatColor.COLOR_CHAR );

        String invalidColorCodesLegacyText = fromAndToLegacyText( allInvalidColorCodes.toString() );
        String emptyLegacyText = fromAndToLegacyText( "" );

        // all invalid color codes and the trailing '§' should be ignored
        assertEquals( emptyLegacyText, invalidColorCodesLegacyText );
    }

    private String fromAndToLegacyText(String legacyText)
    {
        return BaseComponent.toLegacyText( TextComponent.fromLegacyText( legacyText ) );
    }
}
