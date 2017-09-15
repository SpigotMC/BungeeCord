package net.md_5.bungee;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.SkinConfiguration;

/*
 * Bitmask, displayed Skin Parts flags:
 * 
 * Bit 0 (0x01 ): Cape enabled
 * Bit 1 (0x02): Jacket enabled
 * Bit 2 (0x04): Left Sleeve enabled
 * Bit 3 (0x08): Right Sleeve enabled
 * Bit 4 (0x10): Left Pants Leg enabled
 * Bit 5 (0x20): Right Pants Leg enabled
 * Bit 6 (0x40): Hat enabled
 * The most significant bit (bit 7, 0x80) appears to be unused.
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PlayerSkinConfiguration implements SkinConfiguration
{

    // 127 = 01111111
    static final SkinConfiguration SKIN_SHOW_ALL = new PlayerSkinConfiguration( (byte) 127 );
    //
    private final byte bitmask;

    @Override
    public boolean hasCape()
    {
        return ( ( bitmask >> 0 ) & 1 ) == 1;
    }

    @Override
    public boolean hasJacket()
    {
        return ( ( bitmask >> 1 ) & 1 ) == 1;
    }

    @Override
    public boolean hasLeftSleeve()
    {
        return ( ( bitmask >> 2 ) & 1 ) == 1;
    }

    @Override
    public boolean hasRightSleeve()
    {
        return ( ( bitmask >> 3 ) & 1 ) == 1;
    }

    @Override
    public boolean hasLeftPants()
    {
        return ( ( bitmask >> 4 ) & 1 ) == 1;
    }

    @Override
    public boolean hasRightPants()
    {
        return ( ( bitmask >> 5 ) & 1 ) == 1;
    }

    @Override
    public boolean hasHat()
    {
        return ( ( bitmask >> 6 ) & 1 ) == 1;
    }
}
