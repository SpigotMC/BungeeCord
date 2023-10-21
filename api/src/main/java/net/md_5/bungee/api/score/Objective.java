package net.md_5.bungee.api.score;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.chat.BaseComponent;

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
    private BaseComponent value;
    /**
     * Type; integer or hearts
     */
    private String type;
}
