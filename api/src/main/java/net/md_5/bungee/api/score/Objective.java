package net.md_5.bungee.api.score;

import lombok.Data;

/**
 * Represents an objective entry.
 */
@Data
public class Objective
{

    /**
     * Name of the objective.
     */
    private final String name;
    /**
     * Value of the objective.
     */
    private final String value; // displayName
}
