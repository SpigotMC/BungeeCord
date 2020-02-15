package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This component displays the score based on a player score on the scoreboard.
 * <br>
 * The <b>name</b> is the name of the player stored on the scoreboard, which may
 * be a "fake" player. It can also be a target selector that <b>must</b> resolve
 * to 1 target, and may target non-player entities.
 * <br>
 * With a book, /tellraw, or /title, using the wildcard '*' in the place of a
 * name or target selector will cause all players to see their own score in the
 * specified objective.
 * <br>
 * <b>Signs cannot use the '*' wildcard</b>
 * <br>
 * These values are filled in by the server-side implementation.
 * <br>
 * As of 1.12.2, a bug ( MC-56373 ) prevents full usage within hover events.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class ScoreComponent extends BaseComponent
{

    /**
     * The name of the entity whose score should be displayed.
     */
    private String name;

    /**
     * The internal name of the objective the score is attached to.
     */
    private String objective;

    /**
     * The optional value to use instead of the one present in the Scoreboard.
     */
    private String value = "";

    /**
     * Creates a new score component with the specified name and objective.<br>
     * If not specifically set, value will default to an empty string;
     * signifying that the scoreboard value should take precedence. If not null,
     * nor empty, {@code value} will override any value found in the
     * scoreboard.<br>
     * The value defaults to an empty string.
     *
     * @param name the name of the entity, or an entity selector, whose score
     * should be displayed
     * @param objective the internal name of the objective the entity's score is
     * attached to
     */
    public ScoreComponent(String name, String objective)
    {
        setName( name );
        setObjective( objective );
    }

    /**
     * Creates a score component from the original to clone it.
     *
     * @param original the original for the new score component
     */
    public ScoreComponent(ScoreComponent original)
    {
        super( original );
        setName( original.getName() );
        setObjective( original.getObjective() );
        setValue( original.getValue() );
    }

    @Override
    public ScoreComponent duplicate()
    {
        return new ScoreComponent( this );
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        builder.append( this.value );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
    {
        addFormat( builder );
        builder.append( this.value );
        super.toLegacyText( builder );
    }
}
