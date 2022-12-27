package net.md_5.bungee.log;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import jline.console.ConsoleReader;
import net.md_5.bungee.api.ChatColor;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;

public class ColouredWriter extends Handler
{

    private static final HashMap<Character, String> ANSI_COLOR_MAP = new HashMap<>();

    static
    {
        ANSI_COLOR_MAP.put( getChar( ChatColor.BLACK ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.DARK_BLUE ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.DARK_GREEN ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.DARK_AQUA ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.DARK_RED ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.DARK_PURPLE ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.GOLD ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.GRAY ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.DARK_GRAY ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLACK ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.BLUE ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.BLUE ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.GREEN ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.GREEN ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.AQUA ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.CYAN ).boldOff().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.RED ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.RED ).bold().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.LIGHT_PURPLE ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.MAGENTA ).bold().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.YELLOW ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.YELLOW ).bold().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.WHITE ), Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.WHITE ).bold().toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.MAGIC ), Ansi.ansi().a( Ansi.Attribute.BLINK_SLOW ).toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.BOLD ), Ansi.ansi().a( Ansi.Attribute.UNDERLINE_DOUBLE ).toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.STRIKETHROUGH ), Ansi.ansi().a( Ansi.Attribute.STRIKETHROUGH_ON ).toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.UNDERLINE ), Ansi.ansi().a( Ansi.Attribute.UNDERLINE ).toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.ITALIC ), Ansi.ansi().a( Ansi.Attribute.ITALIC ).toString() );
        ANSI_COLOR_MAP.put( getChar( ChatColor.RESET ), Ansi.ansi().a( Ansi.Attribute.RESET ).toString() );

        new HashMap<>( ANSI_COLOR_MAP ).forEach( (k, v) -> ANSI_COLOR_MAP.put( Character.toUpperCase( k ), v ) );
    }

    private static Character getChar(ChatColor color)
    {
        return color.toString().charAt( 1 );
    }

    private static final String formatAnsi(String msg)
    {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = msg.toCharArray();
        for ( int index = 0; index < chars.length; index++ )
        {
            char currentChar = chars[ index ];
            if ( currentChar == ChatColor.COLOR_CHAR )
            {
                if ( index == chars.length - 1 )
                {
                    return stringBuilder.append( ChatColor.COLOR_CHAR ).toString();
                }
                char nextChar = chars[ ++index ];

                if ( Character.toLowerCase( nextChar ) == 'x' && index + 12 < chars.length )
                {
                    StringBuilder hex = new StringBuilder( "#" );
                    for ( int j = 0; j < 6; j++ )
                    {
                        int cur = index + 2 + ( j * 2 );
                        if ( chars[ cur - 1 ] != ChatColor.COLOR_CHAR )
                        {
                            break;
                        }
                        hex.append( chars[ cur ] );
                    }
                    try
                    {
                        // Ansi can't support all colors, so we get a close one
                        ChatColor closest = getClosestChatColor( ChatColor.of( hex.toString() ).getColor().getRGB() );
                        //  Hex color codes are resetting special effects like boldness
                        stringBuilder.append( ANSI_COLOR_MAP.get( 'r' ) ).append( ANSI_COLOR_MAP.get( getChar( closest ) ) );
                        index += 12;
                        continue;
                    } catch ( IllegalArgumentException ex )
                    {
                    }
                }

                // default color support
                String ansi = ANSI_COLOR_MAP.get( nextChar );
                if ( ansi != null )
                {
                    stringBuilder.append( ansi );
                } else
                {
                    stringBuilder.append( ChatColor.COLOR_CHAR ).append( nextChar );
                }
            } else
            {
                stringBuilder.append( currentChar );
            }
        }

        return stringBuilder.toString();
    }

    // from ViaBackwards https://github.com/ViaVersion/ViaBackwards/blob/master/common/src/main/java/com/viaversion/viabackwards/protocol/protocol1_15_2to1_16/chat/TranslatableRewriter1_16.java
    private static ChatColor getClosestChatColor(int rgb)
    {
        int r = ( rgb >> 16 ) & 0xFF;
        int g = ( rgb >> 8 ) & 0xFF;
        int b = rgb & 0xFF;

        ChatColor closest = null;
        int smallestDiff = 0;

        for ( ChatColor color : ChatColor.values() )
        {
            Color javaColor = color.getColor();
            if ( javaColor == null )
            {
                continue;
            }

            if ( javaColor.getRGB() == rgb )
            {
                return color;
            }

            // Check by the greatest diff of the 3 values
            int rAverage = ( javaColor.getRed() + r ) / 2;
            int rDiff = javaColor.getRed() - r;
            int gDiff = javaColor.getGreen() - g;
            int bDiff = javaColor.getBlue() - b;
            int diff = ( ( 2 + ( rAverage >> 8 ) ) * rDiff * rDiff )
                    + ( 4 * gDiff * gDiff )
                    + ( ( 2 + ( ( 255 - rAverage ) >> 8 ) ) * bDiff * bDiff );
            if ( closest == null || diff < smallestDiff )
            {
                closest = color;
                smallestDiff = diff;
            }
        }
        return closest;
    }

    //
    private final ConsoleReader console;

    public ColouredWriter(ConsoleReader console)
    {
        this.console = console;
    }

    public void print(String s)
    {
        s = formatAnsi( s );
        try
        {
            console.print( Ansi.ansi().eraseLine( Erase.ALL ).toString() + ConsoleReader.RESET_LINE + s + Ansi.ansi().reset().toString() );
            console.drawLine();
            console.flush();
        } catch ( IOException ex )
        {
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        if ( isLoggable( record ) )
        {
            print( getFormatter().format( record ) );
        }
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }
}
