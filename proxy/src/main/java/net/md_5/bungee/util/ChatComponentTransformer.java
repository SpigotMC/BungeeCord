package net.md_5.bungee.util;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ScoreComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.protocol.ProtocolConstants;

/**
 * This class transforms chat components by attempting to replace transformable
 * fields with the appropriate value.
 * <br>
 * ScoreComponents are transformed by replacing their
 * {@link ScoreComponent#getName()}} into the matching entity's name as well as
 * replacing the {@link ScoreComponent#getValue()} with the matching value in
 * the {@link net.md_5.bungee.api.score.Scoreboard} if and only if the
 * {@link ScoreComponent#getValue()} is not present.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatComponentTransformer
{

    private static final ChatComponentTransformer INSTANCE = new ChatComponentTransformer();
    /**
     * The Pattern to match entity selectors.
     */
    private static final Pattern SELECTOR_PATTERN = Pattern.compile( "^@([pares])(?:\\[([^ ]*)\\])?$" );

    public BaseComponent legacyHoverTransform(ProxiedPlayer player, BaseComponent next)
    {
        if ( player.getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_1_16 )
        {
            if ( next.getHoverEvent() == null || next.getHoverEvent().isLegacy() )
            {
                return next;
            }
            next = next.duplicate();
            next.getHoverEvent().setLegacy( true );
            if ( next.getHoverEvent().getContents().size() > 1 )
            {
                Content exception = next.getHoverEvent().getContents().get( 0 );
                next.getHoverEvent().getContents().clear();
                next.getHoverEvent().getContents().add( exception );
            }
        }

        return next;
    }

    public static ChatComponentTransformer getInstance()
    {
        return INSTANCE;
    }

    /**
     * Transform a set of components, and attempt to transform the transformable
     * fields. Entity selectors <b>cannot</b> be evaluated. This will
     * recursively search for all extra components (see
     * {@link BaseComponent#getExtra()}).
     *
     * @param player player
     * @param components the component to transform
     * @return the transformed component, or an array containing a single empty
     * TextComponent if the components are null or empty
     * @throws IllegalArgumentException if an entity selector pattern is present
     */
    public BaseComponent transform(ProxiedPlayer player, BaseComponent components)
    {
        return transform( player, false, components );
    }

    /**
     * Transform a set of components, and attempt to transform the transformable
     * fields. Entity selectors <b>cannot</b> be evaluated. This will
     * recursively search for all extra components (see
     * {@link BaseComponent#getExtra()}).
     *
     * @param player player
     * @param transformHover if the hover event should replace contents with
     * value
     * @param root the component to transform
     * @return the transformed component, or an array containing a single empty
     * TextComponent if the components are null or empty
     * @throws IllegalArgumentException if an entity selector pattern is present
     */
    public BaseComponent transform(ProxiedPlayer player, boolean transformHover, BaseComponent root)
    {
        if ( root == null )
        {
            return new TextComponent( "" );
        }

        if ( transformHover )
        {
            root = legacyHoverTransform( player, root );
        }

        if ( root.getExtra() != null && !root.getExtra().isEmpty() )
        {
            List<BaseComponent> list = root.getExtra().stream().map( (extra) -> transform( player, transformHover, extra ) ).collect( Collectors.toList() );
            root.setExtra( list );
        }

        if ( root instanceof ScoreComponent )
        {
            transformScoreComponent( player, (ScoreComponent) root );
        }

        return root;
    }

    /**
     * Transform a ScoreComponent by replacing the name and value with the
     * appropriate values.
     *
     * @param player the player to use for the component's name
     * @param component the component to transform
     */
    private void transformScoreComponent(ProxiedPlayer player, ScoreComponent component)
    {
        Preconditions.checkArgument( !isSelectorPattern( component.getName() ), "Cannot transform entity selector patterns" );

        if ( component.getValue() != null && !component.getValue().isEmpty() )
        {
            return; // pre-defined values override scoreboard values
        }

        // check for '*' wildcard
        if ( component.getName().equals( "*" ) )
        {
            component.setName( player.getName() );
        }

        if ( player.getScoreboard().getObjective( component.getObjective() ) != null )
        {
            Score score = player.getScoreboard().getScore( component.getName() );
            if ( score != null )
            {
                component.setValue( Integer.toString( score.getValue() ) );
            }
        }
    }

    /**
     * Checks if the given string is an entity selector.
     *
     * @param pattern the pattern to check
     * @return true if it is an entity selector
     */
    public boolean isSelectorPattern(String pattern)
    {
        return SELECTOR_PATTERN.matcher( pattern ).matches();
    }
}
