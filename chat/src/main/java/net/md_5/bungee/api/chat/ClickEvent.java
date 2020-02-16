package net.md_5.bungee.api.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ClickEvent
{

    /**
     * The type of action to perform on click.
     */
    private final Action action;
    /**
     * Depends on the action.
     *
     * @see Action
     */
    private final String value;

    public enum Action
    {

        /**
         * Open a url at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}.
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}.
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}.
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value} into the player's
         * text box.
         */
        SUGGEST_COMMAND,
        /**
         * Change to the page number given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value} in a book.
         */
        CHANGE_PAGE,
        /**
         * Copy the string given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value} into the player's
         * clipboard.
         */
        COPY_TO_CLIPBOARD
    }
}
