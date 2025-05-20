package net.md_5.bungee.protocol;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class ProtocolConstants
{

    private static final boolean SNAPSHOT_SUPPORT = Boolean.getBoolean( "net.md_5.bungee.protocol.snapshot" );
    public static final int MINECRAFT_1_8 = 47;
    public static final int MINECRAFT_1_9 = 107;
    public static final int MINECRAFT_1_9_1 = 108;
    public static final int MINECRAFT_1_9_2 = 109;
    public static final int MINECRAFT_1_9_4 = 110;
    public static final int MINECRAFT_1_10 = 210;
    public static final int MINECRAFT_1_11 = 315;
    public static final int MINECRAFT_1_11_1 = 316;
    public static final int MINECRAFT_1_12 = 335;
    public static final int MINECRAFT_1_12_1 = 338;
    public static final int MINECRAFT_1_12_2 = 340;
    public static final int MINECRAFT_1_13 = 393;
    public static final int MINECRAFT_1_13_1 = 401;
    public static final int MINECRAFT_1_13_2 = 404;
    public static final int MINECRAFT_1_14 = 477;
    public static final int MINECRAFT_1_14_1 = 480;
    public static final int MINECRAFT_1_14_2 = 485;
    public static final int MINECRAFT_1_14_3 = 490;
    public static final int MINECRAFT_1_14_4 = 498;
    public static final int MINECRAFT_1_15 = 573;
    public static final int MINECRAFT_1_15_1 = 575;
    public static final int MINECRAFT_1_15_2 = 578;
    public static final int MINECRAFT_1_16 = 735;
    public static final int MINECRAFT_1_16_1 = 736;
    public static final int MINECRAFT_1_16_2 = 751;
    public static final int MINECRAFT_1_16_3 = 753;
    public static final int MINECRAFT_1_16_4 = 754;
    public static final int MINECRAFT_1_17 = 755;
    public static final int MINECRAFT_1_17_1 = 756;
    public static final int MINECRAFT_1_18 = 757;
    public static final int MINECRAFT_1_18_2 = 758;
    public static final int MINECRAFT_1_19 = 759;
    public static final int MINECRAFT_1_19_1 = 760;
    public static final int MINECRAFT_1_19_3 = 761;
    public static final int MINECRAFT_1_19_4 = 762;
    public static final int MINECRAFT_1_20 = 763;
    public static final int MINECRAFT_1_20_2 = 764;
    public static final int MINECRAFT_1_20_3 = 765;
    public static final int MINECRAFT_1_20_5 = 766;
    public static final int MINECRAFT_1_21 = 767;
    public static final int MINECRAFT_1_21_2 = 768;
    public static final int MINECRAFT_1_21_4 = 769;
    public static final int MINECRAFT_1_21_5 = 770;
    public static final int MINECRAFT_1_21_6 = 1073742075;
    public static final List<String> SUPPORTED_VERSIONS;
    public static final List<Integer> SUPPORTED_VERSION_IDS;

    static
    {
        ImmutableList.Builder<String> supportedVersions = ImmutableList.<String>builder().add(
                "1.8.x",
                "1.9.x",
                "1.10.x",
                "1.11.x",
                "1.12.x",
                "1.13.x",
                "1.14.x",
                "1.15.x",
                "1.16.x",
                "1.17.x",
                "1.18.x",
                "1.19.x",
                "1.20.x",
                "1.21.x"
        );
        ImmutableList.Builder<Integer> supportedVersionIds = ImmutableList.<Integer>builder().add(
                ProtocolConstants.MINECRAFT_1_8,
                ProtocolConstants.MINECRAFT_1_9,
                ProtocolConstants.MINECRAFT_1_9_1,
                ProtocolConstants.MINECRAFT_1_9_2,
                ProtocolConstants.MINECRAFT_1_9_4,
                ProtocolConstants.MINECRAFT_1_10,
                ProtocolConstants.MINECRAFT_1_11,
                ProtocolConstants.MINECRAFT_1_11_1,
                ProtocolConstants.MINECRAFT_1_12,
                ProtocolConstants.MINECRAFT_1_12_1,
                ProtocolConstants.MINECRAFT_1_12_2,
                ProtocolConstants.MINECRAFT_1_13,
                ProtocolConstants.MINECRAFT_1_13_1,
                ProtocolConstants.MINECRAFT_1_13_2,
                ProtocolConstants.MINECRAFT_1_14,
                ProtocolConstants.MINECRAFT_1_14_1,
                ProtocolConstants.MINECRAFT_1_14_2,
                ProtocolConstants.MINECRAFT_1_14_3,
                ProtocolConstants.MINECRAFT_1_14_4,
                ProtocolConstants.MINECRAFT_1_15,
                ProtocolConstants.MINECRAFT_1_15_1,
                ProtocolConstants.MINECRAFT_1_15_2,
                ProtocolConstants.MINECRAFT_1_16,
                ProtocolConstants.MINECRAFT_1_16_1,
                ProtocolConstants.MINECRAFT_1_16_2,
                ProtocolConstants.MINECRAFT_1_16_3,
                ProtocolConstants.MINECRAFT_1_16_4,
                ProtocolConstants.MINECRAFT_1_17,
                ProtocolConstants.MINECRAFT_1_17_1,
                ProtocolConstants.MINECRAFT_1_18,
                ProtocolConstants.MINECRAFT_1_18_2,
                ProtocolConstants.MINECRAFT_1_19,
                ProtocolConstants.MINECRAFT_1_19_1,
                ProtocolConstants.MINECRAFT_1_19_3,
                ProtocolConstants.MINECRAFT_1_19_4,
                ProtocolConstants.MINECRAFT_1_20,
                ProtocolConstants.MINECRAFT_1_20_2,
                ProtocolConstants.MINECRAFT_1_20_3,
                ProtocolConstants.MINECRAFT_1_20_5,
                ProtocolConstants.MINECRAFT_1_21,
                ProtocolConstants.MINECRAFT_1_21_2,
                ProtocolConstants.MINECRAFT_1_21_4,
                ProtocolConstants.MINECRAFT_1_21_5
        );

        if ( SNAPSHOT_SUPPORT )
        {
            // supportedVersions.add( "1.21.x" );
            supportedVersionIds.add( ProtocolConstants.MINECRAFT_1_21_6 );
        }

        SUPPORTED_VERSIONS = supportedVersions.build();
        SUPPORTED_VERSION_IDS = supportedVersionIds.build();
    }

    public enum Direction
    {

        TO_CLIENT, TO_SERVER;
    }
}
