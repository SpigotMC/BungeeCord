package net.md_5.bungee.api.boss;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Represents a builder of {@link BossBar}
 */
public final class BossBarBuilder
{

    private BaseComponent[] title;
    private BarColor color;
    private BarStyle style;
    private float progress;
    private Collection<ProxiedPlayer> players;
    private BarFlag[] flags;

    /**
     * Create a fresh boss bar builder
     *
     * @param title boss bar title
     */
    public BossBarBuilder(BaseComponent[] title)
    {
        this.title = title;
        color = BarColor.PINK;
        style = BarStyle.SOLID;
        progress = 1.0f;
        players = new HashSet<>();
        flags = new BarFlag[0];
    }

    /**
     * Creates a BossBarBuilder with the given BossBarBuilder to clone
     * it.
     *
     * @param builder original builder
     */
    public BossBarBuilder(BossBarBuilder builder)
    {
        this.title = builder.title;
        this.color = builder.color;
        this.style = builder.style;
        this.progress = builder.progress;
        this.players = builder.players;
        this.flags = builder.flags;
    }

    /**
     * Set the current boss bar's title
     *
     * @param title the title you wish to set
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder title(BaseComponent[] title)
    {
        Preconditions.checkNotNull( title, "title" );
        this.title = title;
        return this;
    }

    /**
     * Set the current boss bar's color
     *
     * @param color the color you wish to set
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder color(BarColor color)
    {
        this.color = color;
        return this;
    }

    /**
     * Set the current boss bar's style
     *
     * @param style the style you wish to set
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder style(BarStyle style)
    {
        this.style = style;
        return this;
    }

    /**
     * Set the current boss bar's progress. The number specified should be
     * between 0 and 1 including.
     *
     * @param progress the progress you wish to set.
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder progress(float progress)
    {
        this.progress = progress;
        return this;
    }

    /**
     * Adds a player to the boss bar.
     *
     * @param player the player you wish to add
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder player(ProxiedPlayer player)
    {
        Preconditions.checkNotNull( player, "player" );
        players.add( player );
        return this;
    }

    /**
     * Adds the specified players to the boss bar
     *
     * @param players the players you wish to add
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder players(ProxiedPlayer... players)
    {
        Preconditions.checkNotNull( players, "players" );
        this.players.addAll( Arrays.asList( players ) );
        return this;
    }

    /**
     * Adds the specified players to the boss bar
     *
     * @param players the players you wish to add
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder players(Iterable<ProxiedPlayer> players)
    {
        Preconditions.checkNotNull( players, "players" );
        Iterables.addAll( this.players, players );
        return this;
    }

    /**
     * Adds the specified flag(s) to the boss bar.
     *
     * @param flags the flag(s) you wish to add
     * @return this BossBarBuilder for chaining
     */
    public BossBarBuilder flags(BarFlag... flags)
    {
        this.flags = flags;
        return this;
    }

    /**
     * Builds every set boss bar component into a {@link BossBar}
     *
     * @return boss bar
     */
    public BossBar build()
    {
        BossBar bossBar = ProxyServer.getInstance().createBossBar( title, color, style, progress );
        if ( flags.length != 0 )
        {
            bossBar.addFlags( flags );
        }
        if ( players.size() != 0 )
        {
            bossBar.addPlayers( players );
        }
        return bossBar;
    }
}
