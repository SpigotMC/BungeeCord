package net.md_5.bungee.api.score;

import java.util.Collection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Represents a collection of players on a {@link Scoreboard} with properties.
 */
public interface Team
{

    /**
     * Gets the name of this team. It must be unique amongst all teams
     * registered with any one {@link Scoreboard}.
     *
     * @return the identifying name of this team
     */
    String getName();

    /**
     * Gets the name of this team as it will be displayed to players.
     *
     * @return the friendly name of this objective
     */
    String getDisplayName();

    /**
     * Sets the name displayed to players for this team. This must not be longer
     * than 32 characters.
     *
     * @param displayName the friendly name to set
     */
    void setDisplayName(String displayName);

    /**
     * Gets the prefix which will be prepended to the names of all players on
     * this team.
     *
     * @return this teams prefix
     */
    String getPrefix();

    /**
     * Sets the prefix to be prepended to to the names of all players on this
     * team.
     *
     * @param prefix the prefix to set
     */
    void setPrefix(String prefix);

    /**
     * Sets the suffix appended to the names of all players on this team.
     *
     * @return this teams suffix
     */
    String getSuffix();

    /**
     * Sets the suffix to be appended to to the names of all players on this
     * team.
     *
     * @param suffix the suffix to set
     */
    void setSuffix(String suffix);

    /**
     * Gets whether members of this team may harm each other.
     *
     * @return whether or not friendly fire is enabled for this team
     */
    boolean friendlyFire();

    /**
     * Sets whether members of this team may harm each other.
     *
     * @param enabled whether or not to enable friendly fire
     */
    void friendlyFire(boolean enabled);

    /**
     * Sets whether members of this team can see other members, even when they
     * are disguised with a potion of invisibility.
     *
     * @return whether invisible team members can be seen
     */
    boolean friendlyInvisibles();

    /**
     * Sets whether members of this team can see other members, even when they
     * are disguised with a potion of invisibility.
     *
     * @param enabled whether to enable this attribute or not
     */
    void friendlyInvisibles(boolean enabled);

    /**
     * Gets all players present on this team.
     *
     * @return the members of this team
     */
    Collection<ProxiedPlayer> getPlayers();

    /**
     * Gets the scoreboard to which this team is attached.
     *
     * @return the owning scoreboard
     */
    Scoreboard getScoreboard();

    /**
     * Adds a player to this team. This will remove the player from all other
     * teams.
     *
     * @param player the player to add
     */
    void addPlayer(ProxiedPlayer player);

    /**
     * Removes a player from this team.
     *
     * @param player the player to remove
     * @return if this player was successfully removed
     */
    boolean removePlayer(ProxiedPlayer player);
}
