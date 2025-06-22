package net.md_5.bungee.api.dialog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.ActionButton;

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

    @NonNull
    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The "OK" action button for the dialog.
     */
    private ActionButton action;

    public NoticeDialog(DialogBase base)
    {
        this( base, null );
    }
}
