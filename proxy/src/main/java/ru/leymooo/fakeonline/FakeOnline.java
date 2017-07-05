package ru.leymooo.fakeonline;

import java.util.HashMap;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.config.Configuration;

/**
 *
 * @author Leymooo
 */
public class FakeOnline
{

    @Getter
    private static FakeOnline instance;
    @Getter
    private final HashMap<Range, Float> booster = new HashMap<Range, Float>();

    private boolean enabled;
    private float defaultValue = 1.0f;

    public FakeOnline(boolean enabled, Configuration section)
    {
        instance = this;
        this.enabled = enabled;
        if ( !this.enabled )
        {
            return;
        }
        for ( String boost : section.getKeys() )
        {
            if ( !boost.equals( "default" ) )
            {
                String[] boostArgs = boost.split( ";" );
                booster.put( new Range( Integer.valueOf( boostArgs[0] ), Integer.valueOf( boostArgs[1] ) ), section.getFloat( boost ) );
            }
        }
        this.defaultValue = section.getFloat( "default", defaultValue );
    }

    public int getFakeOnline()
    {
        int online = BungeeCord.getInstance().getOnlineCount();
        if ( !enabled || ru.leymooo.captcha.Configuration.getInstance().isRedisBungee() )
        {
            return online;
        }
        for ( Range range : booster.keySet() )
        {
            if ( range.isBetween( online ) )
            {
                return Math.round( online * booster.get( range ) );
            }
        }
        return Math.round( online * defaultValue );
    }

}
