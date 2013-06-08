package net.md_5.bungee.tablist;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.tab.TabAPI;
import net.md_5.bungee.api.tab.TabListAdapter;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;

public class Custom extends TabListAdapter implements TabAPI
{

    private static final int ROWS = 20;
    private static final int COLUMNS = 3;
    /*========================================================================*/
    private final Collection<String> sentStuff = new HashSet<>();
    /*========================================================================*/
    private String[][] sent = new String[ ROWS ][ COLUMNS ];
    private String[][] pending = new String[ ROWS ][ COLUMNS ];

    @Override
    public synchronized void setSlot(int row, int column, String text)
    {
        setSlot( row, column, text, true );
    }

    @Override
    public synchronized void setSlot(int row, int column, String text, boolean update)
    {
        Preconditions.checkArgument( row > 0 && row <= ROWS, "row out of range" );
        Preconditions.checkArgument( column > 0 && column <= COLUMNS, "column out of range" );
        Preconditions.checkNotNull( text, "text" );
        Preconditions.checkArgument( text.length() <= 16, "text must be <= 16 chars" );
        Preconditions.checkArgument( !sentStuff.contains( text ), "list already contains %s", text );
        Preconditions.checkArgument( !ChatColor.stripColor( text ).equals( text ), "Text cannot consist entirely of colour codes" );

        pending[ROWS + 1][COLUMNS + 1] = text;
        if ( update )
        {
            update();
        }
    }

    @Override
    public synchronized void update()
    {
        clear();

        for ( int i = 0; i < ROWS; i++ )
        {
            for ( int j = 0; j < COLUMNS; j++ )
            {
                String text;
                if ( pending[i][j] != null )
                {
                    text = pending[i][j];
                    sentStuff.add( text );
                } else
                {
                    text = new StringBuilder().append( ChatColor.COLOR_CHAR ).append( base( i ) ).append( ChatColor.COLOR_CHAR ).append( base( j ) ).toString();
                }
                getPlayer().unsafe().sendPacket( new PacketC9PlayerListItem( text, true, (short) 0 ) );
                sent[i][j] = text;
                pending[i][j] = null;
            }
        }
    }

    @Override
    public synchronized void clear()
    {
        for ( int i = 0; i < ROWS; i++ )
        {
            for ( int j = 0; j < COLUMNS; j++ )
            {
                getPlayer().unsafe().sendPacket( new PacketC9PlayerListItem( sent[i][j], false, (short) 9999 ) );
            }
        }
        sent = new String[ ROWS ][ COLUMNS ];
        sentStuff.clear();
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
        String hex = Integer.toHexString( n );
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
