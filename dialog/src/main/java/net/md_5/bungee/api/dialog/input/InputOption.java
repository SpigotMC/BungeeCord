package net.md_5.bungee.api.dialog.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents an option choice which may form part of a
 * {@link SingleOptionInput}.
 */
@Data
@AllArgsConstructor
@Accessors(fluent = true)
public class InputOption
{

    /**
     * The string value associated with this option, to be submitted when
     * selected.
     */
    private String id;
    /**
     * The text to display for this option.
     */
    private BaseComponent display;
    /**
     * Whether this option is the one initially selected. Only one option may
     * have this value as true (default: first option).
     */
    private boolean initial;

    public InputOption(String id)
    {
        this( id, null, false );
    }
}
