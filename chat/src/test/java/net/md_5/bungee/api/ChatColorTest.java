package net.md_5.bungee.api;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import org.junit.jupiter.api.Test;

public class ChatColorTest
{

    /**
     * Calculates the maximum distance (squared) a color can have to its nearest default ChatColor.
     *
     * @return 16130
     */
    private static int calcMaxClosestColorDistSq()
    {
        // Copy color values to local, drastically improves speed
        int length = ChatColor.COLORS.length;
        int[] red = new int[ length ];
        int[] green = new int[ length ];
        int[] blue = new int[ length ];
        for ( int i = 0; i < length; i++ )
        {
            Color color = ChatColor.COLORS[ i ].getColor();
            red[ i ] = color.getRed();
            green[ i ] = color.getGreen();
            blue[ i ] = color.getBlue();
        }

        int maxDistToNearestSq = -1;
        for ( int r = 0; r < 256; r++ )
        {
            for ( int g = 0; g < 256; g++ )
            {
                for ( int b = 0; b < 256; b++ )
                {
                    int minSq = Integer.MAX_VALUE;
                    for ( int i = 0; i < length; i++ )
                    {
                        int dr = r - red[ i ];
                        int dg = g - green[ i ];
                        int db = b - blue[ i ];
                        int distSq = dr * dr + dg * dg + db * db;
                        if ( distSq < minSq )
                        {
                            minSq = distSq;
                            if ( minSq == 0 ) break;
                        }
                    }
                    if ( minSq > maxDistToNearestSq )
                    {
                        maxDistToNearestSq = minSq;
                    }
                }
            }
        }
        return maxDistToNearestSq;
    }

    @Test
    public void testMaxClosestColorDistSq()
    {
        assertEquals( 16130, calcMaxClosestColorDistSq() );
    }
}
