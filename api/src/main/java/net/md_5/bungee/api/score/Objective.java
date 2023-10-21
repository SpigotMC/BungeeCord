package net.md_5.bungee.api.score;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents an objective entry.
 */
@Data
@AllArgsConstructor
public class Objective
{

    /**
     * Name of the objective.
     */
    private final String name;
    /**
     * Value of the objective.
     */
    private String value;
    /**
     * Type; integer or hearts
     */
    private String type;
}
