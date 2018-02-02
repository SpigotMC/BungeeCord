package net.md_5.bungee.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ScoreComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.api.score.Scoreboard;
import org.apache.commons.lang.Validate;

import java.util.regex.Pattern;

/**
 * This class transforms chat components by attempting to replace transformable fields with the appropriate value.<br>
 * ScoreComponents are transformed by replacing their {@link ScoreComponent#getName()}} into the matching entity's name
 * as well as replacing the {@link ScoreComponent#getValue()} with the matching value in the {@link net.md_5.bungee.api.score.Scoreboard}
 * if and only if the {@link ScoreComponent#getValue()} is not present.
 */
public final class ChatComponentTransformer
{
    /**
     * The Pattern to match entity selectors.
     */
    private static final Pattern SELECTOR_PATTERN = Pattern.compile( "^@([pares])(?:\\[([^ ]*)\\])?$" );

    /**
     * Transform a given component, and attempt to transform the transformable fields.<br>
     * Entity selectors <b>cannot</b> be evaluated.
     * @param component the component to transform
     * @return the transformed component
     * @throws IllegalArgumentException if an entity selector pattern is present
     */
    public BaseComponent transform(BaseComponent component, Scoreboard scoreboard, ProxiedPlayer player)
    {
        if( component instanceof ScoreComponent )
        {
            return transformScoreComponent( (ScoreComponent) component, scoreboard, player );
        }
        return component;
    }

    /**
     * Transform a ScoreComponent by replacing the name and value with the appropriate values.
     * @param component the component to transform
     * @param scoreboard the scoreboard to retrieve scores from
     * @param player the player to use for the component's name
     * @return the transformed component
     */
    private BaseComponent transformScoreComponent(ScoreComponent component, Scoreboard scoreboard, ProxiedPlayer player)
    {
        Validate.isTrue( !isSelectorPattern( component.getName() ), "Cannot transform entity selector patterns" );

        if( component.getValue() != null && !component.getValue().isEmpty() )
        {
            return component; // pre-defined values override scoreboard values
        }

        // check for '*' wildcard
        if( component.getName().equals( "*" ) )
        {
            component.setName( player.getName() );
        }

        if( scoreboard.getObjective( component.getObjective() ) != null )
        {
            for ( Score boardScore : scoreboard.getScores() )
            {
                if( boardScore.getScoreName().equals( component.getName() ) )
                {
                    component.setValue( Integer.toString( boardScore.getValue() ) );
                }
            }
        }
        return component;
    }

    /**
     * Checks if the given string is an entity selector.
     * @param pattern the pattern to check
     * @return true if it is an entity selector
     */
    public boolean isSelectorPattern(String pattern)
    {
        return SELECTOR_PATTERN.matcher( pattern ).matches();
    }
}
