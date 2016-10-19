package net.md_5.bungee;

import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Title.Action;

public class BungeeTitle implements Title
{

    private net.md_5.bungee.protocol.packet.Title title, subtitle, times, clear, reset;

    private static net.md_5.bungee.protocol.packet.Title createPacket(Action action)
    {
        net.md_5.bungee.protocol.packet.Title title = new net.md_5.bungee.protocol.packet.Title();
        title.setAction( action );

        if ( action == Action.TIMES )
        {
            // Set packet to default values first
            title.setFadeIn( 20 );
            title.setStay( 60 );
            title.setFadeOut( 20 );
        }
        return title;
    }

    @Override
    public Title title(BaseComponent text)
    {
        if ( title == null )
        {
            title = createPacket( Action.TITLE );
        }

        title.setText( ComponentSerializer.toString( text ) );
        return this;
    }

    @Override
    public Title title(BaseComponent... text)
    {
        if ( title == null )
        {
            title = createPacket( Action.TITLE );
        }

        title.setText( ComponentSerializer.toString( text ) );
        return this;
    }

    @Override
    public Title subTitle(BaseComponent text)
    {
        if ( subtitle == null )
        {
            subtitle = createPacket( Action.SUBTITLE );
        }

        subtitle.setText( ComponentSerializer.toString( text ) );
        return this;
    }

    @Override
    public Title subTitle(BaseComponent... text)
    {
        if ( subtitle == null )
        {
            subtitle = createPacket( Action.SUBTITLE );
        }

        subtitle.setText( ComponentSerializer.toString( text ) );
        return this;
    }

    @Override
    public Title fadeIn(int ticks)
    {
        if ( times == null )
        {
            times = createPacket( Action.TIMES );
        }

        times.setFadeIn( ticks );
        return this;
    }

    @Override
    public Title stay(int ticks)
    {
        if ( times == null )
        {
            times = createPacket( Action.TIMES );
        }

        times.setStay( ticks );
        return this;
    }

    @Override
    public Title fadeOut(int ticks)
    {
        if ( times == null )
        {
            times = createPacket( Action.TIMES );
        }

        times.setFadeOut( ticks );
        return this;
    }

    @Override
    public Title clear()
    {
        if ( clear == null )
        {
            clear = createPacket( Action.CLEAR );
        }

        title = null; // No need to send title if we clear it after that again

        return this;
    }

    @Override
    public Title reset()
    {
        if ( reset == null )
        {
            reset = createPacket( Action.RESET );
        }

        // No need to send these packets if we reset them later
        title = null;
        subtitle = null;
        times = null;

        return this;
    }

    private static void sendPacket(ProxiedPlayer player, DefinedPacket packet)
    {
        if ( packet != null )
        {
            player.unsafe().sendPacket( packet );
        }
    }

    @Override
    public Title send(ProxiedPlayer player)
    {
        sendPacket( player, clear );
        sendPacket( player, reset );
        sendPacket( player, times );
        sendPacket( player, subtitle );
        sendPacket( player, title );
        return this;
    }
}
