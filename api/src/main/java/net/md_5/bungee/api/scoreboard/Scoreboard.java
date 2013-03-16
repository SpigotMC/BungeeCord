package net.md_5.bungee.api.scoreboard;

import lombok.Data;

@Data
public class Scoreboard
{

    /**
     * Unique name for this scoreboard.
     */
    private final String name;
    /**
     * Text to be displayed with this scoreboard.
     */
    private final String text;
}
