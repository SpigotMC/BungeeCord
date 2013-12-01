package net.md_5.bungee.tab;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.tab.CustomTabList;
import net.md_5.bungee.api.tab.TabListAdapter;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class Custom extends TabListAdapter implements CustomTabList
{

    private static final int ROWS = 20;
    private static final int COLUMNS = 3;
    private static final char[] FILLER = new char[]
    {
        '0', '1', '2', '2', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    private static final int MAX_LEN = 16;
    /*========================================================================*/
    private final Collection<String> sentStuff = new HashSet<>();
    /*========================================================================*/
    private String[][] sent = new String[ ROWS ][ COLUMNS ];
    private String[][] slots = new String[ ROWS ][ COLUMNS ];
    private int rowLim;
    private int colLim;

    public Custom(ProxiedPlayer player)
    {
        this.init( player );
    }

    @Override
    public synchronized String setSlot(int row, int column, String text)
    {
        return setSlot( row, column, text, true );
    }

    @Override
    public synchronized String setSlot(int row, int column, String text, boolean update)
    {
        Preconditions.checkArgument( row > 0 && row <= ROWS, "row out of range" );
        Preconditions.checkArgument( column > 0 && column <= COLUMNS, "column out of range" );

        if ( text != null )
        {
            Preconditions.checkArgument( text.length() <= MAX_LEN - 2, "text must be <= %s chars", MAX_LEN - 2 );
            Preconditions.checkArgument( !ChatColor.stripColor( text ).isEmpty(), "Text cannot consist entirely of colour codes" );
            text = attempt( text );
            sentStuff.add( text );

            if ( rowLim < row || colLim < column )
            {
                rowLim = row;
                colLim = column;
            }
        } else
        {
            sentStuff.remove( text );
        }

        slots[--row][--column] = text;
        if ( update )
        {
            update();
        }
        return text;
    }

    private String attempt(String s)
    {
        for ( char c : FILLER )
        {
            String attempt = s + Character.toString( ChatColor.COLOR_CHAR ) + c;
            if ( !sentStuff.contains( attempt ) )
            {
                return attempt;
            }
        }
        if ( s.length() <= MAX_LEN - 4 )
        {
            return attempt( s + Character.toString( ChatColor.COLOR_CHAR ) + FILLER[0] );
        }
        throw new IllegalArgumentException( "List already contains all variants of string" );
    }

    @Override
    public synchronized void update()
    {
        clear();
        for ( int i = 0; i < rowLim; i++ )
        {
            for ( int j = 0; j < colLim; j++ )
            {
                String text = ( slots[i][j] != null ) ? slots[i][j] : new StringBuilder().append( base( i ) ).append( base( j ) ).toString();
                sent[i][j] = text;
                getPlayer().unsafe().sendPacket( new PlayerListItem( text, true, (short) 0 ) );
            }
        }
    }

    @Override
    public synchronized void clear()
    {
        for ( int i = 0; i < rowLim; i++ )
        {
            for ( int j = 0; j < colLim; j++ )
            {
                if ( sent[i][j] != null )
                {
                    String text = sent[i][j];
                    sent[i][j] = null;
                    getPlayer().unsafe().sendPacket( new PlayerListItem( text, false, (short) 9999 ) );
                }
            }
        }
    }

    @Override
    public synchronized int getRows()
    {
        return ROWS;
    }

    @Override
    public synchronized int getColumns()
    {
        return COLUMNS;
    }

    @Override
    public synchronized int getSize()
    {
        return ROWS * COLUMNS;
    }

    @Override
    public boolean onListUpdate(String name, boolean online, int ping)
    {
        return false;
    }

    private static char[] base(int n)
    {
        String hex = Integer.toHexString( n + 1 );
        char[] alloc = new char[ hex.length() * 2 ];
        for ( int i = 0; i < alloc.length; i++ )
        {
            if ( i % 2 == 0 )
            {
                alloc[i] = ChatColor.COLOR_CHAR;
            } else
            {
                alloc[i] = hex.charAt( i / 2 );
            }
        }
        return alloc;
    }
}
