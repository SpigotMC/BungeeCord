package net.md_5.bungee.api.chat;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClickEvent
{
    /**
     * The type of action to preform on click
     */
    private Action action;
    /**
     * Depends on action
     * @see net.md_5.bungee.api.chat.ClickEvent.Action
     */
    private String value;

    public enum Action
    {
        /**
         * Open a url at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#setValue(String)}
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#setValue(String)}
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#setValue(String)}
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#setValue(String)}
         * into the players text box
         */
        SUGGEST_COMMAND
    }

    @Override
    public String toString()
    {
        return String.format( "ClickEvent{action=%s, value=%s}", action, value );
    }
}
