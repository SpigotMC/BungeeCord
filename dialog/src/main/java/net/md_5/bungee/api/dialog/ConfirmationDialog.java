package net.md_5.bungee.api.dialog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.DialogClickAction;

/**
 * Represents a simple dialog with text and two actions at the bottom (default:
 * "yes", "no").
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public final class ConfirmationDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The "yes" click action / bottom (appears on the left).
     */
    private DialogClickAction yes;
    /**
     * The "no" click action / bottom (appears on the right).
     */
    private DialogClickAction no;

    public ConfirmationDialog(DialogBase base)
    {
        this( base, null, null );
    }
}
