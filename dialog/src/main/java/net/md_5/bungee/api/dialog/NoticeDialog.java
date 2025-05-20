package net.md_5.bungee.api.dialog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.DialogClickAction;

/**
 * Represents a simple dialog with text and one action at the bottom (default:
 * "OK").
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public final class NoticeDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The "OK" action button for the dialog.
     */
    private DialogClickAction action;

    public NoticeDialog(DialogBase base)
    {
        this( base, null );
    }
}
