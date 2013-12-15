package net.md_5.bungee.api;

import net.md_5.bungee.api.connection.Server;

public interface Location
{

    /**
     * Gets the x-value of the location
     */
    double getX();

    /**
     * Gets the y-value of the location
     */
    double getY();

    /**
     * Gets the z-value of the location
     */
    double getZ();

    /**
     * Gets the yaw-value of the location
     */
    float getYaw();

    /**
     * Gets the pitch-value of the location
     */
    float getPitch();

    /**
     * Gets the server this player is connected to
     */
    Server getServer();

    /**
     * Gets the current dimension of this player
     */
    int getDimension();

    /**
     * Sets a new x for this location This wont affect the players real
     * position!
     */
    void setX(double x);

    /**
     * Sets a new y for this location This wont affect the players real
     * position!
     */
    void setY(double y);

    /**
     * Sets a new z for this location This wont affect the players real
     * position!
     */
    void setZ(double z);

    /**
     * Sets a new yaw for this location This wont affect the players real
     * position!
     */
    void setYaw(float yaw);

    /**
     * Sets a new pitch for this location This wont affect the players real
     * position!
     */
    void setPitch(float pitch);

    /**
     * Sets a new dimension for this location This wont affect the players real
     * position!
     */
    void setDimension(int dimension);

    public class Dimension
    {

        public static final int NETHER = -1;
        public static final int NORMAL = 0;
        public static final int END = 1;
    }
}