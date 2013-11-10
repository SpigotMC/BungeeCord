package net.md_5.bungee.api;

import lombok.ToString;
import net.md_5.bungee.api.connection.Server;

public interface Location
{

    double getX();

    double getY();

    double getZ();

    float getYaw();

    float getPitch();

    Server getServer();

    int getDimension();

    void setX(double x);

    void setY(double y);

    void setZ(double z);

    void setYaw(float yaw);

    void setPitch(float pitch);

    void setDimension(int dimension);

    public class Dimension
    {

        public static final int NETHER = -1;
        public static final int NORMAL = 0;
        public static final int END = 1;
    }
}