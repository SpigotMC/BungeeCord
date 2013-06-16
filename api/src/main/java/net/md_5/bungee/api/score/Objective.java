package net.md_5.bungee.api.score;

/**
 * An objective on a scoreboard that can show scores specific to players. This
 * objective is only relevant to the {@link Scoreboard} which it is associated
 * with.
 */
public interface Objective
{

    /**
     * Gets the name of this objective. It must be unique amongst all objectives
     * registered with any one {@link Scoreboard}.
     *
     * @return the identifying name of this objective
     */
    String getName();

    /**
     * Gets the name of this objective as it will be displayed to players.
     *
     * @return the friendly name of this objective
     */
    String getDisplayName();

    /**
     * Sets the name displayed to players for this objective. This must not be
     * longer than 32 characters.
     *
     * @param displayName the friendly name to set
     */
    void setDisplayName(String displayName);

    /**
     * Gets the scoreboard to which this objective is attached.
     *
     * @return the owning scoreboard
     */
    Scoreboard getScoreboard();

    /**
     * Gets the {@link Score} corresponding to this objective, in the context of
     * the specified target, ie: their score.
     *
     * @param target the target to lookup
     * @return the targets score data
     */
    Score getScore(String target);
}
