package net.md_5.bungee.api.score;

/**
 * A score entry for an item and its corresponding {@link Objective}.
 */
public interface Score
{

    /**
     * Gets the name of item being being tracked by this Score
     *
     * @return this tracked item
     */
    String getItem();

    /**
     * Gets the {@link Objective} being tracked by this Score
     *
     * @return the tracked {@link Objective}
     */
    Objective getObjective();

    /**
     * Gets the current score
     *
     * @return the current score
     */
    int getScore();

    /**
     * Sets the current score.
     *
     * @param score the new score
     */
    void setScore(int score);

    /**
     * Gets the scoreboard which displays this score.
     *
     * @return the {@link Scoreboard} which owns this score
     */
    Scoreboard getScoreboard();
}
