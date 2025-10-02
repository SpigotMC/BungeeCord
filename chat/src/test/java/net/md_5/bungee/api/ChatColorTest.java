package net.md_5.bungee.api;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import org.junit.jupiter.api.Test;

public class ChatColorTest
{

    /**
     * Find the highest distance at which the distance of a color to a color of {@linkplain ChatColor#COLORS} can be,
     * so that there is no other color closer.
     * <p>
     * When searching for the closest fixed color for any color, if encountering a distance equal or smaller than this value,
     * we can be sure that this is indeed the closest color without having to check all other colors.
     *
     * @return the maximum distance squared, {@code 3697}
     */
    private static int calcMaxClosestColorDistSq()
    {
        // Copy color values to local, I think this is faster + this is just a test method
        int length = ChatColor.COLORS.length;
        int[] red = new int[length];
        int[] green = new int[length];
        int[] blue = new int[length];
        for ( int i = 0; i < length; i++ )
        {
            Color color = ChatColor.COLORS[i].getColor();
            red[ i ] = color.getRed();
            green[ i ] = color.getGreen();
            blue[ i ] = color.getBlue();
        }
        int minDistSq = Integer.MAX_VALUE;
        // Iterate over all colors
        for ( int r = 0; r < 256; r++ )
        {
            for ( int g = 0; g < 256; g++ )
            {
                for ( int b = 0; b < 256; b++ )
                {
                    // Find the closest color
                    int closestDistSq = Integer.MAX_VALUE;
                    int closestIndex = -1;
                    for ( int i = 0; i < length; i++ )
                    {
                        int dr = r - red[i];
                        int dg = g - green[i];
                        int db = b - blue[i];
                        int distSq = dr * dr + dg * dg + db * db;
                        if ( distSq < closestDistSq )
                        {
                            closestDistSq = distSq;
                            closestIndex = i;
                        }
                    }
                    // Find second-closest color, ignore same-distance colors
                    int secondClosestDistSq = Integer.MAX_VALUE;
                    //int secondClosestIndex = -1;
                    for ( int i = 0; i < length; i++ )
                    {
                        if ( i == closestIndex ) continue;

                        int dr = r - red[ i ];
                        int dg = g - green[ i ];
                        int db = b - blue[ i ];
                        int distSq = dr * dr + dg * dg + db * db;
                        if ( distSq < secondClosestDistSq && distSq > closestDistSq )
                        {
                            secondClosestDistSq = distSq;
                            //secondClosestIndex = i;
                        }
                    }
                    // If true, this color is the furthest away from its closest color
                    // while still being closer (or as close as) to it than any other color
                    if ( secondClosestDistSq < minDistSq )
                    {
                        minDistSq = secondClosestDistSq;
                        //System.out.println( "New max non-offending distance-sq: " + ( minDistSq - 1) + " at color "
                        //        + r + "," + g + "," + b + " with closest color " + ChatColor.COLORS[ closestIndex ]
                        //        + " with distance-sq " + closestDistSq + " and second closest color "
                        //        + ChatColor.COLORS[ secondClosestIndex ] + " at distance-sq " + secondClosestDistSq );
                    }
                }
            }
        }
        // minDistSq is the distance-sq to the second-closest color, so that is
        // the smallest possibly offending distance-sq. So just subtract 1 to
        // get highest non-offending distance-sq
        return minDistSq - 1;
    }

    @Test
    public void testMaxClosestColorDistSq()
    {
        assertEquals( 3697, calcMaxClosestColorDistSq() );
    }

    @Test
    public void testClosestDefaultColor()
    {
        for ( int r = 0; r < 256; r += 1 )
        {
            for ( int g = 0; g < 256; g += 1 )
            {
                for ( int b = 0; b < 256; b += 1 )
                {
                    Color color = new Color( r, g, b );
                    ChatColor closest = ChatColor.closestDefaultColor( color );
                    Color closestColor = closest.getColor();
                    int dr = r - closestColor.getRed();
                    int dg = g - closestColor.getGreen();
                    int db = b - closestColor.getBlue();
                    int distSq = dr * dr + dg * dg + db * db;

                    // Check result correct, by ensuring that no other color is closer
                    ChatColor closestChk = null;
                    int closestDistSqChk = Integer.MAX_VALUE;
                    for ( ChatColor chatColor : ChatColor.COLORS )
                    {
                        Color c = chatColor.getColor();
                        int drChk = r - c.getRed();
                        int dgChk = g - c.getGreen();
                        int dbChk = b - c.getBlue();
                        int distSqChk = drChk * drChk + dgChk * dgChk + dbChk * dbChk;
                        if ( distSqChk < closestDistSqChk )
                        {
                            closestDistSqChk = distSqChk;
                            closestChk = chatColor;
                        }
                    }
                    // not using assertEquals for performance reasons (string concat)
                    if ( !closest.equals( closestChk ) )
                    {
                        fail( "Color " + color + " has closest color " + closest + " with distance " + distSq
                                + ", but found " + closestChk + " with distance " + closestDistSqChk + " instead" );
                    }
                }
            }
        }
    }
}
