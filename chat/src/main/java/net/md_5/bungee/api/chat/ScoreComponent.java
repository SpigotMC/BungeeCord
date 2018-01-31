package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This component displays the score based on a player score on the scoreboard.<br>
 * The <b>name</b> is the name of the player stored on the scoreboard, which may be a "fake" player.
 * It can also be a target selector that <b>must</b> resolve to 1 target, and may target non-player entities.<br>
 * With a book, /tellraw, or /title, using the wildcard '*' in the place of a name or target selector will cause all
 * players to see their own score in the specified objective.<br>
 * <b>Signs cannot use the wildcard ('*')</b><br>
 *
 * As of 1.12.2, a bug ( MC-56373 ) prevents full usage within hover events.
 */
@Getter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public final class ScoreComponent extends BaseComponent
{

    /**
     * The name of the player whose score should be displayed.
     */
    private final String name;

    /**
     * The internal name of the objective the score is attached to.
     */
    private final String objective;

    /**
     * The optional value to use instead of the one present in the Scoreboard.<br>
     */
    @Setter
    private String value = "";

    /**
     * Creates a score component from the original to clone it.
     *
     * @param original the original for the new score component
     */
    public ScoreComponent(ScoreComponent original)
    {
        super( original );
        this.name = original.getName();
        this.objective = original.getObjective();
        this.value = original.getValue();
    }

    @Override
    public ScoreComponent duplicate()
    {
        return new ScoreComponent( this );
    }

    @Override
    public ScoreComponent duplicateWithoutFormatting()
    {
        return new ScoreComponent( this.name, this.objective, this.value );
    }

    protected void toLegacyText(StringBuilder builder)
    {
        builder.append(this.value);
    }
}
