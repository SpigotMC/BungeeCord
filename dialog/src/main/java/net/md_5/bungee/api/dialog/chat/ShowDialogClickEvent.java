package net.md_5.bungee.api.dialog.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.dialog.Dialog;

/**
 * Click event which displays either a pre-existing dialog by key or a custom
 * dialog.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShowDialogClickEvent extends ClickEvent
{

    /**
     * Key for a pre-existing dialog to show.
     */
    private String reference;
    /**
     * Dialog to show.
     */
    private Dialog dialog;

    public ShowDialogClickEvent(String reference)
    {
        this( reference, null );
    }

    public ShowDialogClickEvent(Dialog dialog)
    {
        this( null, dialog );
    }

    private ShowDialogClickEvent(String reference, Dialog dialog)
    {
        super( Action.SHOW_DIALOG, null );
        this.reference = reference;
        this.dialog = dialog;
    }
}
