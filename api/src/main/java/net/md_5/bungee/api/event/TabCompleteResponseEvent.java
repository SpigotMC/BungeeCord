package net.md_5.bungee.api.event;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Event called when a backend server sends a response to a player asking to
 * tab-complete a chat message or command. Note that this is not called when
 * BungeeCord or a plugin responds to a tab-complete request. Use
 * {@link TabCompleteEvent} for that.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TabCompleteResponseEvent extends TargetedEvent implements Cancellable
{

    /**
     * Whether the event is cancelled.
     */
    private boolean cancelled;

    /**
     * If brigadier support is enabled.
     */
    private final boolean brigadier;

    /**
     * List of affected commands from the tab completion.
     * If brigadier is present this value will be null.
     */
    private final List<String> suggestions;

    /**
     *  Suggestions provided by minecrafts brigadier.
     *  Note: this variable is not named suggestions caused by the method getSuggestions()
     */
    private final Suggestions brigadierSuggestions;

    /**
     * Returns a list of suggestions. The list is mutable for versions < Minecraft 1.13 or if brigadier support is disabled
     * @return list of suggestions
     */
    public List<String> getSuggestions() {
        if(!brigadier) return suggestions;
        List<String> list = new ArrayList<>(brigadierSuggestions.getList().size());
        for(Suggestion suggestion : brigadierSuggestions.getList())
            list.add(suggestion.getText());
        return list;
    }

    public TabCompleteResponseEvent(Connection sender, Connection receiver,
                                    List<String> commands, Suggestions suggestions, boolean brigadier) {
        super( sender, receiver );
        this.suggestions = commands;
        this.brigadierSuggestions = suggestions;
        this.brigadier = brigadier;
    }
}
