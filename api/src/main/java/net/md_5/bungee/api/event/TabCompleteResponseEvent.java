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
     * List of affected commands from the tab completion.
     */
    private final List<String> commands;

    /**
     *  Suggestions provided by minecrafts brigadier.
     *  Note: this variable is not named suggestions caused by the deprecated method getSuggestions()
     */
    private final Suggestions brigadierSuggestions;

    /**
     * List of suggestions sent back to the player.
     * If this list is empty, an empty list is sent back to the client.
     * Changes at this list don't affect the suggested list.
     *
     * @return list from suggestions
     * @deprecated
     */
    @Deprecated
    public List<String> getSuggestions() {
        List<String> list = new ArrayList<>();
        for (Suggestion suggestion : brigadierSuggestions.getList()) {
            list.add(suggestion.getText());
        }
        return list;
    }

    public TabCompleteResponseEvent(Connection sender, Connection receiver, List<String> commands, Suggestions suggestions) {
        super( sender, receiver );
        this.commands = commands;
        this.brigadierSuggestions = suggestions;
    }
}
