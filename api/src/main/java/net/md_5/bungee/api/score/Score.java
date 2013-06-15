package net.md_5.bungee.api.score;

import lombok.Data;

/**
 * Represents a scoreboard score entry.
 */
@Data
public class Score
{

    /**
     * Name to be displayed in the list.
     */
    private final String itemName; // Player
    /**
     * Unique name of the score.
     */
    private final String scoreName; // Score
    /**
     * Value of the score.
     */
    private final int value;
}
