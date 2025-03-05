package net.md_5.bungee.protocol.channel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Signal send through the channel pipeline indicating that compression state has changed.
 */
@Getter
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class CompressionThresholdSignal
{

    /**
     * The compression threshold.
     */
    private final int threshold;

}
