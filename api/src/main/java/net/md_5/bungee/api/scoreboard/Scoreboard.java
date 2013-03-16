package net.md_5.bungee.api.scoreboard;

import java.util.Collection;
import lombok.Data;

@Data
public class Scoreboard
{

    /**
     * Unique name for this scoreboard.
     */
    private final String name;
    /**
     * Position of this scoreboard.
     */
    private final Position position;
    /**
     * Objectives for this scoreboard.
     */
    private final Collection<Objective> objectives;
    /**
     * Scores for this scoreboard.
     */
    private final Collection<Score> scores;
}
