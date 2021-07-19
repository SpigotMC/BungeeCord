package net.md_5.bungee.api.boss;

import java.util.Collection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Represents a boss bar object
 */
public interface BossBar
{

    /**
     * Returns whenever the player will be able to see this boss bar by performing a
     * connection version check.
     *
     * @param player the player you want to check if can be added
     * @return <code>true</code> if can be added, <code>false</code> otherwise
     */
    boolean canBeAdded(ProxiedPlayer player);

    /**
     * Adds a player to view the boss bar. If the player is not connected or
     * the boss bar is not visible, it will not be sent.
     *
     * @param player the player you wish to see the boss bar
     */
    void addPlayer(ProxiedPlayer player);

    /**
     * Adds all players to view the boss bar.
     *
     * @param players the players you wish to see the boss bar
     * @see #addPlayer(ProxiedPlayer)
     */
    void addPlayers(Iterable<ProxiedPlayer> players);

    /**
     * Removes the specified player from viewing the boss bar. If the player is not
     * connected or the boss bar is not viewable the removal packet won't be sent.
     *
     * @param player the player you wish to remove from the boss bar
     */
    void removePlayer(ProxiedPlayer player);

    /**
     * Removes all specified players from viewing the boss bar.
     *
     * @param players the players you wish to remove from the boss bar
     * @see #removePlayer(ProxiedPlayer)
     */
    void removePlayers(Iterable<ProxiedPlayer> players);

    /**
     * Removes all added players from the boss bar.
     */
    void removeAllPlayers();

    /**
     * Returns a immutable copy of all added players.
     *
     * @return immutable list with all added players
     */
    Collection<ProxiedPlayer> getPlayers();

    /**
     * Gets the title (name) of the boss bar.
     *
     * @return title (name)
     */
    BaseComponent[] getTitle();

    /**
     * Sets a new boss bar title (name)
     *
     * @param title the title you wish to be displayed on the boss bar
     */
    void setTitle(BaseComponent[] title);

    /**
     * Gets the progress of the boss bar. Represents a float number between
     * 0 and 1 included.
     *
     * @return progress
     */
    float getProgress();

    /**
     * Sets a new progress of the boss bar.
     *
     * @param progress new progress. should be a float between 0 and 1 included
     */
    void setProgress(float progress);

    /**
     * Gets the color of the boss bar.
     *
     * @return color
     */
    BarColor getColor();

    /**
     * Sets a new boss bar color
     *
     * @param color the color you wish the boss bar to be
     */
    void setColor(BarColor color);

    /**
     * Gets the style of the boss bar
     *
     * @return style
     */
    BarStyle getStyle();

    /**
     * Sets a new style of the boss bar
     *
     * @param style the style you wish the boss bar to be
     */
    void setStyle(BarStyle style);

    /**
     * Returns whenever the boss bar is visible. By default it will return <code>true</code>
     *
     * @return <code>true</code> if visible, <code>false</code> otherwise
     */
    boolean isVisible();

    /**
     * Sets the boss bar's visibility.
     *
     * @param visible value
     */
    void setVisible(boolean visible);

    /**
     * Returns a immutable copy of all flags being added to the boss bar. By default,
     * the collection returned is empty.
     *
     * @return flags
     */
    Collection<BarFlag> getFlags();

    /**
     * Adds flag(s) to the boss bar
     *
     * @param flags the flag(s) you wish to add
     */
    void addFlags(BarFlag... flags);

    /**
     * Removes flag from the boss bar
     *
     * @param flag the flag you wish to remove
     */
    void removeFlag(BarFlag flag);

    /**
     * Removes flag(s) from the boss bar
     *
     * @param flags the flag(s) you wish to remove
     */
    void removeFlags(BarFlag... flags);

}
