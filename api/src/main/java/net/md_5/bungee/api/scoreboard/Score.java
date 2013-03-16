package net.md_5.bungee.api.scoreboard;

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
    private final String itemName;
    /**
     * Unique name of the score.
     */
    private final String scoreName;
    /**
     * Value of the score.
     */
    private final int value;
}
