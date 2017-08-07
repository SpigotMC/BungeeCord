package net.md_5.bungee.api;

/**
 * Represents a player's skin settings. These settings can be changed by the
 * player under Skin Configuration in the Options menu.
 */
public interface SkinConfiguration
{

    boolean hasCape();

    boolean hasJacket();

    boolean hasLeftSleeve();

    boolean hasRightSleeve();

    boolean hasLeftPants();

    boolean hasRightPants();

    boolean hasHat();

}
