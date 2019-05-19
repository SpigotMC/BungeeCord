package net.md_5.bungee.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a player position
 */
@Data
@AllArgsConstructor
public class Position
{

    /**
     * X axis
     */
    private double x;

    /**
     * Y axis
     */
    private double y;

    /**
     * Z axis
     */
    private double z;

    /**
     * Yaw axis
     */
    private float yaw;

    /**
     * Pitch axis
     */
    private float pitch;

    /**
     * Does the player is on ground
     */
    private boolean onGround;
}
