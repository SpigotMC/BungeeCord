package net.md_5.bungee.bossbar;

import java.util.Arrays;
import java.util.EnumSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.boss.BarColor;
import net.md_5.bungee.api.boss.BarFlag;
import net.md_5.bungee.api.boss.BarStyle;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@AllArgsConstructor
public class BossBarMeta
{
    private BaseComponent title;
    private BarColor color;
    private BarStyle style;
    private float progress;
    private EnumSet<BarFlag> flags;

    private boolean visible;

    public BossBarMeta duplicate()
    {
        return new BossBarMeta( title.duplicate(), color, style, progress, EnumSet.copyOf( flags ), visible );
    }

    public void addFlags(BarFlag... flags)
    {
        this.flags.addAll( Arrays.asList( flags ) );
    }

    public void removeFlag(BarFlag flag)
    {
        this.flags.remove( flag );
    }

    public void removeFlags(BarFlag... flags)
    {
        this.flags.removeAll( Arrays.asList( flags ) );
    }

    public byte serializeFlags()
    {
        byte flagMask = 0x00;
        if ( getFlags().contains( BarFlag.DARKEN_SCREEN ) )
        {
            flagMask |= 0x01;
        }
        if ( getFlags().contains( BarFlag.PLAY_BOSS_MUSIC ) )
        {
            flagMask |= 0x02;
        }
        if ( getFlags().contains( BarFlag.CREATE_WORLD_FOG ) )
        {
            flagMask |= 0x04;
        }
        return flagMask;
    }
}
