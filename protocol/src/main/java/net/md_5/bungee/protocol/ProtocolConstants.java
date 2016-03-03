package net.md_5.bungee.protocol;

import java.util.Arrays;
import java.util.List;

public class ProtocolConstants
{
    public static final int MINECRAFT_1_7_2 = 4;
    public static final int MINECRAFT_1_7_6 = 5;
    public static final int MINECRAFT_1_8 = 47;
    public static final int MINECRAFT_1_9 = 107;
    public static final List<String> SUPPORTED_VERSIONS = Arrays.asList(
            "1.7.x",
            "1.8.x",
            "1.9"
    );

    public enum Direction
    {

        TO_CLIENT, TO_SERVER;
    }
}
