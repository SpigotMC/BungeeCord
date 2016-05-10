package net.md_5.bungee.protocol;

import com.google.common.collect.ImmutableList;

public class ProtocolConstants
{

    public static final int MINECRAFT_1_8 = 47;
    public static final int MINECRAFT_1_9 = 107;
    public static final int MINECRAFT_1_9_1 = 108;
    public static final int MINECRAFT_1_9_2 = 109;
    public static final int MINECRAFT_1_9_4 = 110;
    public static final ImmutableList<String> SUPPORTED_VERSIONS = ImmutableList.of(
            "1.8.x",
            "1.9.x"
    );
    public static final ImmutableList<Integer> SUPPORTED_VERSION_IDS = ImmutableList.of(
            ProtocolConstants.MINECRAFT_1_8,
            ProtocolConstants.MINECRAFT_1_9,
            ProtocolConstants.MINECRAFT_1_9_1,
            ProtocolConstants.MINECRAFT_1_9_2,
            ProtocolConstants.MINECRAFT_1_9_4
    );

    public enum Direction
    {

        TO_CLIENT, TO_SERVER;
    }
}
