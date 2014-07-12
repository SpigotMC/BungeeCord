package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.api.chat.ClickEvent.Action;

@Getter
@ToString
@RequiredArgsConstructor
public final class ClickEvent
{

    /**
     * The type of action to preform on click
     */
    private final Action action;
    /**
     * Depends on action
     *
     * @see Action
     */
    private final String value;

    public enum Action
    {

        /**
         * Open a url at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value} into the players
         * text box
         */
        SUGGEST_COMMAND
    }
}
