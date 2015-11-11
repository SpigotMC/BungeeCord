package net.md_5.bungee.protocol;

import java.util.Arrays;
import java.util.List;

public class ProtocolConstants
{
    public static final int MINECRAFT_1_8 = 47;
    public static final int MINECRAFT_SNAPSHOT = 85;
    public static final List<String> SUPPORTED_VERSIONS = Arrays.asList(
            "1.8.x",
            "15w45a"
    );

    public enum Direction
    {

        TO_CLIENT, TO_SERVER;
    }
}
