package net.md_5.bungee.api.event;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.util.SuggestionList;

/**
 * Event called when a player uses tab completion.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TabCompleteEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;

    /**
     * The message the player has already entered.
     */
    private final String cursor;

    /**
     * Whether force brigadier has been enabled in the BungeeCord config.
     */
    private final boolean brigadier;

    /**
     *  Suggestions provided by minecrafts brigadier.
     *  Note: this variable is not named suggestions caused by the method getSuggestions()
     */
    private final Suggestions brigadierSuggestions;

    // Only used for MC < 1.13
    private final List<String> suggestions;

    /**
     * Returns a list of suggestions.
     * @return list of suggestions
     */
    public List<String> getSuggestions() {
        if(brigadier)
            return new SuggestionList(brigadierSuggestions);
        else
            return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        if(brigadier) {
            brigadierSuggestions.getList().clear();
            for (String suggestion : suggestions) {
                brigadierSuggestions.getList().add(new Suggestion(brigadierSuggestions.getRange(), suggestion));
            }
        } else {
            this.suggestions.clear();
            this.suggestions.addAll(suggestions);
        }
    }

    public TabCompleteEvent(Connection sender, Connection receiver,
                            String cursor, List<String> suggestions, boolean brigadier)
    {
        super( sender, receiver );
        this.cursor = cursor;
        this.brigadier = brigadier;
        if(brigadier) {
            this.brigadierSuggestions = new Suggestions(
                    StringRange.between(cursor.lastIndexOf(' ') + 1, cursor.length()),
                    new ArrayList<Suggestion>());
            this.suggestions = null;
        } else {
            this.brigadierSuggestions = null;
            this.suggestions = suggestions;
        }
    }
}
