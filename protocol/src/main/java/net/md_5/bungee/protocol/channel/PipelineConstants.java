package net.md_5.bungee.protocol.channel;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PipelineConstants
{
    public static final String TIMEOUT_HANDLER = "timeout";
    public static final String WRITE_TIMEOUT_HANDLER = "write-timeout";
    public static final String PACKET_DECODER = "packet-decoder";
    public static final String PACKET_ENCODER = "packet-encoder";
    public static final String BOSS_HANDLER = "inbound-boss";
    public static final String ENCRYPT_HANDLER = "encrypt";
    public static final String DECRYPT_HANDLER = "decrypt";
    public static final String FRAME_DECODER = "frame-decoder";
    public static final String FRAME_PREPENDER_AND_COMPRESS = "frame-prepender-compress";
    public static final String DECOMPRESS = "decompress";
    public static final String LEGACY_DECODER = "legacy-decoder";
    public static final String LEGACY_KICKER = "legacy-kick";
}
