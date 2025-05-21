package net.md_5.bungee.api.dialog.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a dialog action which will usually appear as a button.
 */
@Data
@AllArgsConstructor
public class DialogAction
{

    /**
     * The text label of the button, mandatory.
     */
    private BaseComponent label;
    /**
     * The hover tooltip of the button.
     */
    private BaseComponent tooltip;
    /**
     * The width of the button (default: 150, minimum: 1, maximum: 1024).
     */
    private int width;

    public DialogAction(BaseComponent label)
    {
        this( label, null, 150 );
    }
}
