package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
final public class ClickEvent
{

    /**
     * The type of action to preform on click
     */
    private final Action action;
    /**
     * Depends on action
     *
     * @see net.md_5.bungee.api.chat.ClickEvent.Action
     */
    private final String value;

    public enum Action
    {

        /**
         * Open a url at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#getValue()}
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#getValue()}
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#getValue()}
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#getValue()} into the
         * players text box
         */
        SUGGEST_COMMAND
    }

    @Override
    public String toString()
    {
        return String.format( "ClickEvent{action=%s, value=%s}", action, value );
    }
}
