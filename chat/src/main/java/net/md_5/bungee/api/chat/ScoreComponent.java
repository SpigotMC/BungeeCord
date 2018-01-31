package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public final class ScoreComponent extends BaseComponent
{

    /**
     * The name of the player whose score should be displayed. Selectors can be used (such as "@p"),
     * though BungeeCord does not guarantee the state of said entity or the validity of the selector. <br>
     * The wildcard '*' can be used to show the reader's own score.
     * (i.e. in a tellraw command to everyone, '*' will show each player their own score in the given objective)<br>
     * BungeeCord makes no guarantees about using wildcards or selectors in this component.
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
